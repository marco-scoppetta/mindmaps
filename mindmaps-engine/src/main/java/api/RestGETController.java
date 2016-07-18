package api;

import factory.GraphFactory;
import io.mindmaps.core.implementation.MindmapsTransactionImpl;
import io.mindmaps.graql.api.parser.QueryParser;

import java.util.stream.Collectors;

import static spark.Spark.get;

public class RestGETController {

    MindmapsTransactionImpl graphTransaction;

    public RestGETController() {

        graphTransaction = GraphFactory.getInstance().buildMindmapsGraph();

        get("/hello", (req, res) -> "こんにちは from Mindmaps Engine!");

        get("/select", (req, res) -> {
            QueryParser parser = QueryParser.create(graphTransaction);
            return parser.parseMatchQuery(req.queryParams("query")).resultsString()
                    .map(x -> x.replaceAll("\u001B\\[\\d+[m]", ""))    // instead of replacing use the code in the web page to highlight the syntax
                    .collect(Collectors.joining("\n"));
        });

    }
}
