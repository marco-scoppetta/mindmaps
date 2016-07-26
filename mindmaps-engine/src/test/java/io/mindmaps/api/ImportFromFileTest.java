package io.mindmaps.api;

import io.mindmaps.core.dao.MindmapsGraph;
import io.mindmaps.factoryengine.GraphFactory;
import org.junit.Before;
import org.junit.Test;

public class ImportFromFileTest {

    ImportFromFile importer;

    @Before
    public void setUp() throws Exception {
     //   MindmapsGraph graph = MindmapsTestGraphFactory.newEmptyGraph();
        MindmapsGraph graph = GraphFactory.getInstance().buildMindmapsGraphBatchLoading();
        importer = new ImportFromFile(graph);
    }

    @Test
    public void testLoadOntologyAndData() {
//        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
//        logger.setLevel(Level.INFO);
//
//        ClassLoader classLoader = getClass().getClassLoader();
//        File file = new File(classLoader.getResource("ldbc-snb-ontology.gql").getFile());
//        importer.loadOntologyFromFile(file.getAbsolutePath());
//        file= new File(classLoader.getResource("ldbc-snb-data.gql").getFile());
//        importer.importDataFromFile(file.getAbsolutePath());
    }

}