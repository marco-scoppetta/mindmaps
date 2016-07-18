// Load concept-type initially
var conceptType = "type";
var params = $.param({"itemIdentifier": conceptType});


function get(url, callback) {

        $.get(url, function (data) {
            callback(data);
        })

}


// bottom buttons
// Search by item identifier and value
$("#search-form").submit(function () {
    var value = $("#search").val();
    var params = $.param({"itemIdentifier": value});
    get("http://localhost:8080/graph/concept/" + value, function (data) {
        console.log(data);
        _.map(data.content, addNode);
    });
    return false;
});


$(function() {
    $("#graql-result").hide();

    $( "#sendquery" ).click(function(){
        console.log("HERE THE QUERY-> "+$("#graql-string").val());
        get("http://localhost:4567/select?query=" + $("#graql-string").val(), function (data) {
            $("#graql-result").val(data);
            $("#graql-result").show();
            $("#graql-result").resize();


        });
    });
});



