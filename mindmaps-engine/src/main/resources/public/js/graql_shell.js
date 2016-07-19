
function get(url, callback) {

        $.get(url, function (data) {
            callback(data);
        })

}

$(function() {
    $("#graql-result").hide();

    $( "#sendquery" ).click(function(){
        get("http://localhost:4567/select?query=" + $("#graql-string").val(), function (data) {
            $("#graql-result").val(data);
            $("#graql-result").show();
            $("#graql-result").resize();
        });
    });
});



