(function(){
    $.fn.iScroll = function(options){

        function set_wrap_width(dom){
            var wid = 0;
            var ul = dom.find('ul');
            var li = dom.find('li');
            li.each(function(){
                wid += $(this).outerWidth(true);
            });
            ul.css('width',wid+1);
            console.info("scroll width:" + (wid+1));
            console.info("scroll length:" + li.length);
        }
        return this.each(function(){
            set_wrap_width($(this));
            this.iScroll = new iScroll(this,options);
        });
    };

    $.fn.tab = function(options){
        var defaults = {
            startIndex:0
        };
        var opts = $.extend({},defaults,options);
        return this.each(function(){
            var me = $(this);
            var navitem = me.find('.tabnav-item');
            var conitem = me.find('.tabcon-item');
            navitem.eq(opts.startIndex).addClass('active');
            conitem.hide().eq(opts.startIndex).fadeIn();
            me.on('click','.tabnav-item',function(){
                var idx = navitem.index(this);
                navitem.eq(idx).addClass('active').siblings().removeClass('active');
                conitem.hide().eq(idx).fadeIn();
            });
        });
    };

    //document.addEventListener('touchmove', function (e) { e.preventDefault(); }, false);
    document.addEventListener("deviceready", function(){
        console.log('Device is Ready!');
        //设置日志定时器
        var loggerTimer = setInterval(function(){
            //调用接口
            //if(WisapeEditer.loggerStr.length == 0) {
            //    console.info("日志未变化");
            //    return
            //}
            //console.info("调用接口:");
            //console.info("调用接口:" + WisapeEditer.loggerStr);
            //cordova.exec(function(retval) {
            //}, function(e) {
            //}, "Logger", "log", [WisapeEditer.loggerStr]);

            WisapeEditer.loggerStr == "";
        },1000*10);

        (function (doc, win) {

            var docEl = doc.documentElement,
                resizeEvt = 'orientationchange' in window ? 'orientationchange' : 'resize',
                recalc = function () {
                    var clientWidth = docEl.clientWidth;
                    if (!clientWidth) return;
                    docEl.style.fontSize = 20 * (clientWidth / 320) + 'px';

                    console.info("html font-size:" + clientWidth);
                    console.info("html font-size:" + $('body').width());
                    console.info("html font-size:" + (20 * (clientWidth / 320) + 'px'));
                };
            recalc();
            //if (!doc.addEventListener) return;
            //win.addEventListener(resizeEvt, recalc, false);
            //doc.addEventListener('DOMContentLoaded', recalc, false);
        })(document, window);

        WisapeEditer.GetNativeData("checkInitState",[],function(data){
            console.info(data);
            WisapeEditer.LoadDefaultData(data,0);
        });
    }, false);


    $(".j-vmiddle").each(function(){
        var _this = $(this),
            parent = _this.parents(".stage-content");
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

})();


function CurentTime()
{
    var date = new Date();
    var seperator1 = "-";
    var seperator2 = ":";
    var month = date.getMonth() + 1;
    var strDate = date.getDate();
    if (month >= 1 && month <= 9) {
        month = "0" + month;
    }
    if (strDate >= 0 && strDate <= 9) {
        strDate = "0" + strDate;
    }
    var currentdate = date.getFullYear() + seperator1 + month + seperator1 + strDate
        + " " + date.getHours() + seperator2 + date.getMinutes()
        + seperator2 + date.getSeconds();
    return currentdate;
}


