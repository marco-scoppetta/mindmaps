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

import io.mindmaps.core.dao.MindmapsTransaction;
import io.mindmaps.factory.GraphFactory;
import io.mindmaps.visualiser.HALConcept;
import spark.Request;
import spark.Response;

import static spark.Spark.get;

public class VisualiserController {

    public VisualiserController() {

        get("/concepts", this::getConceptsByValue);

        get("/concept/:id", this::getConceptById);

    }

    private String getConceptsByValue(Request req, Response res) {
        GraphFactory.getInstance().getGraph("mindmaps").newTransaction().getConceptsByValue(req.queryParams("value"));
        return req.queryParams("value");
    }

    private String getConceptById(Request req, Response res) {

        MindmapsTransaction transaction = GraphFactory.getInstance().getGraph("mindmaps").newTransaction();

        if (transaction.getConcept(req.params(":id")) != null)
            return new HALConcept(transaction.getConcept(req.params(":id"))).render();
        else {
            res.status(404);
            return "ID not found in the graph.";
        }
    }

}
