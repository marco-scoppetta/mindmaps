package io.mindmaps.api;

import io.mindmaps.core.dao.MindmapsGraph;
import io.mindmaps.core.dao.MindmapsTransaction;
import io.mindmaps.factory.GraphFactory;
import io.mindmaps.visualiser.HALConcept;
import spark.Request;
import spark.Response;

import static spark.Spark.get;

public class VisualiserController {

    MindmapsGraph graph;

    public VisualiserController() {

        get("/concepts", this::getConceptsByValue);

        get("/concept/:id", this::getConceptById);

    }

    private String getConceptsByValue(Request req, Response res) {

        // Finish this.

        //read graph name from http header
        String graphName  = "mindmaps";
        GraphFactory.getInstance().getGraph(graphName).newTransaction().getConceptsByValue(req.queryParams("value"));
        return req.queryParams("value");
    }

    private String getConceptById(Request req, Response res) {

        //read graph name from http header
        String graphName  = "mindmaps";
        MindmapsTransaction transaction = GraphFactory.getInstance().getGraph(graphName).newTransaction();

        try {
            return new HALConcept(transaction.getConcept(req.params(":id"))).render();
        } catch (Exception e) {
            // Look into defining exception handling in spark, since it tries to redirect to an error URL automagically
            res.status(400);
            return e.getMessage();
        }
    }

}
