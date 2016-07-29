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

package io.mindmaps.conf;

public class ConfigProperties {
    public static final String CONFIG_FILE = "application.properties";
    public static final String GRAPH_CONFIG_PROPERTY = "graphdatabase.config";
    public static final String GRAPH_NAME_PROPERTY = "graphdatabase.default-graph-name";
    public static final String BATCH_SIZE_PROPERTY = "blockingLoader.batch-size";
    public static final String NUM_THREADS_PROPERTY = "blockingLoader.num-threads";
}
