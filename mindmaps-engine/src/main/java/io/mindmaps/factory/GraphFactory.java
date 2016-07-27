package io.mindmaps.factory;

import io.mindmaps.core.dao.MindmapsGraph;

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


    //For now this method and buildMindmapGraph are the same, but the future is full of hopes! And we should have in factory a way to enable batchloading on a new graph
//    public MindmapsGraph buildMindmapsGraphBatchLoading() {
//        MindmapsGraph graph = buildGraph(DEFAULT_NAME, graphConfig);
//        return graph;
//    }
//
//    public MindmapsGraph buildMindmapsGraph() {
//        return buildGraph(DEFAULT_NAME, graphConfig);
//    }

    public MindmapsGraph getGraph(String name) {
        return titanGraphFactory.getGraph(name, null, graphConfig);
    }

//    private synchronized MindmapsGraph buildGraph(String name, String config) {
//
//        MindmapsGraph mindmapsGraph = titanGraphFactory.getGraph(name, "localhost", config);
//
//        //Move to Factory:
//        mindmapsGraph.getGraph().configuration().setProperty("ids.block-size", idBlockSize);
//
//        return mindmapsGraph;
//    }
}


