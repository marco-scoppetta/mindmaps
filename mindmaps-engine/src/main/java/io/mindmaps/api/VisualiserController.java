/*
 * MindmapsDB - A Distributed Semantic Database
 * Copyright (C) 2016  Mindmaps Research Ltd
 *
 * MindmapsDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MindmapsDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MindmapsDB. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */

package io.mindmaps.api;

import io.mindmaps.core.dao.MindmapsGraph;
import io.mindmaps.core.dao.MindmapsTransaction;
import io.mindmaps.factory.GraphFactory;
import io.mindmaps.visualiser.HALConcept;
import spark.Request;
import spark.Response;

import static spark.Spark.get;

public class VisualiserController {

    MindmapsGraph graph;

    public VisualiserController() {

        get("/concepts", this::getConceptsByValue);

        get("/concept/:id", this::getConceptById);

    }

    private String getConceptsByValue(Request req, Response res) {

        // Finish this.

        //read graph name from http header
        String graphName  = "mindmaps";
        GraphFactory.getInstance().getGraph(graphName).newTransaction().getConceptsByValue(req.queryParams("value"));
        return req.queryParams("value");
    }

    private String getConceptById(Request req, Response res) {

        //read graph name from http header
        String graphName  = "mindmaps";
        MindmapsTransaction transaction = GraphFactory.getInstance().getGraph(graphName).newTransaction();

        try {
            return new HALConcept(transaction.getConcept(req.params(":id"))).render();
        } catch (Exception e) {
            // Look into defining exception handling in spark, since it tries to redirect to an error URL automagically
            res.status(400);
            return e.getMessage();
        }
    }

}
