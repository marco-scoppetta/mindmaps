function hideElement(elem) {
    elem.hide();
}

function showElement(elem) {
    elem.show();
}

function showModal(modalElem) {
    modalElem.modal('show');
}

function hideModal(modalElem) {
    modalElem.modal('hide');
}

function matchQueryUrl(elem) {
    return "http://" + $(location).attr('host') + "/match?query=" + elem.val();
}

function renderInstanceList(element,array){
    array.sort();
    array.forEach(function(instance){
       // console.log("new instance "+instance);
        element.append("<button class='btn btn-sm instance-btn'>"+instance+"</button>");
    })
}

function renderRoleList(element,array){
    array.sort();
    array.forEach(function(instance){
        // console.log("new instance "+instance);
        element.append("<button class='btn btn-sm role-btn'>"+instance+"</button>");
    })
}

function getMetaInstances(){
    $.ajax({
        url: "http://" + $(location).attr('host') + "/metaTypeInstances",
        type: "get"
    }).done(function(data){
        var responseObj = $.parseJSON(data);
        renderRoleList($("#roles-list"),responseObj.roles);
        renderInstanceList($("#entities-list"),responseObj.entities);
        renderInstanceList($("#relations-list"),responseObj.relations);
        renderInstanceList($("#resources-list"),responseObj.resources);
    });
}

function matchQuery(elem) {
    return $.ajax({
        url: matchQueryUrl(elem),
        type: "get"
    });
}

$(function () {

    // The DOM is loaded!

    var graqlResultTextarea = $("#graql-result");
    var queryErrorSpan = $("#query-error");
    var queryErrorRow = $("#query-error-row");
    var loadingModal = $('#myPleaseWait');
    var togglerButton = $("#toggler");
    var graqlStringTextarea = $("#graql-string");
    var closeErrorButton = $("#close-error");

    hideElement(graqlResultTextarea);
    hideElement(queryErrorRow);
    hideElement($("fieldset"));

    getMetaInstances();

    $("#sendquery").click(function () {
        showModal(loadingModal);

        matchQuery(graqlStringTextarea)

            .done( //  If we have a valid result for our match query!
            function (data) {
                hideModal(loadingModal);
                showElement(togglerButton);
                hideElement(queryErrorRow);
                showElement(graqlResultTextarea)

                graqlResultTextarea.val(data);
                $("fieldset").slideUp();
                $("#toggler > span").removeClass("glyphicon-menu-up").addClass("glyphicon-menu-down");
            })

            .fail(// If there are errors in our request!!
            function (data) {
                hideModal(loadingModal);
                hideElement(togglerButton);
                hideElement(graqlResultTextarea);
                queryErrorSpan.html(JSON.stringify(data.responseText));
                queryErrorRow.slideDown();
            });

    });


    togglerButton.click(function () {
        $("fieldset").slideToggle("slow", function () {
            if ($("fieldset").is(":hidden")) {
                $("#toggler > span").removeClass("glyphicon-menu-up").addClass("glyphicon-menu-down");
            } else {
                $("#toggler > span").removeClass("glyphicon-menu-down").addClass("glyphicon-menu-up");
            }
        });
    });

    closeErrorButton.click(function () {
        queryErrorRow.slideUp();
    });

    // ---- SNIPPET FOR AUTORESIZABLE TEXTAREA ---------  //

    $.each(graqlStringTextarea, function() {
        var offset = this.offsetHeight - this.clientHeight;

        var resizeTextarea = function(el) {
            $(el).css('height', 'auto').css('height', el.scrollHeight + offset);
        };
        graqlStringTextarea.on('keyup input', function() { resizeTextarea(this); });
    });

    // ------- END OF SNIPPET -------------------   //

});

$(document).on("click",".instance-btn", function(){
    $("#graql-string").val("match $x isa "+$(this).text()+";");
});

$(document).on("click",".role-btn", function(){
    $("#graql-string").val("match ("+$(this).text()+" $x);");
});


