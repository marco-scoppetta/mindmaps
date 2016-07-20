
//function get(url, callback) {
//
//        $.get(url, function (data) {
//            callback(data);
//        })
//
//}

$(function() {
    $("#graql-result").hide();
    $("#query-error").hide();
    $("#toggler").hide();

    $( "#sendquery" ).click(function(){
        $('#myPleaseWait').modal('show');
        $.get("http://"+$(location).attr('host')+"/match?query=" + $("#graql-string").val(), function (data) {
            $('#myPleaseWait').modal('hide');
            $("#toggler").show();
            $("#query-error").hide();
            $("#graql-result").val(data);
            $("#graql-result").show();
            $("#graql-result").resize();
        }).fail(function(data){
            $('#myPleaseWait').modal('hide');
            $("#toggler").hide();
            $("#graql-result").hide();
            $("#query-error").html("<span><strong> Oh snap! </strong></span> Error: "+JSON.stringify(data.responseText)+"<span id='close-error' ><strong> X </strong></span>");
            $("#query-error").slideDown();
        });
    });

    $(document).on("click","#close-error",function(){    $("#query-error").slideUp();});

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



