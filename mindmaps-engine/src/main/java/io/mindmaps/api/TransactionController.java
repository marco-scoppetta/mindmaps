package io.mindmaps.api;

import io.mindmaps.loader.Loader;

import java.util.UUID;

import static spark.Spark.post;

public class TransactionController {

    Loader loader;

    public TransactionController() {

        loader = new Loader();

        post("/transaction", (request, response) -> {
            UUID uuid = loader.addJob(request.body());
            if (uuid != null) {
                response.status(201);
                return uuid.toString();
            } else {
                response.status(405);
                return "Error";
            }
        });


    }
}
