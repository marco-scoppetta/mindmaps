package io.mindmaps.core;

import io.mindmaps.core.dao.MindmapsGraph;
import io.mindmaps.core.implementation.MindmapsTransactionImpl;
import io.mindmaps.core.model.Concept;
import io.mindmaps.core.model.Relation;
import io.mindmaps.factoryengine.GraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

class ConceptFixer {
    private final Logger LOG = LoggerFactory.getLogger(ConceptFixer.class);
    private final Cache cache;
    private final MindmapsGraph graph;

    public ConceptFixer(Cache c){
        cache = c;
        graph = GraphFactory.getInstance().buildMindmapsGraph();
    }

    public String createAssertionHashCode(String assertionId) {
        String code = "";
        MindmapsTransactionImpl transaction = (MindmapsTransactionImpl) graph.newTransaction();
        Relation relation = transaction.getRelation(assertionId);
        if(relation != null){
            code = transaction.getUniqueRelationId(relation);
        }
        closeGraph(transaction);
        return code;
    }

    public void deleteDuplicateAssertion(Long assertionId){
        MindmapsTransactionImpl transaction = (MindmapsTransactionImpl) graph.newTransaction();
        transaction.getTinkerPopGraph().traversal().V(assertionId).next().remove();
        commitGraph(transaction);
    }

    public void fixElements(String conceptType, String key, Long... newDegree){
        int MAX_RETRY = 10;
        int retry = 0;
        while (retry < MAX_RETRY){
            boolean complete = false;

            try {
                complete = fixCastings(conceptType, key);
                System.out.print(".");
            } catch (Exception e){
                LOG.error("Error during post processing fixType [Casting Merge] on Key[" + key +  "]", e);
            }

            if(complete)
                return;

            retry = performRetry(retry);
        }

        if (newDegree.length > 0){
            LOG.error("Unable to update degree of key [" + key + "] to [" + newDegree[0] + "] after [" + MAX_RETRY + "] retries");
        } else {
            LOG.error("Error when performing [Casting Merge] fix on key [" + key + "] after [" + MAX_RETRY + "] retries");
        }
    }

    private boolean fixCastings(String type, String key){

        //this transaction used to be open with batch loading enabled, is it necessary?? ask Filipe

        MindmapsTransactionImpl transaction = (MindmapsTransactionImpl) graph.newTransaction();
        boolean commitNeeded = false;
        Set<String> castingIds = cache.getCastingJobs().get(type).get(key);
        Set<Concept> castings = new HashSet<>();

        for (String baseId : castingIds) {
            Concept concept = transaction.getConcept(baseId);
            if(concept != null) {
                castings.add(concept);
            }
        }

        if (castings.size() >= 2) {
            LOG.info("Duplicate castings found and being merged.");
            commitNeeded = true;
            transaction.mergeCastings(castings);
        }

        if(commitNeeded){
            if(!commitGraph(transaction))
                return false;
        } else {
            closeGraph(transaction);
        }

        cache.deleteJobCasting(type, key);
        return true;
    }

    private void closeGraph(MindmapsTransactionImpl dao){
        try {
            dao.close();
        } catch (Exception e) {
            LOG.error("Error while closing graph [" + dao + "] ", e);
        }
    }

    private boolean commitGraph(MindmapsTransactionImpl graphDAO){
        try {
            graphDAO.getTinkerPopGraph().tx().commit();
            return true;
        } catch (Exception e){
            LOG.error("Failed to commit postprocessing job", e);
            return false;
        }
    }

    private int performRetry(int retry){
        retry ++;
        double seed = 1.0 + (Math.random() * 5.0);
        double waitTime = (retry * 2.0)  + seed;
        LOG.error("Unexpected failure performing backoff and retry of [" + waitTime + "]S");

        try {
            Thread.sleep((long) Math.ceil(waitTime * 1000));
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        return retry;
    }


}
