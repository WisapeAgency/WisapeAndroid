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
        var me = $(this);
        if(me.hasClass("disable")) return;
        me.addClass("disable");
        console.info("#publish");
        $(".loading").show();
        var retHtml = '',retImg = [], $con= $("#con");
        $con.find(".pages-item").each(function(i,v){
            retHtml += '<section class="m-page hide" >' + $(this).html().replace("file://","") + "</section>";
        });
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
            setTimeout(function(){
                me.removeClass("disable");
            },1000);
            $(".loading").hide();
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

    $(".j-vmiddle").each(function(){
        var _this = $(this),
            parent = _this.parents("div[symbol=top]");
        scale = 1;
        if(_this.css("transform").split("matrix(")[1]) {
            scale = parseFloat(_this.css("transform").split("matrix(")[1].split(",")[0])
        }
        console.info(_this.css("transform"));
        console.info(_this.parents(".stage-content").height());
        console.info(_this.height());
        _this.css({
            "margin-top": (parent.height() - _this.height()*scale)/2
        })
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
