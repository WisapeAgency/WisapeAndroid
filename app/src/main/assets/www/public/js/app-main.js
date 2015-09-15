var WisapeEditer = WisapeEditer || {};
WisapeEditer = {

    currentTplData : '<div class="stage-content edit-area pages-img" style="background: url(/mnt/sdcard/wisape/com.wisape.android/data/template/dddddddddddd/img/bg.jpg);background-size: 100% 100%;width:100%;height:100%;"> <div class="symbol" style="z-index: 2;"> <div class="pages-img edit-area"><img data-name="img1" style="" src="/mnt/sdcard/wisape/com.wisape.android/data/template/dddddddddddd/img/icon-earth.png"/> </div> </div> <div class="symbol" style="z-index: 3;"> <div class="pages-txt edit-area" style="margin:70px auto;width:150px;color:#fff;text-align: center;">As c update of the classictranslation corpus, the combination of network technology and language essence </div> </div> </div>',

    selectedStagetIndex : 1,

    stageData : [],

    storyData : [],

    initial : function(){

        //获取模板分类
        WisapeEditer.GetNativeData("getStageCategory", [],function(data){
            var menuScroll = $("#menu-scroll");
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

        });

    },

    event : function(){

        //模板分类事件
        var menuScroll = $("#menu-scroll");
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
                pageScroll.find("ul li.active").html(data);
                WisapeEditer.storyData[WisapeEditer.selectedStagetIndex-1] = data;
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

        pageScroll.delegate("li.active .pages-img ","click",function(event){
            var _me = $(this);
            if(!_me.hasClass("active")) {
                console.info(pageScroll.find(".edit-area").length);
                pageScroll.find(".edit-area").removeClass("active");
                _me.addClass("active");
                if(_me.find(".ico-acitve").length == 0) _me.append('<i class="ico-acitve"></i>');
                return false;
            }
            WisapeEditer.ShowView('main','editorImage');
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


            if(pageScrollLi.length > 0 ){
                pageScroll.find("li").removeClass("active").find(".pages-img,.pages-txt").removeClass("active");
                target.after('<li class="active">' + WisapeEditer.currentTplData + '</li>');
            } else {
                target.find("ul").html('<li class="active">' + WisapeEditer.currentTplData + '</li>');
            }

            WisapeEditer.selectedStagetIndex++;

            pageScroll.iScroll( {scrollX: true, scrollY: false, preventDefault: false });
        })

        //文本编辑事件
        $("#editorText .backToMain").click(function(){//返回主界面，并保存

            var pagesScroll = $("#pages-scroll li").eq(WisapeEditer.selectedStagetIndex-1);
            var eidtPage = $("#editorText .pages");
            eidtPage.find(".pages-txt").removeAttr("contenteditable");
            pagesScroll.html( eidtPage.html());

            WisapeEditer.ShowView('editorText','main');

            if($("#TextEditerOpt").hasClass("active")) {
                $("#TextEditerOpt").click();
            }
            $(".pop-editer-opt i,.pop-editer-animation i").removeClass("active");
            $(".input-href").val("http://");

        })

        $("#TextEditerOpt").click(function(){
            var me = $(this);
            var parent = $("#editorText");
            var target = $("#editorText .pop-editer-opt");
            if(me.hasClass("active")) {
                me.removeClass("active");
                parent.removeClass("pop-active");
                target.hide();
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
        $(".pop-editer-animation .anim-item").click(function(){
            var me = $(this).find("i");
            if(me.hasClass("active")) {
                me.removeClass("active")
            } else {
                me.addClass("active");
                me.parent().siblings().find("i").removeClass("active");
            }
        })
    },

    //加载模板列表
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
                +           '<img class="stage-thumb" src="{{value.temp_img_local}}"  alt="{{value.temp_name}}"/>'
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

    //加载,更新stages
    LoadStage : function(){

    },

    //更新选中的主界面的stage
    UpdateSelectedStage : function(tplIndex,symbolInex){
        var pagesScroll = $("#pages-scroll li").eq(tplIndex-1);
        var eidtPage = $("#editorText .pages");
        var curPagesTxt = eidtPage.find(".symbol").eq(symbolInex).find(".pages-txt");
        console.info("symbolInex:" + eidtPage.find(".symbol").eq(symbolInex).html());
        eidtPage.html(pagesScroll.html()).find(".symbol").eq(symbolInex).addClass("active").find(".pages-txt").attr({"contenteditable":"true"});

        //初始化编辑菜单
        eidtPage.find(".opt-val-color").css({"color":curPagesTxt.css("color")});
    },


    //加载stagelist的stages
    LoadStageList : function(){},

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

