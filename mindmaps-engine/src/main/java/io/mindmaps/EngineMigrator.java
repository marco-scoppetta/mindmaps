package io.mindmaps;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.mindmaps.core.dao.MindmapsGraph;
import io.mindmaps.factory.GraphFactory;
import io.mindmaps.graql.api.query.Var;
import io.mindmaps.loader.Loader;
import io.mindmaps.migration.TransactionManager;
import io.mindmaps.migration.sql.SqlDataMigrator;
import io.mindmaps.migration.sql.SqlSchemaMigrator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class EngineMigrator {

    //    private static TransactionManager manager;

    public static void main(String[] args) throws SQLException {
        disableInternalLogs();
        MindmapsGraph graph = GraphFactory.getInstance().buildMindmapsGraphBatchLoading();

//        manager = new TransactionManager(graph);

        Loader loader = new Loader(graph);
        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost/occrp_datavault", "postgres", "postgres");

        SqlSchemaMigrator schemamigrator = new SqlSchemaMigrator(connection);
        SqlDataMigrator datamigrator = new SqlDataMigrator(connection);
        int i = 0;

        Collection<Var> currentBatch = new ArrayList<>();


        TransactionManager manager = new TransactionManager(graph);
        manager.setBatchSize(200);
        while (schemamigrator.hasNext()) {
            manager.insert(schemamigrator.next());
        }

        manager.waitToFinish();

        long start = System.currentTimeMillis();
        while (datamigrator.hasNext()) {
            i++;
//            currentBatch.addAll(datamigrator.next());
//            if (i % 10 == 0) {
//                System.out.println("== NEW BATCH !!! ====> " + i);
            manager.insert(datamigrator.next());
//                currentBatch = new ArrayList<>();
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }

            if (i > 10000) {
                break;
            }
        }

        manager.waitToFinish();
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
