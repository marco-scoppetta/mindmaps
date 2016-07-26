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

public class MindmapsEngineServer {


    public static void main(String[] args) {

        // --- Spark JAVA configurations ---- //

        Spark.staticFileLocation("/public");

        // Max number of concurrent threads
        //  int maxThreads = 8;
        //  threadPool(maxThreads);

        // Listening port
        //port(9090);

        // --------------------------------- //

        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.INFO);


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
