

function post(url, json ,callback) {
        $.post(url, json ).done(callback);
}

$(function() {

    $( "#loadDataFile" ).click(function(){
        console.log("FILE PATH "+$("#importDataFilePath").val());

        post("http://"+$(location).attr('host')+"/importDataFromFile/","{\"path\":\""+$("#importDataFilePath").val()+"\"}",
            function(){
            console.log("response "+data);
        });
    });

    $( "#loadOntologyfile" ).click(function(){
        console.log("FILE PATH "+$("#importOntologyFilePath").val());

        post("http://"+$(location).attr('host')+"/importOntologyFromFile/","{\"path\":\""+$("#importOntologyFilePath").val()+"\"}",
            function(){
                console.log("response "+data);
            });
    });
});



