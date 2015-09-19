var WisapeEditer = WisapeEditer || {};

WisapeEditer = {

    currentTplData : '<div class="stage-content edit-area pages-img pages-img-bg" style="background: url(/mnt/sdcard/wisape/com.wisape.android/data/template/dddddddddddd/img/bg.jpg);background-size: 50% 50%;background-position:cover;width:100%;height:100%;" data="600,900"><div class="symbol" style="z-index: 2;"> <div class="pages-img edit-area"><img data-name="img1" style=""  src="/mnt/sdcard/wisape/com.wisape.android/data/template/dddddddddddd/img/icon-earth.png"/ width="50" height="50"> </div> </div><div class="symbol" style="z-index: 2;"> <div class="pages-img edit-area"><img data-name="img1" style="" src="/mnt/sdcard/wisape/com.wisape.android/data/template/dddddddddddd/img/icon-earth.png"/ width="50" height="50"> </div> </div> <div class="symbol" style="z-index: 3;"> <div class="pages-txt edit-area" style="margin:70px auto;width:150px;color:#fff;text-align: center;">As c update of the classictranslation corpus, the combination of network technology and language essence </div> </div> </div>',

    selectedStagetIndex : 1,

    stageData : [],

    storyData : [],

    initial : function(){

        var menuScroll = $("#menu-scroll");

        //获取模板分类
        WisapeEditer.GetNativeData("getStageCategory", [],function(data){
            console.info("getStageCategory");
            console.info(JSON.stringify(data));

            var source = '<ul class="tabcon-item">'
                +    '{{each list as value i}}'
                +        '<li class="tabnav-item"  data-id="{{value.id}}"> {{value.name}} </li>'
                +    '{{/each}}'
                + '</ul>';

            var render = template.compile(source);
            var html = render({
                list: data
            });

            document.getElementById('menu-scroll').innerHTML = html;
            menuScroll.iScroll( { eventPassthrough: true,scrollX: true, scrollY: false, preventDefault: false });


            //获取默认模板模板列表
            console.info("获取默认模板模板列表:" + parseInt(menuScroll.find("li").data("id")));
            WisapeEditer.LoadTplList(parseInt(menuScroll.find("li").data("id")));
            menuScroll.find("li").eq(0).addClass("active");

            //默认加载的stage，需入口传递
            WisapeEditer.storyData.push(WisapeEditer.currentTplData);
            var pageScroll = $("#pages-scroll");
            pageScroll.find("li").eq(0).append('<span class="count">1/1</span>');


            console.info("WisapeEditer.storyData:");
            console.info(WisapeEditer.storyData);

        });

        //获取字体接口数据
        WisapeEditer.GetNativeData("getFonts", [],function(data){
            console.info("fonts:");
            console.info(data);
            console.info(JSON.stringify(data));
        })

    },

    event : function(){

        //预览
        $("#storyPublish").click(function(){
            var retHtml = "",retImg = [];
            for(var i = 0;i < WisapeEditer.storyData.length;i++) {
                retHtml += WisapeEditer.storyData[i];
            }
            pageScroll.find(".pages-img").each(function(){
                var me = $(this);
                if(me.hasClass("pages-img-bg")) {
                    retImg.push(me.css("background-image"));
                } else {
                    retImg.push(me.find("img").attr("src"));
                }
            });

            console.info("storyPublish");
            console.info(retHtml);
            console.info(retImg);

            WisapeEditer.GetNativeData("preview", [retHtml,retImg],function(){
                console.info("publis done");
            })



        })

        //模板分类事件
        var menuScroll = $("#menu-scroll"),
            pageScroll = $("#pages-scroll");
        console.info("menu-scroll length:" +  menuScroll.length);
        menuScroll.delegate("li","click",function(){

            var _me = $(this);

            console.info("menu-scroll click");
            console.info("id:" + _me.data("id"));

            _me.addClass("active").siblings().removeClass("active");
            WisapeEditer.LoadTplList(parseInt(_me.data("id")));
        });

        //显示隐藏分类
        $("#toggleCat").click(function(){
            $(".tpl-page-cat").toggle();
        });

        //管理stage列表
        $("#manageStageList").click(function(){
            console.info("manageStageList:");
            console.info(typeof WisapeEditer.storyData);
            $("#pageScroll li").find(".ico-acitve,.count").remove();
            var retHtml = "";
            var computedHeight = parseInt($(document).width()/3*1.67);
            console.info($(document).width());
            console.info(computedHeight);
            for(var i in WisapeEditer.storyData) {
                retHtml += '<li class="tpl-page-item" draggable="false" style="height:' + computedHeight +'px"><span class="drag-handle">☰</span><i class="icon-correct"></i>' + WisapeEditer.storyData[i] + '</li>';
            };
            $("#storyDragBox").html(retHtml);
            WisapeEditer.ShowView('main','storyStageList');
        });

        //从管理stage列表页面返回，并保存
        $("#storyStageList .icon-arrow-left").click(function(){
            var retHtml = [];
            $("#storyDragBox").find(".drag-handle,.icon-correct").remove();
            $("#storyDragBox li").each(function(){
                retHtml.push($(this).removeAttr("style").html());
            });
            console.info("storyStageList:");
            console.info(retHtml);
            WisapeEditer.storyData = retHtml;//覆盖更新storyData
            WisapeEditer.LoadStageList(retHtml,function(){//重新加载主界面的stage列表
                WisapeEditer.ShowView('storyStageList','main')
            })

        })

        //模板列表事件
        var catScroll = $("#cat-scroll");
        catScroll.delegate("li","click",function() {

            var _me = $(this);

            _me.addClass("active").siblings().removeClass("active");
            console.info(!_me.hasClass("tpl-exist"));
            //if (!_me.hasClass("tpl-exist")) {
            console.info("down");
            WisapeEditer.GetNativeData("start", [parseInt(_me.data("id")), parseInt(_me.data("type"))], function (data) {
                console.info("down data:");
                console.info(data);
                _me.removeClass("tpl-exist");
            });
            //} else {
            console.info("read:");
            WisapeEditer.GetNativeData("read", ["/mnt/sdcard/wisape/com.wisape.android/data/template/" + _me.data("name") + "/stage.html"], function (data) {
                console.info("read data:");
                console.info(data);
                WisapeEditer.currentTplData = data;
                pageScroll.find("ul li.active").html('<span class="count">' + WisapeEditer.selectedStagetIndex + '/' + WisapeEditer.storyData.length + '</span>' + data);
                WisapeEditer.storyData[WisapeEditer.selectedStagetIndex-1] = data;

                console.info("WisapeEditer.storyData:");
                console.info(WisapeEditer.storyData);
            });
            //}
        });

        //stage列表事件
        var pageScroll = $("#pages-scroll");
        pageScroll.delegate("li","click",function(){
            var _me = $(this);
            WisapeEditer.selectedStagetIndex = _me.index()+1;
            _me.addClass("active").siblings().removeClass("active").find(".pages-img,.pages-txt").removeClass("active");
        });

        pageScroll.delegate("li.active .pages-img","click",function(event){
            var _me = $(this);
            console.info(_me.parent().html());
            if(!_me.hasClass("active")) {
                console.info(pageScroll.find(".edit-area").length);
                pageScroll.find(".edit-area").removeClass("active");
                _me.addClass("active");
                if(_me.children(".ico-acitve").length == 0) _me.append('<i class="ico-acitve"></i>');
                return false;
            }
            cordova.exec(function(retval) {
                console.info("PhotoSelector:" + retval);
                console.info("_me:" + _me.html());
                console.info("_me.hasClass:" + _me.hasClass("pages-img-bg"));
                if(_me.hasClass("pages-img-bg")) {
                    _me.css({"background-image": "url(" + retval + ")"})
                } else {
                    _me.find("img").attr({"src":retval})
                }

                WisapeEditer.storyData[WisapeEditer.selectedStagetIndex-1] = pageScroll.find("li.active").html();

                console.info("WisapeEditer.storyData:");
                console.info(WisapeEditer.storyData);

            }, function(e) {
                alert("Error: "+e);
            }, "PhotoSelector", "execute", [800,600]);
            event.stopPropagation();

        });

        pageScroll.delegate("li.active .pages-txt ","click",function(event){
            var _me = $(this);
            if(!_me.hasClass("active")) {
                console.info(pageScroll.find(".edit-area").length);
                pageScroll.find(".edit-area").removeClass("active");
                _me.addClass("active");
                return false;
            };
            WisapeEditer.UpdateSelectedStage(WisapeEditer.selectedStagetIndex,_me.parents(".stage-content").find(".symbol.active").index());
            WisapeEditer.ShowView('main','editorText');
            event.stopPropagation();
        });

        //新建stage事件
        var AddNewStage = $("#AddNewStage");
        AddNewStage.click(function(){
            var target = pageScroll.find("li.active"),
                pageScrollLi = pageScroll.find("li");

            WisapeEditer.storyData.push(WisapeEditer.currentTplData);

            console.info("WisapeEditer.storyData:");
            console.info(WisapeEditer.storyData);

            if(WisapeEditer.storyData.length > 0 ){
                pageScroll.find("li").removeClass("active").find(".pages-img,.pages-txt").removeClass("active");
                target.after('<li class="active"><span class="count">' + WisapeEditer.selectedStagetIndex++ + '/' + WisapeEditer.storyData.length + '</span>' + WisapeEditer.currentTplData + '</li>');
            } else {
                target.find("ul").html('<li class="active">' + WisapeEditer.currentTplData + '</li>');
            }

            pageScroll.find("li").each(function(i){
                var me = $(this);
                me.find(".count").html((i+1) + "/" +WisapeEditer.storyData.length);
            })

            pageScroll.iScroll( {scrollX: true, scrollY: false, mouseWheel: true, checkDOMChanges: true , preventDefault: false });
        })

        //文本编辑事件
        $("#editorText .backToMain").click(function(){//返回主界面，并保存

            var pagesScroll = $("#pages-scroll li").eq(WisapeEditer.selectedStagetIndex-1);
            var eidtPage = $("#editorText .pages");
            eidtPage.find(".pages-txt").removeAttr("contenteditable").removeClass(preAnimation);
            console.info(preAnimation);
            eidtPage.find(".pages-txt").removeClass(preAnimation);
            console.info(eidtPage.find(".pages-txt").parent().html());
            console.info(eidtPage.html());
            var ret = eidtPage.html();
            console.info("back html:" + ret);
            pagesScroll.html(ret);

            WisapeEditer.storyData[WisapeEditer.selectedStagetIndex-1] = ret;

            console.info("WisapeEditer.storyData:");
            console.info(WisapeEditer.storyData);



            //reset Editer
            if($("#TextEditerOpt").hasClass("active")) {
                $("#TextEditerOpt").click();
            }
            $(".pop-editer-opt i,.pop-editer-animation i").removeClass("active");
            $(".input-href").val("http://");

            WisapeEditer.ShowView('editorText','main');

        })

        $("#TextEditerOpt").click(function(){
            var me = $(this);
            var parent = $("#editorText");
            var target = $("#editorText .pop-editer-opt");
            if(me.hasClass("active")) {
                me.removeClass("active");
                parent.removeClass("pop-active");
                parent.find(".pop-layer").hide();
            } else {
                me.addClass("active");
                parent.addClass("pop-active");
                target.show();
            }
        });

        $("#setFontWeight").click(function(){
            var me = $(this);
            var target = $("#editorText .pages-txt.active");
            if(me.hasClass("active")) {
                me.removeClass("active");
                target.css({"font-weight":"normal"});

            } else {
                me.addClass("active");
                target.css({"font-weight":"bolder"})
            }
            console.info(target.attr("style"));
        });

        $("#setFontStyle").click(function(){
            var me = $(this);
            var target = $("#editorText .pages-txt.active");
            if(me.hasClass("active")) {
                me.removeClass("active");
                target.css({"font-style":"normal"})
            } else {
                me.addClass("active");
                target.css({"font-style":"italic"})
            }
        });

        $("#setFontLink").click(function(){
            var me = $(this);
            var target = $("#editorText .pages-txt.active");
            var tmpText = target.text();
            var mask = $(".ui-mask");
            var link = "";
            var hrefDialog = $(".href-dialog");
            if(me.hasClass("active")) {
                me.removeClass("active");
                hrefDialog.find(".input-href input").val("http://");
                target.html(tmpText);

            } else {
                mask.show();
                hrefDialog.show();
                hrefDialog.find(".btn-cancle").click(function(){
                    mask.hide();
                    hrefDialog.hide();
                });
                hrefDialog.find(".btn-submit").click(function(){
                    link = hrefDialog.find(".input-href input").val();
                    mask.hide();
                    hrefDialog.hide();
                    if(link == "http://" || link == "") return false;
                    me.addClass("active");
                    target.html("<a href='" + link + "'>" + tmpText +"</a>");
                });
            }
        });
        $("#setFontAlign").click(function($event){
            var me = $(this);
            var target = $("#editorText .pages-txt.active");
            if(me.hasClass("icon-align-left")) {
                me.removeClass("icon-align-left").addClass("icon-align-center");
                target.css({"text-align":"center"});
            } else if(me.hasClass("icon-align-center")) {
                me.removeClass("icon-align-center").addClass("icon-align-right");
                target.css({"text-align":"right"});
            } else if(me.hasClass("icon-align-right")) {
                me.removeClass("icon-align-right").addClass("icon-align-left");
                target.css({"text-align":"left"});
            }
        });

        $(".pop-editer-opt .item").click(function(){
            var popLayers = $("#editorText .pop-layer");
            var me = $(this);
            popLayers.hide().parent().find("."+me.data("name")).show();
        });

        $(".backPopLayerOpt").click(function(){
            var popLayers = $("#editorText .pop-layer");
            popLayers.hide().parent().find(".pop-editer-opt").show();
        });

        //动画事件
        var preAnimation = "";

        $(".pop-editer-animation .anim-item").click(function(){

            var selected = $("#editorText .pages-txt.active");
            var me = $(this).find("i"),animtionClassName = "animated " + me.data("animation");
            console.info(animtionClassName);
            if(me.hasClass("active")) {
                me.removeClass("active");
                selected.removeClass(animtionClassName);
                selected.attr({"data-animation" : ""});
            } else {
                me.addClass("active");
                me.parent().siblings().find("i").removeClass("active");
                selected.removeClass(preAnimation).addClass(animtionClassName).attr({"data-animation" : animtionClassName});
                selected.attr({"data-animation" : animtionClassName});
                preAnimation = animtionClassName;
                console.info(selected.parent().html());
            };
            console.info(selected.length);

            console.info(selected.hasClass(animtionClassName));
        })


    },

    //通过接口加载模板列表
    LoadTplList : function(id){
        WisapeEditer.GetNativeData("getStageList", [id],function(data){
            console.info("getStageList");
            console.info(JSON.stringify(data));

            var catScroll = $("#cat-scroll");
            var source = '<ul class="tpl-page-item">'
                +    '{{each list as value i}}'
                +        '<li class="tpl-page-item {{if value.exists}}tpl-exist{{/if}}"  data-id="{{value.id}}" data-exists="{{value.exists}}" data-type="{{value.type}}"  data-name="{{value.temp_name}}">'
                +           '<i style="display: none" class="icon-tags-hot"></i>'
                +           '{{if !value.exists}} <span class="icon-download"></span> {{/if}}<div style="display:none;" class="download-progress-bar"><div class="download-progress-percent"></div></div>'
                +           '<img class="stage-thumb" src="{{value.temp_img}}"  alt="{{value.temp_name}}"/>'
                +           '<div class="tpl-page-item-name">{{value.temp_name}}</div>'
                +        ' </li>'
                +    '{{/each}}'
                + '</ul>';



            var render = template.compile(source);
            var html = render({
                list: JSON.parse(data)
            });

            document.getElementById('cat-scroll').innerHTML = html;
            catScroll.iScroll( { eventPassthrough: true,scrollX: true, scrollY: false, preventDefault: false });
            catScroll.find("li").eq(0).addClass("active");
        });
    },

    //加载,更新主界面stages
    LoadStageList : function(arr,cb){
        console.info("LoadStageList:");
        console.info(arr);
        var retHtml = "";
        for(var i in arr) {
            retHtml += '<li><span class="count">' + (parseInt(i)+1) + '/' + WisapeEditer.storyData.length + '</span>' + arr[i] + '</li>';
        };
        $("#pages-scroll ul").html(retHtml).find("li").eq("0").addClass("active");
        WisapeEditer.selectedStagetIndex = 1;
        cb();

    },

    //更新选中的主界面的stage
    UpdateSelectedStage : function(tplIndex,symbolInex){
        var pagesScroll = $("#pages-scroll li").eq(tplIndex-1);
        var eidtPage = $("#editorText .pages");
        var curPagesTxt = eidtPage.find(".symbol").eq(symbolInex).find(".pages-txt");
        var curAnimation = "";
        //初始化编辑菜单
        eidtPage.find(".opt-val-color").css({"color":curPagesTxt.css("color")});
        if(curPagesTxt.attr("data-animation")) {
            curAnimation = curPagesTxt.attr("data-animation").split(" ")[1];
            console.info('i[data-animation=' + curAnimation + ']');
            console.info(eidtPage.find(".anim-item").eq(0).html());
            eidtPage.find('.anim-item i').eq(0).addClass("active");
        }
         console.info("symbolInex:" + eidtPage.find(".symbol").eq(symbolInex).html());
        eidtPage.html(pagesScroll.html()).find(".symbol").eq(symbolInex).addClass("active").find(".pages-txt").attr({"contenteditable":"true"});
    },

    //生成发布，保存的story
    GenerateStory : function(){},

    ShowView : function(from,to){
        console.info(from + " to " + to);
        $("#" + from).hide();
        $("#" + to).show();
    },

    GetNativeData : function(fn,params,cb){
        cordova.exec(function(retval) {
            console.info(fn + "exec: "  + retval);
            cb(retval);
        }, function(e) {
            console.info(fn + " Error: "+e);
        }, "StoryTemplate", fn, params);

        console.info("fn:" + fn);
        console.info("params:");
        console.info(params);
    }
};

var browser = {
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
};

