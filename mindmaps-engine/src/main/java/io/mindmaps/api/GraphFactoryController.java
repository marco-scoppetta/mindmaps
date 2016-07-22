package io.mindmaps.api;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import static spark.Spark.get;

public class GraphFactoryController {
    private final Logger LOG = LoggerFactory.getLogger(GraphFactoryController.class);
    private final String CONFIG_FILE = "application.properties";
    private final String GRAPH_CONFIG_PROPERTY = "graphdatabase.config";

    public GraphFactoryController() {

        Properties prop = new Properties();
        try {
            prop.load(getClass().getClassLoader().getResourceAsStream(CONFIG_FILE));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String graphConfigFile = prop.getProperty(GRAPH_CONFIG_PROPERTY);

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
