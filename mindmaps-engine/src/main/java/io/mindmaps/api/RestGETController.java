package io.mindmaps.api;

import io.mindmaps.core.implementation.MindmapsTransactionImpl;
import io.mindmaps.factory.GraphFactory;
import io.mindmaps.graql.api.parser.QueryParser;
import spark.Request;
import spark.Response;

import java.util.stream.Collectors;

import static spark.Spark.get;

public class RestGETController {

    MindmapsTransactionImpl graphTransaction;

    public RestGETController() {
        graphTransaction = GraphFactory.getInstance().buildMindmapsGraph();

        get("/hello", (req, res) -> "こんにちは from Mindmaps Engine!");

        get("/match", this::matchQuery);
    }


    private String matchQuery(Request req, Response res) {
        QueryParser parser = QueryParser.create(graphTransaction);
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
