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

    public synchronized MindmapsGraph getGraph(String name) {
        return titanGraphFactory.getGraph(name, null, graphConfig);
    }

//    public synchronized MindmapsGraph getGraphBtachLoading(String name){
//        //yeeee
//    }
}


