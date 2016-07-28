package io.mindmaps.api;

import io.mindmaps.core.implementation.MindmapsTransactionImpl;
import io.mindmaps.factory.GraphFactory;
import io.mindmaps.graql.api.parser.QueryParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.stream.Collectors;

import static spark.Spark.get;
import static spark.Spark.redirect;

public class RestGETController {

    private final Logger LOG = LoggerFactory.getLogger(RestGETController.class);


    //READ GRAPH NAME FROM HEADER OF HTTP REQUEST!!!!!
    String graphName = "mindmaps";

    public RestGETController() {

        redirect.get("/", "/dashboard");

        get("/hello", (req, res) -> "こんにちは from Mindmaps Engine!");

        get("/match", this::matchQuery);

        get("/metaTypeInstances", this::buildMetaTypeInstancesObject);
    }


    private String buildMetaTypeInstancesObject(Request req, Response res){

        MindmapsTransactionImpl transaction = (MindmapsTransactionImpl) GraphFactory.getInstance().getGraph(graphName).newTransaction();

        JSONObject responseObj = new JSONObject();
        responseObj.put("roles", new JSONArray(transaction.getMetaRoleType().instances().stream().map(x -> x.getId()).toArray()));
        responseObj.put("entities", new JSONArray(transaction.getMetaEntityType().instances().stream().map(x -> x.getId()).toArray()));
        responseObj.put("relations", new JSONArray(transaction.getMetaRelationType().instances().stream().map(x -> x.getId()).toArray()));
        responseObj.put("resources", new JSONArray(transaction.getMetaResourceType().instances().stream().map(x -> x.getId()).toArray()));

        return responseObj.toString();
    }

    private String matchQuery(Request req, Response res) {

        QueryParser parser = QueryParser.create(GraphFactory.getInstance().getGraph(graphName).newTransaction());

        LOG.info("[ Received match query: \"" + req.queryParams("query") + "\"]");

        try {
            return parser.parseMatchQuery(req.queryParams("query"))
                    .resultsString()
                    .map(x -> x.replaceAll("\u001B\\[\\d+[m]", ""))
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            res.status(400);
            return e.getMessage();
        }
    }


}
