package io.mindmaps;

import io.mindmaps.core.dao.MindmapsGraph;
import io.mindmaps.core.dao.MindmapsTransaction;
import io.mindmaps.core.exceptions.MindmapsValidationException;
import io.mindmaps.factory.GraphFactory;
import io.mindmaps.migration.TransactionManager;
import io.mindmaps.migration.sql.SqlDataMigrator;
import io.mindmaps.migration.sql.SqlSchemaMigrator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class EngineMigrator {

    private static MindmapsGraph graph;
    private static MindmapsTransaction transaction;
    private static SqlSchemaMigrator schemamigrator;
    private static SqlDataMigrator datamigrator;
    private static TransactionManager manager;
    private static Connection connection;

    public static void main() throws MindmapsValidationException, SQLException {

        disableInternalLogs();
        graph = GraphFactory.getInstance().buildMindmapsGraphBatchLoading();

        manager = new TransactionManager(graph);
        schemamigrator = new SqlSchemaMigrator(manager);
        datamigrator = new SqlDataMigrator(manager);

        transaction = graph.newTransaction();
        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost/occrp_datavault", "postgres", "postgres");

        schemamigrator.migrateSchema(connection);
        datamigrator.migrateData(connection);
    }

    public static void disableInternalLogs(){
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.INFO);
    }
}
