var WisapePlayer = angular.module('WisapePlayer',[]);
var demension;
var fontSize = 24;
WisapePlayer.value('browser',{
    versions : function() {
        var u = navigator.userAgent, app = navigator.appVersion;
        return {//移动终端浏览器版本信息
            trident : u.indexOf('Trident') > -1, //IE内核
            presto : u.indexOf('Presto') > -1, //opera内核
            webKit : u.indexOf('AppleWebKit') > -1, //苹果、谷歌内核
            gecko : u.indexOf('Gecko') > -1 && u.indexOf('KHTML') == -1, //火狐内核
            mobile : !!u.match(/AppleWebKit.*Mobile.*/)
                || !!u.match(/AppleWebKit/), //是否为移动终端
            ios : !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), //ios终端
            android : u.indexOf('Android') > -1 || u.indexOf('Linux') > -1, //android终端或者uc浏览器
            iPhone : u.indexOf('iPhone') > -1 || u.indexOf('Mac') > -1, //是否为iPhone或者QQHD浏览器
            iPad: u.indexOf('iPad') > -1, //是否iPad
            webApp : u.indexOf('Safari') == -1,//是否web应该程序，没有头部与底部
            google:u.indexOf('Chrome')>-1
        };
    }(),
    language : (navigator.browserLanguage || navigator.language).toLowerCase()
});
WisapePlayer.value('prefix',(function(){
    if(navigator.userAgent.indexOf("MSIE")!=-1){
        return '-ms-';
    }
    if(navigator.userAgent.indexOf("Firefox")!=-1){
        return '-moz-';
    }
    if(navigator.userAgent.indexOf("Chrome")!=-1||
        navigator.userAgent.indexOf("Safari")!=-1||
        navigator.userAgent.indexOf("MicroMessenger")!=-1){
        return '-webkit-';
    }
    if(isOpera=navigator.userAgent.indexOf("Opera")!=-1){
        return '-o-';
    }
})());
WisapePlayer.service('Animator',function(prefix){
    return {
        play:function(page){
            var aniSymbols = page.find('[animation]');
            var aniSymbols_style_array = new Array();
            aniSymbols.each(function(){
                var This = $(this);
                var style = This.attr('style') || '';
                aniSymbols_style_array.push(style);
                This.css('opacity',0);
            });
            aniSymbols.each(function(index){
                var This = $(this);
                var style = aniSymbols_style_array[index];
                var cur_style =  This.attr('style') || '';
                var animation = This.attr('animation').split(' ');
                var ani_Css = prefix + 'animation-name:' + animation[0] + ';'
                    + prefix + 'animation-duration:' + animation[1] + ';'
                    + prefix + 'animation-timing-function:' + animation[2] + ';'
                    + prefix + 'animation-delay:' + animation[3] + ';'
                    + prefix + 'animation-iteration-count:' + animation[4] + ';';
                if(This.attr('animationReady') != 'true') {
                    This.attr('animationReady', 'true');
                    if(prefix = '-webkit-'){
                        This.on('webkitAnimationEnd', function () {
                            This.attr('style', style);
                        });
                        This.on('webkitAnimationStart', function () {
                            This.css('opacity',1);
                        });
                    }else if(prefix = '-o-'){
                        This.on('oanimationend', function () {
                            This.attr('style', style);
                        });
                        This.on('oanimationstart', function () {
                            This.css('opacity',1);
                        });
                    }else if(prefix = '-ms-'){
                        This.on('MSAnimationEnd', function () {
                            This.attr('style', style);
                        });
                        This.on('MSAnimationStart', function () {
                            This.css('opacity',1);
                        });
                    }else{
                        This.on('animationend', function () {
                            This.attr('style', style);
                        });
                        This.on('animationstart', function () {
                            This.css('opacity',1);
                        });
                    }
                }
                This.attr('style', cur_style + ani_Css);
            });
        }
    };
});
WisapePlayer.directive('story',function(Animator){
    return function($scope, $element, $attrs){
        var $element = $($element);
        var story = $scope.$eval($attrs.story);
        $('head').append('<title>'+story.name+'</title>');
        $('head').append('<meta name="description" content="'+story.des+'"/>');

        var current = 0;
        var curpage = $element.find('[page]').eq(current);
        curpage.css('z-index',3);
        Animator.play(curpage);
    }
});
WisapePlayer.directive('page',function(){
    return function($scope, $element, $attrs){
        var $element = $($element);
        var page = $scope.$eval($attrs.page);
        $scope.pagePath = '../templeteDev/Rcene/mingpian01';
        $scope.$broadcast('getPagePath');
        $element.css({
            position:'absolute',
            width:demension.win_w,
            height:demension.win_h,
            top:0,
            left:0,
            overflow:'hidden'
        });
    }
});
WisapePlayer.directive('layout',function(prefix){
    return function($scope, $element, $attrs){
        var $element = $($element);
        var layout = $scope.$eval($attrs.layout);
        $element.css({
            position:'absolute',
            width:demension.width,
            height:demension.height,
            top:demension.y,
            left:demension.x
        });
        if(layout.scale == 'both'){
            $element.css(prefix+'transform','scale('+demension.scale+','+demension.scale+')');
        }else if(layout.scale == 'fill'){
            if(demension.fix == 'x'){
                $element.css(prefix+'transform','scaleX('+demension.scale+')');
            }else if(demension.fix == 'y'){
                $element.css(prefix+'transform','scaleY('+demension.scale+')');
            }
        }
    }
});
WisapePlayer.directive('layer',function(){
    return function($scope, $element, $attrs){
        var $element = $($element);
        $element.css({
            position: 'absolute',
            overflow: 'hidden'
        });
    }
});
WisapePlayer.directive('symbol',function(){
    return function($scope, $element, $attrs){
        var $element = $($element);
        var symbol = $attrs.symbol;

        $scope.$on('getPagePath',function(event){
            var path = $scope.pagePath;
            if(symbol == 'image') {
                $element.attr('src', path + '/' + $element.attr('data-src'));
            }
        })
    }
});
WisapePlayer.controller('MainCtrl',MainCtrl);
function MainCtrl(browser,$scope){
    if(browser.versions.ios || browser.versions.android){
        $scope.mainHtmlUrl = 'mobile.html';
        //移动端meta处理
        //$('head').append('<meta name="viewport" content="width=640, initial-scale='+($(window).width()/640).toFixed(2)+', user-scalable=no"/>');
        $('head').append('<meta name="format-detection"content="telephone=no, email=no" />');
        $('head').append('<link href="css/mobile.css" rel="stylesheet"/>');
        //$('head').append('<style>*{font-size:'+Math.ceil($(window).width()/640*fontSize)+'px}</style>');
        $('head').append('<style>*{font-size:24px;}</style>');
        //初始化demension
        demension = (function(){
            var ratio = 1080/1695;
            var win_w = $(window).width();
            var win_h = $(window).height();
            var win_ratio = win_w/win_h;
            var width, height, x, y, scale, fix;
            if(ratio>win_ratio){
                width = win_h*ratio;
                height = win_h;
                x = (win_w-width)/2;
                y = 0;
                scale = win_w/width;
                fix = 'x';
            }else{
                width = win_w;
                height = win_w/ratio;
                x = 0;
                y = (win_h-height)/2;
                scale = win_h/height;
                fix = 'y';
            }
            return {
                win_w:win_w,
                win_h:win_h,
                width: Math.ceil(width),
                height: Math.ceil(height),
                x: Math.ceil(x),
                y: Math.ceil(y),
                scale: scale,
                fix:fix
            };
        })();

        //禁用系统默认的touchmove
        /*document.addEventListener('touchmove',function(e){
            e.preventDefault();
        },false);*/
    }else{
        $scope.mainHtmlUrl = 'pc.html';
    }
}