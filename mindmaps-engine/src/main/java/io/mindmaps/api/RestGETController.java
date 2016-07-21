package io.mindmaps.api;

import io.mindmaps.core.dao.MindmapsGraph;
import io.mindmaps.core.dao.MindmapsTransaction;
import io.mindmaps.core.implementation.MindmapsTransactionImpl;
import io.mindmaps.factory.GraphFactory;
import io.mindmaps.graql.api.parser.QueryParser;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.stream.Collectors;

import static spark.Spark.get;

public class RestGETController {

    MindmapsGraph graph;

    public RestGETController() {
        graph = GraphFactory.getInstance().buildMindmapsGraph();

        get("/hello", (req, res) -> "こんにちは from Mindmaps Engine!");

        get("/match", this::matchQuery);

        get("/metaTypeInstances",(req, res)->{
            JSONObject responseObj = new JSONObject();
            MindmapsTransactionImpl transaction = (MindmapsTransactionImpl)graph.newTransaction();
            responseObj.put("roles",new JSONArray(transaction.getMetaRoleType().instances().stream().map(x -> x.getId()).toArray()));
            responseObj.put("entities", new JSONArray(transaction.getMetaEntityType().instances().stream().map(x -> x.getId()).toArray()));
            responseObj.put("relations", new JSONArray(transaction.getMetaRelationType().instances().stream().map(x -> x.getId()).toArray()));
            responseObj.put("resources", new JSONArray(transaction.getMetaResourceType().instances().stream().map(x->x.getId()).toArray()));

            return responseObj.toString();
        });
    }


    private String matchQuery(Request req, Response res) {
        QueryParser parser = QueryParser.create(graph.newTransaction());
        System.out.println("RECEIVED SELECT QUERY " + req.queryParams("query"));

        try {
            return parser.parseMatchQuery(req.queryParams("query"))
                    .resultsString()
                    .map(x -> x.replaceAll("\u001B\\[\\d+[m]", ""))    // instead of replacing use the code in the web page to highlight the syntax (?)
                    .collect(Collectors.joining("\n"));
        }catch(Exception e){
            res.status(400);
            return e.getMessage();
        }
    }




}
