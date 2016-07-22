package io.mindmaps.factory;

import io.mindmaps.core.dao.MindmapsGraph;
import io.mindmaps.core.implementation.MindmapsTransactionImpl;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.io.IOException;
import java.util.Properties;

public class GraphFactory {

    private String graphConfig;

    private int idBlockSize;

    private static GraphFactory instance = null;

    private MindmapsGraphFactory titanGraphFactory;


    public static synchronized GraphFactory getInstance() {
        if (instance == null) {
            instance = new GraphFactory();
        }
        return instance;
    }

    public String getGraphConfig() {
        return graphConfig;
    }

    private GraphFactory() {

//        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
//        Configuration conf = ctx.getConfiguration();
//        conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.ERROR);


        titanGraphFactory = new MindmapsTitanGraphFactory();
        Properties prop = new Properties();
        try {
            prop.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
            idBlockSize = Integer.parseInt(prop.getProperty("graph.block-size"));
            graphConfig = prop.getProperty("graphdatabase.config");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public MindmapsGraph buildMindmapsGraphBatchLoading() {
        MindmapsGraph graph = buildGraph(graphConfig);
        graph.newTransaction().enableBatchLoading();
        return graph;
    }

    public MindmapsGraph buildMindmapsGraph() {
        return buildGraph(graphConfig);
    }

    private synchronized MindmapsGraph buildGraph(String config) {

        MindmapsGraph mindmapsGraph = titanGraphFactory.newGraph(config);

        Graph graph = mindmapsGraph.getGraph();
        graph.configuration().setProperty("ids.block-size", idBlockSize);

        return mindmapsGraph;
    }
}
