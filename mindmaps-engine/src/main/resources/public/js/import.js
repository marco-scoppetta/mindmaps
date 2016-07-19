

function post(url, json ,callback) {
        $.post(url, json ).done(callback);
}

$(function() {

    $( "#loadfile" ).click(function(){
        console.log("FILE PATH "+$("#importFilePath").val());

        post("http://"+$(location).attr('host')+"/importFile/","{\"path\":\""+$("#importFilePath").val()+"\"}",
            function(){
            console.log("response "+data);
        });
    });
});



