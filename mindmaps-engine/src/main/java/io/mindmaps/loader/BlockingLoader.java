package io.mindmaps.loader;

import io.mindmaps.core.Cache;
import io.mindmaps.core.dao.MindmapsGraph;
import io.mindmaps.core.dao.MindmapsTransaction;
import io.mindmaps.core.exceptions.MindmapsValidationException;
import io.mindmaps.core.implementation.MindmapsTransactionImpl;
import io.mindmaps.factory.GraphFactory;
import io.mindmaps.graql.api.query.QueryBuilder;
import io.mindmaps.graql.api.query.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class BlockingLoader {

    private final Logger LOG = LoggerFactory.getLogger(BlockingLoader.class);


    private static final int NUMBER_THREADS = 16;
    private ExecutorService executor;
    private MindmapsGraph graph;
    private Cache cache;
    private ExecutorService flushToCache;
    private Collection<Var> currentBatch;
    private int batchSize = 60;
    private static Semaphore limitSem = new Semaphore(NUMBER_THREADS*2);
    private static final int REPEAT_COMMITS = 5;


    public BlockingLoader() {
        new BlockingLoader(GraphFactory.getInstance().buildMindmapsGraphBatchLoading());
    }

    public BlockingLoader(MindmapsGraph initGraph) {
        flushToCache = Executors.newFixedThreadPool(10);
        cache = Cache.getInstance();
        graph = initGraph;
        executor = Executors.newFixedThreadPool(NUMBER_THREADS);
        currentBatch = new ArrayList<>();

        System.out.println("===============  SEMAPHORE SIZE: "+limitSem.availablePermits());
    }

    public void addToQueue(Collection<Var> vars) {
        currentBatch.addAll(vars);
        if (currentBatch.size() >= batchSize) {
            submitToExecutor(currentBatch);
            currentBatch = new ArrayList<>();
        }
    }
    private void submitToExecutor(Collection<Var> vars) {
        try {
            limitSem.acquire();
            executor.submit(() -> loadData(vars));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void addToQueue(Var var) {
        addToQueue(Collections.singletonList(var));
    }

    public void waitToFinish(){
        if(currentBatch.size()>0){
            executor.submit(() -> loadData(currentBatch));
        }
        try{
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.MINUTES);
            System.out.println("All tasks submitted, waiting for termination..");
        } catch (InterruptedException e){
            throw new RuntimeException(e);
        } finally {
            System.out.println("ALL TASKS DONE!");
            executor = Executors.newFixedThreadPool(50);
        }
    }

    private List<String> loadData(Collection<Var> batch) {
        List<String> errors = new ArrayList<>();

        // Attempt committing the transaction a certain number of times
        // If a transaction fails, it must be repeated from scratch because Titan is forgetful
        for (int i = 0; i < REPEAT_COMMITS; i++) {
            MindmapsTransaction transaction = graph.newTransaction();
            try {

                QueryBuilder.build(transaction).insert(batch).execute();

                if (Thread.currentThread().isInterrupted()) {
                    errors.add("Transaction cancelled");
                    return errors;
                }

                System.out.println();
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
