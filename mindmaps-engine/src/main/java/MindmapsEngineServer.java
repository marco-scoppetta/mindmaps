import api.GraphFactoryController;
import api.ImportFromFile;
import api.RestGETController;
import api.VisualiserController;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import spark.Spark;
import webapp.Dashboard;
import webapp.GraqlShell;
import webapp.Import;
import webapp.Visualiser;

public class MindmapsEngineServer {


    public static void main(String[] args) {

        // --- Spark JAVA configurations ---- //

        Spark.staticFileLocation("/public");

        // Max number of concurrent threads
        //  int maxThreads = 8;
        //  threadPool(maxThreads);

        // Listening port
        // port(9090);

        // --------------------------------- //

        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.ERROR);


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
