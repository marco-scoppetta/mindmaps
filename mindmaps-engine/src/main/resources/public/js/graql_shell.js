
function get(url, callback) {

        $.get(url, function (data) {
            callback(data);
        })

}

$(function() {
    $("#graql-result").hide();

    $( "#sendquery" ).click(function(){
        $('#myPleaseWait').modal('show');
        get("http://"+$(location).attr('host')+"/select?query=" + $("#graql-string").val(), function (data) {
            $('#myPleaseWait').modal('hide');
            $("#graql-result").val(data);
            $("#graql-result").show();
            $("#graql-result").resize();
        });
    });

    $( "#toggler" ).click(function() {
        $( ".query-row" ).slideToggle( "slow", function() {
            if($(".query-row").is(":hidden")){
                $("#toggler > span").removeClass("glyphicon-menu-up").addClass("glyphicon-menu-down");
            }else{
                $("#toggler > span").removeClass("glyphicon-menu-down").addClass("glyphicon-menu-up");
            }
        });
    });

});



