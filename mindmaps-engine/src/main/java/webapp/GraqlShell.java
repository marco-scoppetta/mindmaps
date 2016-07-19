package webapp;

import spark.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;

public class GraqlShell {

    public GraqlShell() {

        get("/graqlshell", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            return new ModelAndView(attributes, "template/graql_shell.jin");
        }, new JinjavaEngine());


    }

}
