import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.mindmaps.factory.GraphFactory;
import io.mindmaps.loader.BlockingLoader;
import io.mindmaps.migration.TransactionManager;
import io.mindmaps.migration.sql.SqlDataMigrator;
import io.mindmaps.migration.sql.SqlSchemaMigrator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class EngineMigrator {


    public static void main(String[] args) {
        disableInternalLogs();


        BlockingLoader loader = new BlockingLoader(Integer.parseInt(args[0]), Integer.parseInt(args[1]),"mindmaps");
        Connection connection = null;
        System.out.println("TRYING TO CONNECT TO PSQL");
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://10.0.1.9/occrp_datavault", "postgres", "postgres");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("SUCCESSFULLY CONNECTED");

        SqlSchemaMigrator schemamigrator = new SqlSchemaMigrator(connection);
        SqlDataMigrator datamigrator = new SqlDataMigrator(connection);
        int i = 0;


        System.out.println("about to migrate schema");
        TransactionManager manager = new TransactionManager(GraphFactory.getInstance().getGraph("mindmaps"));
        manager.setBatchSize(200);
        while (schemamigrator.hasNext()) {
            manager.insert(schemamigrator.next());
        }
        manager.waitToFinish();
        System.out.println("schema loaded, migrate data");

        long start = System.currentTimeMillis();
        while (datamigrator.hasNext()) {
            i++;
            loader.addToQueue(datamigrator.next());

            if (i % 20 == 0) {
                System.out.println("== NEW BATCH !!! ====> " + i);
            }

            if (i > 10000) {
                break;
            }
        }

        loader.waitToFinish();
//        while (QueueManager.getInstance().getFinishedJobs() < (10000 / 10)) {
//            try {
//                Thread.sleep(300);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        System.out.println("========== TIME ELAPSED  " + (System.currentTimeMillis() - start));

    }

    public static void disableInternalLogs() {
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.INFO);
    }
}
