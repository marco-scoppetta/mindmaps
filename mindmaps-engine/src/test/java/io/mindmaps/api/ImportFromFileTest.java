package io.mindmaps.api;

import io.mindmaps.core.dao.MindmapsGraph;
import io.mindmaps.factory.GraphFactory;
import io.mindmaps.factory.MindmapsTestGraphFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

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
        importer.loadOntology(getClass().getClassLoader().getResource("ldbc-snb-ontology.gql").getPath());
        importer.importGraph(getClass().getClassLoader().getResource("ldbc-snb-data.gql").getPath());
    }

}