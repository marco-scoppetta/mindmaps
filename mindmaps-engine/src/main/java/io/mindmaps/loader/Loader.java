package io.mindmaps.loader;

import io.mindmaps.core.Cache;
import io.mindmaps.core.exceptions.MindmapsValidationException;
import io.mindmaps.core.implementation.MindmapsTransactionImpl;
import io.mindmaps.factory.GraphFactory;
import io.mindmaps.graql.api.parser.QueryParser;
import io.mindmaps.graql.api.query.QueryBuilder;
import io.mindmaps.graql.api.query.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Loader {
    private final Logger LOG = LoggerFactory.getLogger(Loader.class);

    private QueueManager queueManager;

    private Cache cache;

    private int NUM_THREADS = 9; // READ FROM CONFIG FILE!!

    private ExecutorService flushToCache;
    private static final int REPEAT_COMMITS = 5;
    private static Loader instance = null;

    public static synchronized Loader getInstance() {
        if (instance == null) instance = new Loader();
        return instance;
    }

    private Loader() {
        flushToCache = Executors.newFixedThreadPool(10);
        queueManager = QueueManager.getInstance();
        cache = Cache.getInstance();
    }

    private interface LoadableBatch {
        void load(MindmapsTransactionImpl gam) throws Exception;
    }

    private class loadableString implements LoadableBatch {
        private String stringToLoad;

        public loadableString(String s) {
            stringToLoad = s;
        }

        @Override
        public void load(MindmapsTransactionImpl gam) throws Exception {
            QueryParser.create(gam).parseInsertQuery(stringToLoad).execute();
        }
    }

    private class loadableVars implements LoadableBatch {
        private List<Var> batchToLoad;

        public loadableVars(List<Var> batch) {
            batchToLoad = batch;
        }

        @Override
        public void load(MindmapsTransactionImpl gam) throws Exception {
            QueryBuilder.build(gam).insert(batchToLoad).execute();
        }
    }

    public UUID addJob(String queryString) {
        return queueManager.addJob(() -> loadData(new loadableString(queryString)));
    }

    public UUID addJob(List<Var> batchToLoad) {
        return queueManager.addJob(() -> loadData(new loadableVars(batchToLoad)));
    }

    private List<String> loadData(LoadableBatch batch) {
        List<String> errors = new ArrayList<>();

        // Attempt committing the transaction a certain number of times
        // If a transaction fails, it must be repeated from scratch because Titan is forgetful
        for (int i = 0; i < REPEAT_COMMITS; i++) {
            MindmapsTransactionImpl gam = GraphFactory.getInstance().buildMindmapsGraphBatchLoading();
            try {

                batch.load(gam);

                if (Thread.currentThread().isInterrupted()) {
                    errors.add("Transaction cancelled");
                    return errors;
                }

                Map<String, Map<String, Set<String>>> castingIds = gam.getModifiedCastingIds();
                Map<String, Set<String>> relationIds = gam.getModifiedRelationIds();

                gam.commit();

                //flush to cache for post processing
                if (errors.isEmpty()) {
                    flushToCache.submit(() -> writeNewJobsToCache(castingIds, relationIds));
                }

            } catch (MindmapsValidationException e) {
                System.out.println("Caught exception during validation" + e.getMessage());
                return errors;
            } catch (Exception e) {
                handleError(e, 1);
                continue;
            } finally {
                try {
                    gam.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return errors; //Is empty if no errors found
        }

        errors.add("could not commit to graph after " + REPEAT_COMMITS + " retries");
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

    private void writeNewJobsToCache(Map<String, Map<String, Set<String>>> futureCastingJobs, Map<String, Set<String>> futureAssertionJobs) {
        LOG.info("Updating casting jobs . . .");
        for (Map.Entry<String, Map<String, Set<String>>> entry : futureCastingJobs.entrySet()) {
            String type = entry.getKey();
            for (Map.Entry<String, Set<String>> innerEntry : entry.getValue().entrySet()) {
                String key = innerEntry.getKey();
                for (String value : innerEntry.getValue()) {
                    cache.addJobCasting(type, key, value);
                }
            }
        }

        LOG.info("Updating assertion jobs . . .");
        for (Map.Entry<String, Set<String>> entry : futureAssertionJobs.entrySet()) {
            String type = entry.getKey();
            for (String value : entry.getValue()) {
                cache.addJobAssertion(type, value);
            }
        }
    }


}

