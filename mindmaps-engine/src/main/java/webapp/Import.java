package webapp;

import spark.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;

public class Import {

    public Import() {

        get("/import", (request, response) -> {

            Map<String, Object> attributes = new HashMap<>();
            return new ModelAndView(attributes, "template/import.jin");
        }, new JinjavaEngine());


    }

}
