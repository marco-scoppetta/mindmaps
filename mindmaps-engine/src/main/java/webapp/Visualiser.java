package webapp;

import spark.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;

public class Visualiser {

    public Visualiser() {

        get("/visualiser", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            return new ModelAndView(attributes, "template/visualiser.jin");
        }, new JinjavaEngine());


    }

}
