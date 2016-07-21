package io.mindmaps.api;

import io.mindmaps.core.dao.MindmapsGraph;
import io.mindmaps.core.dao.MindmapsTransaction;
import io.mindmaps.core.implementation.MindmapsTransactionImpl;
import io.mindmaps.factory.GraphFactory;
import spark.Request;
import spark.Response;
import io.mindmaps.visualiser.HALConcept;

import static spark.Spark.get;

public class VisualiserController {

    MindmapsGraph graph;

    public VisualiserController() {

        graph = GraphFactory.getInstance().buildMindmapsGraph();

        get("/concepts", this::getConceptsByValue);

        get("/concept/:id", this::getConceptById);

    }

    private String getConceptsByValue(Request req, Response res) {
        graph.newTransaction().getConceptsByValue(req.queryParams("value"));
        return req.queryParams("value");
    }

    private String getConceptById(Request req, Response res) {
//        graph.getConcept(req.params(":id")).getValue();
        MindmapsTransaction transaction = graph.newTransaction();

       // if (transaction.getConcept(req.params(":id")) != null) {
            System.out.println("hallo ive been request id " + req.params(":id"));
            String risposta = "";
            try {
               risposta = new HALConcept(transaction.getConcept(req.params(":id"))).render();
            }catch(Exception e ){
                System.out.println("E X X X C C C C E E E E P P P P T T T I I I I I O O O O O N N N N N "+e.getMessage()    );
                // Look into defining exception handling in spark, since it tries to redirect to an error URL automagically
                res.status(400);
                return  e.getMessage();
            }
            return risposta;
//        }
//        else {
//            res.status(404);
//            return "ID not found in the graph.";
//        }
    }

}
