$(function(){
    $("#prBack").click(function(){
        console.info("prBack");
        cordova.exec(function(retval) {
            console(retval);
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
        var retHtml = '',retImg = [], $con= $("#con");
        $con.find(".pages-item").each(function(i,v){
            retHtml += '<section class="m-page hide" >' + $(this).html().replace("file://","") + "</section>";
        })
        $con.find(".pages-img").each(function(){
            var me = $(this);
            if(me.hasClass("pages-img-bg")) {
                retImg.push((me.css("background-image").split("url(")[1].split(")")[0]+"").replace("file://",""));
            } else {
                retImg.push(me.find("img").attr("src").replace("file://",""));
            }
        });
        console.info(retHtml);
        console.info(retImg);
        cordova.exec(function(retval) {
        }, function(e) {
        }, "StoryTemplate", "publish", [retImg[0],retHtml,retImg]);
    })
    $(".pages-txt[data-href]").click(function(){

        console.info("href click:");
        console.info($(this).attr("data-href"));
        var url = $(this).attr("data-href");

        cordova.exec(function() {
        }, function(e) {
        }, "StoryTemplate", "openLink", [url]);
    })
})

//播放音乐
var lanren = {
    changeClass: function (target,id) {
        console.log();
        var className = $(target).attr('class');
        var ids = document.getElementById(id);
        console.info("classname:" + className);
        (className == 'on')
            ? $(target).removeClass('on').addClass('off')
            : $(target).removeClass('off').addClass('on');
        (className == 'on')
            ? ids.pause()
            : ids.play();
    },
    play:function(){
        $("#media").length && document.getElementById('media').play();
    }
};
lanren.play();

(function (doc, win) {

    var docEl = doc.documentElement,
        recalc = function () {
            var clientWidth = docEl.clientWidth;
            if (!clientWidth) return;
            docEl.style.fontSize = 20 * (clientWidth / 320) + 'px';

            console.info("html font-size:" + clientWidth);
            console.info("html font-size:" + $('body').width());
            console.info("html font-size:" + (20 * (clientWidth / 320) + 'px'));
        };
    recalc();
})(document, window);
