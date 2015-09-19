$(function(){
    $("#prBack").click(function(){
        console.info("prBack");
        cordova.exec(function(retval) {
            showMessage(retval);
        }, function(e) {
        }, "StoryTemplate", "back", []);
    });
    $("#prEdit").click(function(){
        console.info("prEdit");
        cordova.exec(function(retval) {
        }, function(e) {
        }, "StoryTemplate", "edit",[]);
    });
    $("#publish").click(function(){
        console.info("#publish");
        var retHtml = '<div  class="p-index main" id="con">',retImg = [], $con= $("#con");
        retHtml += $con.html();
        retHtml += '</div>';
        $con.find(".pages-img").each(function(){
            var me = $(this);
            if(me.hasClass("pages-img-bg")) {
                retImg.push(me.css("background-image").split("url(")[1].split(")")[0]);
            } else {
                retImg.push(me.find("img").attr("src"));
            }
        });
        cordova.exec(function(retval) {
        }, function(e) {
        }, "StoryTemplate", "publish", [retHtml,retImg]);
    })
})