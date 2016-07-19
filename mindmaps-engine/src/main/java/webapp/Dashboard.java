package webapp;

import factory.GraphFactory;
import spark.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;

public class Dashboard {

    public Dashboard() {

        get("/dashboard", (request, response) -> {

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("graphConfig", GraphFactory.getInstance().getGraphConfig());
            attributes.put("body", new ModelAndView(null, "template/graql_shell.jin"));
            return new ModelAndView(attributes, "template/dashboard.jin");
        }, new JinjavaEngine());


    }

}
