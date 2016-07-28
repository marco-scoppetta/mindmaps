/*
 * MindmapsDB - A Distributed Semantic Database
 * Copyright (C) 2016  Mindmaps Research Ltd
 *
 * MindmapsDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MindmapsDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MindmapsDB. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */

package io.mindmaps.loader;

import io.mindmaps.core.Cache;
import io.mindmaps.core.exceptions.MindmapsValidationException;
import io.mindmaps.core.implementation.MindmapsTransactionImpl;
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
    private final String CONFIG_FILE = "application.properties";
    private final String GRAPH_NAME_PROPERTY = "graphdatabase.name";
    private final String BATCH_SIZE_PROPERTY = "blockingLoader.batch-size";
    private final String NUM_THREADS_PROPERTY = "blockingLoader.num-threads";

    private ExecutorService executor;
    private Cache cache;
    private Collection<Var> batch;
    private int batchSize;
    private static Semaphore transactionsSemaphore;
    private static final int REPEAT_COMMITS = 5;
    private String graphName;


    //Empty constructor uses default graph name, num of threads and batch size from config file.
    public BlockingLoader() {
        Properties prop = new Properties();
        try {
            prop.load(getClass().getClassLoader().getResourceAsStream(CONFIG_FILE));
        } catch (Exception e) {
            e.printStackTrace();
        }
        int defaultNumOfThreads = Integer.parseInt(prop.getProperty(NUM_THREADS_PROPERTY));
        int defaultBatchSize = Integer.parseInt(prop.getProperty(BATCH_SIZE_PROPERTY));
        String defaultGraphName = prop.getProperty(GRAPH_NAME_PROPERTY);

        new BlockingLoader(defaultNumOfThreads, defaultBatchSize, defaultGraphName);
    }

    public BlockingLoader(String name) {
        Properties prop = new Properties();
        try {
            prop.load(getClass().getClassLoader().getResourceAsStream(CONFIG_FILE));
        } catch (Exception e) {
            e.printStackTrace();
        }
        int defaultNumOfThreads = Integer.parseInt(prop.getProperty(NUM_THREADS_PROPERTY));
        int defaultBatchSize = Integer.parseInt(prop.getProperty(BATCH_SIZE_PROPERTY));

        new BlockingLoader(defaultNumOfThreads, defaultBatchSize, name);
    }

    public BlockingLoader(int numThreads, int batchSizeInit, String graphNameInit) {

        graphName = graphNameInit;
        cache = Cache.getInstance();
        batch = new HashSet<>();

        batchSize = batchSizeInit;
        executor = Executors.newFixedThreadPool(numThreads);
        transactionsSemaphore = new Semaphore(numThreads * 3);

    }

    public void addToQueue(Collection<Var> vars) {
        batch.addAll(vars);
        if (batch.size() >= batchSize) {
            submitToExecutor(batch);
            batch = new HashSet<>();
        }
    }

    private void submitToExecutor(Collection<Var> vars) {
        try {
            transactionsSemaphore.acquire();
            executor.submit(() -> loadData(graphName, vars));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void addToQueue(Var var) {
        addToQueue(Collections.singletonList(var));
    }

    public void waitToFinish() {
        if (batch.size() > 0) {
            executor.submit(() -> loadData(graphName, batch));
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

        for (int i = 0; i < REPEAT_COMMITS; i++) {
            // we probably want to be able to swith ON or OFF batchloading, so maybe just better ask for graph and decide here whether activate batchloading
            MindmapsTransactionImpl transaction = (MindmapsTransactionImpl) GraphFactory.getInstance().getGraphBtachLoading(name).newTransaction();
            try {

                QueryBuilder.build(transaction).insert(batch).execute();

                if (Thread.currentThread().isInterrupted()) {
                    errors.add("Transaction cancelled");
                    return errors;
                }

                transaction.commit();

                if (errors.isEmpty()) {
                    cache.addCacheJob(transaction.getModifiedCastingIds(), transaction.getModifiedRelationIds());
                }

                transactionsSemaphore.release();
                return errors; //Is empty if no errors found

            } catch (MindmapsValidationException e) {
                //If it's a validation exception there is no point in re-trying
                System.out.println("Caught exception during validation" + e.getMessage());

                transactionsSemaphore.release();
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
        transactionsSemaphore.release();
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
