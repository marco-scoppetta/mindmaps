/*
 * MindmapsDB - A Distributed Semantic Database
 * Copyright (C) 2016  Mindmaps Research Ltd
 *
 * MindmapsDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MindmapsDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MindmapsDB. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */

package io.mindmaps.factory;

import io.mindmaps.core.dao.MindmapsGraph;

import java.io.IOException;
import java.util.Properties;

public class GraphFactory {

    private String graphConfig;

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
            graphConfig = prop.getProperty("graphdatabase.config");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public synchronized MindmapsGraph getGraph(String name) {
        return titanGraphFactory.getGraph(name, null, graphConfig);
    }

    public synchronized MindmapsGraph getGraphBtachLoading(String name) {
        MindmapsGraph graph = titanGraphFactory.getGraph(name, null, graphConfig);
        graph.enableBatchLoading();
        return graph;
    }
}


