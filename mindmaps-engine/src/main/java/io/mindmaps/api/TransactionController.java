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

import io.mindmaps.loader.Loader;

import java.util.UUID;

import static spark.Spark.post;

public class TransactionController {

    Loader loader;

    public TransactionController() {

        loader = new Loader();

        post("/transaction", (request, response) -> {
            //read graphName from http
            String graphName = "mindmaps";
            UUID uuid = loader.addJob(graphName,request.body());
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
