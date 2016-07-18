import api.GraphFactoryController;
import api.RestGETController;
import api.VisualiserController;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import spark.Spark;
import webapp.Dashboard;
import webapp.GraqlShell;
import webapp.Visualiser;

public class MindmapsEngineServer {


    public static void main(String[] args) {

        Spark.staticFileLocation("/public");
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.ERROR);

        new RestGETController();
        new VisualiserController();
        new GraphFactoryController();


        // ------ WEB INTERFACE ----- //

        new Dashboard();
        new Visualiser();
        new GraqlShell();

    }
}
