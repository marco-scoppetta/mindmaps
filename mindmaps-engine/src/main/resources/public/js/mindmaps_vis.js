var nodeDataDict = {};  // lookup by href
var nodeVisDict = {};  // lookup by href

var edgeDict = {};

var nodes = new vis.DataSet([]);
var edges = new vis.DataSet([]);

var focusedId = null;
var loadStuff = false;

var colors = {
    conceptInstance : {
        border: "#61BC60",
        background : "#77dd77"
    },
    conceptType : {
        border: "#008B5D",
        background: "#3CB68E"
    },
    relation : {
        border: "#FFA81C",
        background: "#FFBF57"
    },
    relationType : {
        border: "#F73882",
        background: "#FA72A7"
    },
    resource : {
        border: "#46A5C7",
        background: "#5bc2e7"
    },
    resourceType : {
        border: "#4854ED",
        background: "#747DF2"
    },
    roleType : {
        border: "#ffb96d",
        background: "#ffb96d"
    },
    ruleType : {
        border: "#ffb96d",
        background: "#ffb96d"
    },
    metaType : {
        border: "#A15FD1",
        background: "#BF77F3"
    },
    highlight : {
        border: "#77dd77",
        background: "#77dd77"
    }
}


// create a network
var container = document.getElementById('mynetwork');

var vis_data  = {
    nodes: nodes,
    edges: edges
};
var options = {
    edges: {
        arrows: {
            to: true
        }
    },
    physics: {
        solver: "forceAtlas2Based",
    }
};

// Functions on graph data
function getLabel(nodeData) {
    if (nodeData.value !== undefined) {
        return nodeData.value.substring(0, 50);
    } else if (
            nodeData.type === "CASTING" ||
            nodeData.type === "RELATION") {
        return nodeData.isa;
    } else if (nodeData.itemIdentifier !== undefined) {
        return nodeData.itemIdentifier;
    } else {
        return nodeData.type;
    }
}

function getColor(nodeData) {
    var color;

    if (nodeData.type === "ENTITY"){
        color = colors.conceptInstance;
    } else if(nodeData.type === "ENTITY_TYPE") {
        color = colors.conceptType;
    } else if(nodeData.type === "RELATION") {
        color = colors.relation;
    } else if(nodeData.type === "RELATION_TYPE") {
        color = colors.relationType;
    } else if(nodeData.type === "RESOURCE") {
        color = colors.resource;
    } else if(nodeData.type === "RESOURCE_TYPE") {
        color = colors.resourceType;
    } else if (nodeData.type === "ROLE_TYPE") {
        color = colors.roleType;
    } else if (nodeData.type === "RULE_TYPE") {
        color = colors.ruleType;
    } else if (nodeData.type === "TYPE"){
        color = colors.metaType;
    }
    else{color = colors.conceptInstance;}

    return {
        border: color.border,
        background: color.background,
        highlight: {
            border: colors.highlight.border,
            background: color.background
        }
    };
}

function getShape(nodeData) {
    if (
        nodeData.type === "CONCEPT_INSTANCE" ||
        nodeData.type === "RESOURCE" ||
        nodeData.type === "RELATION" ||
        nodeData.type === "CASTING"
    ) {
        return "ellipse";
    } else {
        return "box";
    }
}

function getHref(nodeData) {
    return nodeData.links[0].href;
}

function addNode(nodeData) {
    href = getHref(nodeData);
    if (!(href in nodeVisDict)) {
        nodeVis = {
            id: nodeData.links[0].href,
            label: getLabel(nodeData),
            color: getColor(nodeData),
            selected: false,
            shape: getShape(nodeData),
        };

        nodeVisDict[href] = nodeVis;
        nodeDataDict[href] = nodeData;
        nodes.add(nodeVis);
    }
}

function removeNode(nodeId) {
    nodes.remove(nodeVisDict[nodeId]);
    delete nodeDataDict[nodeId];
    delete nodeVisDict[nodeId];
}

function addEdge(fromNode, toNode, type) {
    var edgeVis = {
        from: getHref(fromNode),
        to: getHref(toNode),
        label: type.toLowerCase().replace("_","-"),
        color: {
            color: "#000000",
            highlight: getColor(fromNode).highlight.border  
        }
    };

    if (!(edgeVis.label in edgeDict)) {
        edgeDict[edgeVis.label] = {};
    }
    if (!(edgeVis.from in edgeDict[edgeVis.label])) {
        edgeDict[edgeVis.label][edgeVis.from] = {}
    }
    if (!(edgeVis.to in edgeDict[edgeVis.label][edgeVis.from])) {
        edgeDict[edgeVis.label][edgeVis.from][edgeVis.to] = edgeVis;
        edges.add(edgeVis);
    }
}

function addEdges(nodeData) {
    $.each(nodeData.out, function(i, edge) {
        addNode(edge.source);
        addNode(edge.target);
        addEdge(edge.source, edge.target, edge.type);
    });

    // Get only a few nodes if too many
    var inNodes = nodeData.in;
    if (nodeData.in.length > 50) {
        inNodes = _.sample(nodeData.in, 50);
    }

    $.each(inNodes, function(i, edge) {
        addNode(edge.source);
        addNode(edge.target);
        addEdge(edge.source, edge.target, edge.type);
    });
}

function removeUnselected() {
    var nodesToRemove = [];
    $.each(nodes.get(), function(i, nodeVis) {
        if (!(nodeVis.selected)) {
            nodesToRemove.push(nodeVis.id);
        }
    });

    $.each(nodesToRemove, function(i, nodeId) {
        removeNode(nodeId);
    })
}

function selectNode(nodeVis) {
    if (!nodeVis.selected && focusedId !== nodeVis.id) {
        // Select node
        nodeVis.selected = true;
        nodeVis.borderWidth = 3;
        nodeVis.color.border = colors.highlight.border;
        nodeVis.shadow = true;
        nodes.update(nodeVis);
    }
}

function expandNode(id) {
    selectNode(nodeVisDict[id]);

    if (focusedId !== id) {
        focusedId = id;
        removeUnselected();
    }

    // Pump out some more attached nodes
    get(getHref(nodeDataDict[id]), addEdges);
}

function loadRandomNode() {
    if (loadStuff) {
        // Select ALL nodes
        _.each(_.values(nodeVisDict), selectNode);

        var node = _.sample(_.values(nodeDataDict), 1)[0];
        expandNode(getHref(node));
        setTimeout(loadRandomNode, 1000);
    }
}

var getCache = {};

function get(url, callback) {
    if (url in getCache) {
        callback(getCache[url])
    } else {
        $.get(url, function (data) {
            getCache[url] = data;
            callback(data);
        })
    }
}

// declare network and define actions
var network = new vis.Network(container, vis_data, options);
network.fit();
network.moveTo({scale: 2})

network.on("click", function (params) {
    if (params.nodes.length !== 0) {
        var id = params.nodes[0];
        var nodeVis = nodeVisDict[id];
        selectNode(nodeVis);
    }
});

network.on("doubleClick", function (params) {
    if (params.nodes.length === 0) {
        focusedId = null;
        removeUnselected();
    } else {
        var id = params.nodes[0];
        expandNode(id);
    }
});

// On right click
network.on("oncontext", function (params) {
    var id = network.getNodeAt(params.pointer.DOM);
    if (id !== undefined) {
        removeNode(id);
        if (focusedId === id) {
            focusedId = null;
            removeUnselected();
        }
    }
});

// Load concept-type initially
var conceptType = "type";
var params = $.param({"itemIdentifier": conceptType});
get("http://localhost:8080/graph/concept/?" + params, addNode);

// bottom buttons
// Search by item identifier and value
$("#search-form").submit(function () {
    var value = $("#search").val();
    var params = $.param({"itemIdentifier": value});
    get("http://localhost:8080/graph/concept/?" + params, addNode);
    get("http://localhost:8080/graph/concept/" + value, function (data) {
        console.log(data);
        _.map(data.content, addNode);
    });
    return false;
});

// Emergency override! Load everything, quick!!!
$("#load-all").click(function () {
    loadStuff = !loadStuff;
    loadRandomNode();
});

