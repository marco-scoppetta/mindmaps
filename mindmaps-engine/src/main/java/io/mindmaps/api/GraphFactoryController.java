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

package io.mindmaps.api;


import io.mindmaps.conf.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import static spark.Spark.get;

public class GraphFactoryController {
    private final Logger LOG = LoggerFactory.getLogger(GraphFactoryController.class);


    public GraphFactoryController() {

        Properties prop = new Properties();
        try {
            prop.load(getClass().getClassLoader().getResourceAsStream(ConfigProperties.CONFIG_FILE));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String graphConfigFile = prop.getProperty(ConfigProperties.GRAPH_CONFIG_PROPERTY);

        //move to config properties
        get("/graph_factory", (req, res) -> {
            try {
                return new String(Files.readAllBytes(Paths.get(graphConfigFile)));
            } catch (IOException e) {
                LOG.error("Cannot find config file [" + graphConfigFile + "]", e);
                res.status(500);
                return "Cannot find config file [" + graphConfigFile + "]";
            }
        });

    }
}
