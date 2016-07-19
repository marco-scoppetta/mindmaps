package api;

import loader.Loader;

import java.util.UUID;

import static spark.Spark.post;

public class TransactionController {

    public TransactionController() {

        post("/transaction", (request, response) -> {
            UUID uuid = Loader.getInstance().addJob(request.body());
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
