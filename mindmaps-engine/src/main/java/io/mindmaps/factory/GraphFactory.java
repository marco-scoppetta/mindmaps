package io.mindmaps.factory;

import io.mindmaps.core.dao.MindmapsGraph;
import io.mindmaps.core.implementation.MindmapsTransactionImpl;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.io.IOException;
import java.util.Properties;

public class GraphFactory {

    private String graphConfig;
    private String DEFAULT_NAME; //TO_DO: This should be parametrised

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
            DEFAULT_NAME = prop.getProperty("graphdatabase.name");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public MindmapsGraph buildMindmapsGraphBatchLoading() {
        MindmapsGraph graph = buildGraph(DEFAULT_NAME,graphConfig);
        //graph.getGraph().configuration(). why dont we enable the batchloading by setting the configuration like we do for block-size?
        graph.newTransaction().enableBatchLoading();  //why this? is this useless if we than close the tx?
        return graph;
    }

    public MindmapsGraph buildMindmapsGraph() {
        return buildGraph(DEFAULT_NAME,graphConfig);
    }

    private synchronized MindmapsGraph buildGraph(String name, String config) {

        MindmapsGraph mindmapsGraph = titanGraphFactory.getGraph(name, "localhost", config);

     //   MindmapsTransactionImpl tx = (MindmapsTransactionImpl)mindmapsGraph.newTransaction();

        Graph graph = mindmapsGraph.getGraph();
//        Graph graph = mindmapsGraph.getTinkerPopGraph(); why is this?


        graph.configuration().setProperty("ids.block-size", idBlockSize);

        return mindmapsGraph;
    }
}


