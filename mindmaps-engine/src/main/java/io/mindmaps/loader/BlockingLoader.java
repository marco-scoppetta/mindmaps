package io.mindmaps.loader;

import io.mindmaps.core.Cache;
import io.mindmaps.core.dao.MindmapsTransaction;
import io.mindmaps.core.exceptions.MindmapsValidationException;
import io.mindmaps.factory.GraphFactory;
import io.mindmaps.graql.api.query.QueryBuilder;
import io.mindmaps.graql.api.query.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class BlockingLoader {

    private final Logger LOG = LoggerFactory.getLogger(BlockingLoader.class);


    private static final int NUMBER_THREADS = 16;
    private ExecutorService executor;
    private Cache cache;
    private ExecutorService flushToCache;
    private Map<String, Collection<Var>> batchesMap;
    private int batchSize = 30;
    private static Semaphore limitSem = new Semaphore(NUMBER_THREADS * 2);
    private static final int REPEAT_COMMITS = 5;


    public BlockingLoader() {

        flushToCache = Executors.newFixedThreadPool(10);
        cache = Cache.getInstance();
        executor = Executors.newFixedThreadPool(NUMBER_THREADS);
        batchesMap = new HashMap<>();

        LOG.info("===============  SEMAPHORE SIZE: " + limitSem.availablePermits());
    }

    public void addToQueue(String name, Collection<Var> vars) {
        if(!batchesMap.containsKey(name)) batchesMap.put(name,new HashSet<>());
        batchesMap.get(name).addAll(vars);
        if (batchesMap.get(name).size() >= batchSize) {
            submitToExecutor(name, batchesMap.get(name));
            batchesMap.remove(name);
            batchesMap.put(name, new HashSet<>());
        }
    }

    private void submitToExecutor(String name, Collection<Var> vars) {
        try {
            limitSem.acquire();
            executor.submit(() -> loadData(name, vars));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void addToQueue(String name, Var var) {
        addToQueue(name, Collections.singletonList(var));
    }

    public void waitToFinish() {
        // scorri tutta la mappa dei batchesMap e vai a tuono
        // How to wait for specific keyspace
        if (batchesMap.get("mindmaps").size() > 0) {
            executor.submit(() -> loadData("mindmaps",batchesMap.get("mindmaps")));
        }
        try {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.MINUTES);
            System.out.println("All tasks submitted, waiting for termination..");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("ALL TASKS DONE!");
            executor = Executors.newFixedThreadPool(50);
        }
    }

    private List<String> loadData(String name, Collection<Var> batch) {
        List<String> errors = new ArrayList<>();

        // Attempt committing the transaction a certain number of times
        // If a transaction fails, it must be repeated from scratch because Titan is forgetful
        for (int i = 0; i < REPEAT_COMMITS; i++) {
            MindmapsTransaction transaction = GraphFactory.getInstance().getGraph(name).newTransaction();
            transaction.enableBatchLoading(); //eventually this will go away
            try {

                QueryBuilder.build(transaction).insert(batch).execute();

                if (Thread.currentThread().isInterrupted()) {
                    errors.add("Transaction cancelled");
                    return errors;
                }

                transaction.commit();

                limitSem.release();
                return errors; //Is empty if no errors found

            } catch (MindmapsValidationException e) {
                //If it's a validation exception there is no point in re-trying
                System.out.println("Caught exception during validation" + e.getMessage());

                limitSem.release();
                return errors;
            } catch (Exception e) {
                //If it's not a validation exception we need to remain in the for loop
                handleError(e, 1);
            } finally {
                try {
                    transaction.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        //If we reach this point it means the transaction failed REPEAT_COMMITS times
        limitSem.release();
        errors.add("Could not commit to graph after " + REPEAT_COMMITS + " retries");
        return errors;
    }

    private void handleError(Exception e, int i) {
        LOG.error("Caught exception ", e);
        e.printStackTrace();

        try {
            Thread.sleep((i + 2) * 1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }
}
