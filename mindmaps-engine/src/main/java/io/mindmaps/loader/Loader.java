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

import io.mindmaps.postprocessing.Cache;
import io.mindmaps.core.exceptions.MindmapsValidationException;
import io.mindmaps.core.implementation.MindmapsTransactionImpl;
import io.mindmaps.factory.GraphFactory;
import io.mindmaps.graql.api.parser.QueryParser;
import io.mindmaps.graql.api.query.QueryBuilder;
import io.mindmaps.graql.api.query.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


public class Loader {
    private final Logger LOG = LoggerFactory.getLogger(Loader.class);

    private QueueManager queueManager;

    private Cache cache;

    private static final int REPEAT_COMMITS = 5;


    public Loader() {
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
        private Collection<Var> batchToLoad;

        public loadableVars(Collection<Var> batch) {
            batchToLoad = batch;
        }

        @Override
        public void load(MindmapsTransactionImpl gam) throws Exception {
            QueryBuilder.build(gam).insert(batchToLoad).execute();
        }
    }


    public UUID addJob(String name, String queryString) {
        return queueManager.addJob(() -> loadData(name, new loadableString(queryString)));
    }

    public UUID addJob(String name, List<Var> batchToLoad) {
        return queueManager.addJob(() -> loadData(name, new loadableVars(batchToLoad)));
    }

    public List<String> loadData(String name, Collection<Var> batch){
        return loadData(name, new loadableVars(batch));
    }

    public List<String> loadData(String name, LoadableBatch batch) {
        List<String> errors = new ArrayList<>();

        // Attempt committing the transaction a certain number of times
        // If a transaction fails, it must be repeated from scratch because Titan is forgetful
        for (int i = 0; i < REPEAT_COMMITS; i++) {
            MindmapsTransactionImpl transaction = (MindmapsTransactionImpl) GraphFactory.getInstance().getGraphBatchLoading(name).newTransaction();
            try {

                batch.load(transaction);

                if (Thread.currentThread().isInterrupted()) {
                    errors.add("Transaction cancelled");
                    return errors;
                }

                transaction.commit();

                //flush to cache for post processing
                if (errors.isEmpty()) {
                    cache.addCacheJob(transaction.getModifiedCastingIds(), transaction.getModifiedRelationIds());
                }
                return errors; //Is empty if no errors found

            } catch (MindmapsValidationException e) {
                //If it's a validation exception there is no point in re-trying
                System.out.println("Caught exception during validation" + e.getMessage());
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

