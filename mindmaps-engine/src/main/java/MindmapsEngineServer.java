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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.mindmaps.api.GraphFactoryController;
import io.mindmaps.api.ImportFromFile;
import io.mindmaps.api.RestGETController;
import io.mindmaps.api.VisualiserController;
import spark.Spark;
import webapp.Dashboard;
import webapp.GraqlShell;
import webapp.Import;
import webapp.Visualiser;

import java.util.Properties;

import static spark.Spark.port;

public class MindmapsEngineServer {

    private static final String CONFIG_FILE = "application.properties";
    private static final String SERVER_PORT_PROPERTY = "server.port";


    public static void main(String[] args) {

        // --- Spark JAVA configurations ---- //

        Spark.staticFileLocation("/public");

        // Max number of concurrent threads
        //  int maxThreads = 8;
        //  threadPool(maxThreads);

        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.INFO);


        Properties prop = new Properties();
        try {
            prop.load(MindmapsEngineServer.class.getClassLoader().getResourceAsStream(CONFIG_FILE));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Listening port
        port(Integer.parseInt(prop.getProperty(SERVER_PORT_PROPERTY)));

        // --------------------------------- //


        // ----- APIs --------- //

        new RestGETController();
        new VisualiserController();
        new GraphFactoryController();
        new ImportFromFile();

        // ------ WEB INTERFACE ----- //
        new Dashboard();
        new Visualiser();
        new GraqlShell();
        new Import();
    }
}
