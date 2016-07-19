

function post(url, json ,callback) {
        $.post(url, json ).done(callback);
}

$(function() {

    $( "#loadfile" ).click(function(){
        console.log("FILE PATH "+$("#importFilePath").val());

        post("http://localhost:4567/importFile/","{\"path\":\""+$("#importFilePath").val()+"\"}",
            function(){
            console.log("response "+data);
        });
    });
});



