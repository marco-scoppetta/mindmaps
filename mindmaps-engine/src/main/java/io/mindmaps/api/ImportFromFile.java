package io.mindmaps.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.mindmaps.core.dao.MindmapsGraph;
import io.mindmaps.core.dao.MindmapsTransaction;
import io.mindmaps.core.exceptions.MindmapsValidationException;
import io.mindmaps.core.implementation.MindmapsTransactionImpl;
import io.mindmaps.factory.GraphFactory;
import io.mindmaps.graql.api.parser.QueryParser;
import io.mindmaps.graql.api.query.Var;
import io.mindmaps.loader.Loader;
import io.mindmaps.loader.QueueManager;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

import static spark.Spark.post;

public class ImportFromFile {


    //Todo:
    // - we add entities to cache before trying to commit. A lot of optimism here
    // - think about how to escape the semi-colon: value "djnjsdk; eoine;"; $person .... this will not work correctly -> Felix should be implementing a feature that allows to read one Pattern at the time given an InputStream


    private final int batchSize = 60;
    private final int sleepTime = 100;

    Map<String, String> entitiesMap;
    ArrayList<Var> relationshipsList;

    private Loader loader;
    private QueueManager queueManager;
    private MindmapsGraph graph;



    public ImportFromFile(){
        new ImportFromFile(GraphFactory.getInstance().buildMindmapsGraphBatchLoading());
    }

    public ImportFromFile(MindmapsGraph initGraph) {
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.INFO);
        graph = initGraph;
        entitiesMap = new ConcurrentHashMap<>();
        relationshipsList = new ArrayList<>();
        loader = new Loader(initGraph);
        queueManager = QueueManager.getInstance();

        post("/importFile/", (req, res) -> {
            JSONObject bodyObject = new JSONObject(req.body());
            System.out.println("PATHHH " + bodyObject.get("path"));
            importGraph(bodyObject.get("path").toString());
            return "ok";
        });

    }

    public void importGraph(String dataFile) {
        System.out.println("LOAD GRAPH FROM GRAQL FILE!");

        BiPredicate<String, List<Var>> parseEntity = this::parseEntity;
        BiPredicate<String, List<Var>> parseRelation = this::parseRelation;

        try {
            scanFile(parseEntity, dataFile);
            scanFile(parseRelation, dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void scanFile(BiPredicate<String, List<Var>> parser, String dataFile) throws IOException {
        int i = 0;
        int latestBatchNumber = 0;
        String line;
        List<Var> currentVarsBatch = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile)));
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("insert")) line = line.substring(6);

//            for (String command : line.split(";")) // we cannot use the split function
//                if (command.length() > 0 && !command.startsWith("#") && parser.test(command, currentVarsBatch))
//                    i++;

            while (queueManager.getTotalJobs() - queueManager.getFinishedJobs() - queueManager.getErrorJobs() > 100) {
                //like in TCP protocol, every time we have to wait this time to let time to the loader to  catch up on the workload, we should increase the sleeping time between one batch and the other!
                // and slowly decrease it again as the time goes by
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (line.length() > 0 && !line.startsWith("#") && parser.test(line, currentVarsBatch))
                i++;

            if (i % batchSize == 0 && latestBatchNumber != i) {
                latestBatchNumber = i;
                loadCurrentBatch(currentVarsBatch);
                System.out.println("== NEW BATCH !!! ====> " + i);
                currentVarsBatch = new ArrayList<>();
            }
        }

        if (currentVarsBatch.size() > 0) {
            loadCurrentBatch(currentVarsBatch);
            System.out.println("== NEW BATCH !!! ====> " + i);
        }

        bufferedReader.close();
    }

    private void loadCurrentBatch(List<Var> currentVarsBatch) {
        final List<Var> finalCurrentVarsBatch = new ArrayList<>(currentVarsBatch);
        loader.addJob(finalCurrentVarsBatch);

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean parseEntity(String command, List<Var> currentVarsBatch) {
        try {

            Var var = (Var) QueryParser.create().parseInsertQuery("insert " + command).admin().getVars().toArray()[0];

            if (!entitiesMap.containsKey(var.admin().getName()) && !var.admin().isRelation() && var.admin().getType().isPresent()) {

                if (var.admin().isUserDefinedName()) {
                    String varId = (var.admin().getId().isPresent()) ? var.admin().getId().get() : UUID.randomUUID().toString();
                    entitiesMap.put(var.admin().getName(), varId); // add check for var name.
                    currentVarsBatch.add(var.admin().id(varId));
                } else {
                    currentVarsBatch.add(var);
                }

                return true;
            }
        } catch (Exception e) {
            System.out.println("Exception caused by " + command);
            e.printStackTrace();
        }
        return false;
    }

    private boolean parseRelation(String command, List<Var> currentVarsBatch) {
        // if both role players have id in the cache then substitute the var to var().id(map.get(variable))
        try {
            Var var = (Var) QueryParser.create().parseInsertQuery("insert " + command).admin().getVars().toArray()[0];
            boolean ready = false;
            if (var.admin().isRelation()) {
                ready = true;

                for (Var.Casting x : var.admin().getCastings()) {
                    if (!x.getRolePlayer().admin().isUserDefinedName())
                        continue; ///aaahhh very ugly
                    if (entitiesMap.containsKey(x.getRolePlayer().getName()))
                        x.getRolePlayer().id(entitiesMap.get(x.getRolePlayer().getName()));
                    else
                        return false; /// aaahhhhhh
                }
                currentVarsBatch.add(var);
            }
            return ready;
        } catch (Exception e) {
            System.out.println("Exception caused by " + command);
            e.printStackTrace();
            return false;
        }

    }

    public void loadOntology(String ontologyFile) {

        MindmapsTransactionImpl transaction = (MindmapsTransactionImpl)graph.newTransaction();

        try {
            System.out.println("============  LOADING ONTOLOGY ==============");

            List<String> lines = Files.readAllLines(Paths.get(ontologyFile), StandardCharsets.UTF_8);
            String query = lines.stream().reduce("", (s1, s2) -> s1 + "\n" + s2);
            QueryParser.create(transaction).parseInsertQuery(query).execute();
            transaction.commit();

            System.out.println("=============  ONTOLOGY LOADED ==============");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
