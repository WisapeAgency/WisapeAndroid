(function(){
    /*动画执行模块*/
    if(navigator.userAgent.indexOf("MSIE")!=-1){prefix = '-ms-';}
    if(navigator.userAgent.indexOf("Firefox")!=-1){prefix = '-moz-';}
    if(navigator.userAgent.indexOf("Chrome")!=-1||
        navigator.userAgent.indexOf("Safari")!=-1){prefix = '-webkit-';}
    if(isOpera=navigator.userAgent.indexOf("Opera")!=-1){
        prefix = '-o-';
    }
    function Animator(stages){
        this.stages = stages || $('stage');
    }
    Animator.prototype.play = function(stageIndex){
        var stage = this.stages.eq(stageIndex);
        var aniSymbols = stage.find('[animation]');
        aniSymbols.each(function(){
            var This = $(this);
            var style = This.attr('style') || '';
            var aniObj,aniCss;

            if(This.get(0).aniObj){
                aniObj = This.get(0).aniObj;
            }else{
                var aniData = This.attr('animation').split('|');
                aniObj = new Object();
                for(var i in aniData){
                    var s = aniData[i].split(':');
                    aniObj[s[0]]=s[1];
                }
                This.get(0).aniObj = aniObj;
            }

            This.attr('animation','ready');
            aniCss = '';
            aniCss += prefix + 'animation-name:' + aniObj.name + ';';
            aniCss += prefix + 'animation-duration:' + aniObj.dura + ';';
            aniCss += prefix + 'animation-timing-function:' + aniObj.func + ';';
            aniCss += prefix + 'animation-iteration-count:' + aniObj.count;

            This.css('opacity',0);
            window.setTimeout(function(){
                This.attr('style',style+aniCss);
            },parseInt(aniObj.delay)*1000);
            This.on('animationend',function(){
                This.attr('style',style);
            });
        });
    };
    window.Animator = Animator.prototype.constructor;
})();