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
            this.iScroll = new IScroll(this,options);
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

        WisapeEditer.GetNativeData("checkInitState",[],function(){

        });
    }, false);

})();
