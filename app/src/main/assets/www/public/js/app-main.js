var WisapeEditer = WisapeEditer || {};
WisapeEditer = {

    currentTplData: '',

    fontPath : "",

    selectedStagetIndex: 1,

    stageData: [],

    storyData: [],

    config : {},

    loggerStr : "",

    pagesIScroll : null,

    catIScroll : null,

    saveTimmer : true,

    logger : function(name,str){
        var ret = "\n" + CurentTime() + "WisapeEditer.logger " + name + " :" + str  + "\n";
        this.loggerStr += ret;
    },

    Init: function () {
        var menuScroll = $("#menu-scroll"),
            catScroll = $("#cat-scroll"),
            pageScroll = $("#pages-scroll");

        setTimeout(function(){
            console.info("调用日志接口")
            cordova.exec(function(retval) {
                WisapeEditer.loggerStr = "";
            }, function(e) {
                console("log Error: "+e);
            }, "Logger", "log", [WisapeEditer.loggerStr]);

            setTimeout(arguments.callee, 10000);

        }, 10000);

        //通过getStageCategory接口获取模板分类
        WisapeEditer.GetNativeData("getStageCategory", [], function (data) {
            console.info("getStageCategory");
            console.info(JSON.stringify(data));
            WisapeEditer.logger("过getStageCategory接口获取模板分类",JSON.stringify(data));
            var source = '<ul class="tabcon-item">'
                + '{{each list as value i}}'
                + '<li class="tabnav-item"  data-id="{{value.id}}"> {{value.name}} </li>'
                + '{{/each}}'
                + '</ul>';

            var render = template.compile(source);
            var htmlStr = render({
                list: data
            });

            $("#menu-scroll").html(htmlStr);
            set_wrap_width($("#menu-scroll"));
            new iScroll('menu-scroll',{
                scrollX: true,
                scrollY: false,
                click : false,
                hScrollbar :false,
                lockDirection:true
            });
            $("#menu-scroll").find("li").eq(0).addClass("active");



            //获取默认模板模板列表
            console.info("获取默认模板模板列表:" + parseInt(menuScroll.find("li").data("id")));
            WisapeEditer.logger("获取默认模板模板列表 分类id",parseInt(menuScroll.find("li").data("id")));
            WisapeEditer.LoadTplList(parseInt(menuScroll.find("li").data("id")), function () {
                console.info(catScroll.find("li").eq(0).html());
                var _me = catScroll.find("li").eq(0), localTplPath = _me.find(".stage-thumb").attr("src").split("/thumb.jpg")[0] + "/stage.html";
                _me.addClass("active");
                console.info("path:" + localTplPath);
            });
        });

    },

    LoadDefaultData : function(data){

        var firstPageData = data;
        console.info("firstPageData:" + firstPageData);
        WisapeEditer.logger("firstPageData:",firstPageData);
        //获取pages数据
        WisapeEditer.GetNativeData("getContent", [], function (data) {

            WisapeEditer.Init();
            WisapeEditer.Event();

            console.info("getContent");
            console.info(JSON.stringify(data));
            WisapeEditer.logger("getContent:",JSON.stringify(data));
            console.info(data == null);
            if (data == null) {//新建stroy 直接返回
                WisapeEditer.logger("新建stroy:","");
                WisapeEditer.currentTplData = firstPageData;
                WisapeEditer.storyData.push(firstPageData);
                $("#pages-scroll").find("ul").html('<li class="active"><span class="count">1/1</span>' + firstPageData + "</li>");
                $(".loading").hide();
                setPagesScroll();
                return false;
            };
            //编辑story逻辑
            console.info("编辑story逻辑");
            WisapeEditer.logger("编辑story逻辑:","");
            var ret = [];
            $('<div id="tpmHtml" style="display: none">' + data + '</div>').insertBefore("body");
            $("#tpmHtml").find(".m-img").each(function () {
                ret.push($(this).html());
            });

            console.info("ret arr");
            console.info(ret.length);

            console.info($("#tpmHtml").length);
            console.info($("#tpmHtml").find(".m-img").length);
            console.info(ret);
            WisapeEditer.storyData = ret;
            WisapeEditer.currentTplData = ret[0];
            WisapeEditer.LoadStageList(ret, function () {
                console.info("loadStageList succ");
                $(".loading").hide();
            });
            setTimeout(function () {
                $("#tpmHtml").remove();
            }, 2000);
        });
    },

    Event: function () {

        var menuScroll = $("#menu-scroll"),
            pageScroll = $("#pages-scroll"),
            catScroll = $("#cat-scroll"),
            AddNewStage = $("#AddNewStage");

        //获取字体接口数据
        $("#pop-editer-font").click(function () {
            var me = $(this);
            if(me.hasClass("disable")) return;
            me.addClass("disable");
            WisapeEditer.GetNativeData("getFonts", [], function (data) {
                me.removeClass("disable");
                var curFamily = $("#editorText").find(".edit-area.active").css("font-family").split(",")[0];
                console.info("fonts:");
                console.info(JSON.stringify(data.filePath));
                console.info(data.fonts);
                WisapeEditer.logger("getFonts:",JSON.stringify(data.filePath));
                var sourceFont = ''
                    + '{{each listFont as value i}}'
                    + '<div class="item {{if !value.downloaded}}download{{/if}}" data-fontname="{{value.name}}" ><span class="opt-img"><img src="{{value.preview_img_local}}" alt=""/></span> <span class="opt-right"><i class="icon-correct" ></i><i class="icon-download" ></i><div class="download-progress-bar"><div class="download-progress-percent" style="width:0%;"></div></div></span> </div>'
                    + '{{/each}}';

                var render = template.compile(sourceFont);
                var htmlFont = render({
                    listFont: JSON.parse(data.fonts)
                });

                console.info(htmlFont);
                console.info("font path:" + data.filePath);
                WisapeEditer.fontPath = data.filePath;
                $(".pop-editer-font .opts").html(htmlFont);
                $(".pop-editer-font .opts .item").each(function(){
                    console.info("curFamily" + curFamily);
                    console.info("data-fontname" + $(this).attr("data-fontname"));
                    console.info("xxx:" + $(this).hasClass("download"));
                    if(!$(this).hasClass("download") && (curFamily == $(this).attr("data-fontname"))) {
                        $(this).addClass("selected");
                    }
                })
            })
        });

        $(".pop-editer-font").delegate(".item", "click", function () {
            console.info("set-font");
            var me = $(this), fontname = me.attr("data-fontname"), target = $("#editorText .pages-txt.active"),isDownloading = (me.find(".download-progress-bar").css("display") == "block");
            if (!me.hasClass("download") && !isDownloading) {
                console.info(WisapeEditer.fontPath);
                loadjscssfile(WisapeEditer.fontPath + "?"  +Date.parse(new Date()), "css");
                me.addClass("selected").siblings().removeClass("selected");
                target.css({"font-family": fontname});
                console.info("add font:" + target.css("font-family"));
                console.info("font-box:" + me.parent().html());
                $(".opt-font-val").html(fontname);
                return false
            };
            console.info("font name:" + fontname);
            !isDownloading && WisapeEditer.GetNativeData("downloadFont", [fontname], function (data) {

            });
        })


        //预览
        $("#storyPreview").click(function () {
            var me = $(this);
            if(me.hasClass("disable")) return;
            me.addClass("disable");
            WisapeEditer.logger("预览",WisapeEditer.storyData);
            $(".loading").show();
            var retHtml = '<div class="p-index main" id="con">', retImg = [];
            for (var i = 0; i < WisapeEditer.storyData.length; i++) {
                retHtml += '<section class="m-page hide pages-item" > <div class="m-img" >' + WisapeEditer.storyData[i].replace("file://","") + '</div> </section>';
            }
            //retHtml += '<section class="u-arrow"><img src="file:///android_asset/www/public/img/btn01_arrow.png" /></section></div>';
            pageScroll.find(".pages-img").each(function () {
                var me = $(this);
                if (me.hasClass("pages-img-bg")) {
                    retImg.push((me.css("background-image").split("url(")[1].split(")")[0]+"").replace("file://",""));
                } else {
                    console.info(me.find("img").attr("src"));
                    retImg.push(me.find("img").attr("src").replace("file://",""));
                }
            });
            WisapeEditer.GetNativeData("preview", [retImg[0],retHtml, retImg], function () {
                setTimeout(function(){
                    me.removeClass("disable");
                },1000);
                console.info("preview done!!!");
                $(".loading").hide();
            })
        });

        //保存
        $("#storySave").click(function () {

            if($("#pages-scroll").hasClass("pages-big")  && $("#main").css("display") == "block") {
                $("#toggleCat").click();
                return false;
            }

            WisapeEditer.SaveStory();
        });



        //模板分类事件
        console.info("menu-scroll length:" + menuScroll.length);
        menuScroll.delegate("li", "click", function () {
            var _me = $(this);
            console.info("menu-scroll click");
            console.info("id:" + _me.data("id"));
            _me.addClass("active").siblings().removeClass("active");
            WisapeEditer.LoadTplList(parseInt(_me.data("id")), null);
        });

        //显示隐藏分类
        $("#toggleCat").click(function () {
            var tplPageCat = $(".tpl-page-cat");
            $(".tpl-page-cat").toggle();
            $(this).toggleClass("active");
            console.info(tplPageCat.hasClass("pages-big"));
            if(pageScroll.hasClass("pages-big")) {
                pageScroll.removeClass("pages-big");
            } else {
                pageScroll.addClass("pages-big");
            }
            setPagesScroll();

        });

        //管理stage列表
        $("#manageStageList").click(function () {
            console.info("manageStageList:");
            WisapeEditer.logger("管理stage列表","");
            console.info(typeof WisapeEditer.storyData);
            $("#pageScroll li").find(".ico-acitve").remove();
            $("#pageScroll li").find(".count").remove();
            var retHtml = "";
            console.info($(document).width());
            for (var i in WisapeEditer.storyData) {
                retHtml += '<li class="tpl-page-item" draggable="false"><span class="drag-handle">☰</span><i class="icon-correct"></i>' + WisapeEditer.storyData[i] + '</li>';
            };
            $("#storyDragBox").html(retHtml);
            setTplLayout(0.3);

            for (var k = 0; k < $("#storyDragBox li").length; k++) {

                (function (k) {
                    var me = $("#storyDragBox li").eq(k)[0];
                    console.info("press in");
                    //console.info($(me).html());
                    var hammerLi = new Hammer(me);
                    //hammerLi.on("press", function(e){
                    //    console.log(e.type);
                    //    $(me).find(".drag-handle").addClass("dragable");
                    //    console.info($(me).html());
                    //    $(me).trigger("dragstart")
                    //
                    //});

                    hammerLi.on("tap", function (e) {
                        console.log(e.type);
                        $(me).toggleClass("selected");
                    });

                })(k);

            }

            WisapeEditer.ShowView('main', 'storyStageList');
        });

        //从管理stage列表页面返回，并保存
        $("#storyStageList .icon-arrow-left").click(function () {
            var retHtml = [];
            $("#storyDragBox").find(".drag-handle,.icon-correct,.count").remove();
            $("#storyDragBox li").each(function () {
                retHtml.push($(this).removeAttr("style").html());
            });
            console.info("storyStageList:");
            console.info(retHtml);
            WisapeEditer.storyData = retHtml;//覆盖更新storyData
            WisapeEditer.logger("管理stage列表返回","");
            WisapeEditer.logger("WisapeEditer.currentTplData",WisapeEditer.currentTplData);
            WisapeEditer.logger("WisapeEditer.storyData",WisapeEditer.storyData);
            WisapeEditer.LoadStageList(retHtml, function () {//重新加载主界面的stage列表
                WisapeEditer.ShowView('storyStageList', 'main')
            })

        })


        //模板列表事件
        var catClickTimmer = true;
        catScroll.delegate("li", "click", function () {

            if(!catClickTimmer) return;
            catClickTimmer = false;
            setTimeout(function(){
                catClickTimmer = true;
            },500);

            var _me = $(this),localTplPath = _me.find(".stage-thumb").attr("src").split("/thumb.jpg")[0] + "/stage.html";
            _me.addClass("active");
            _me.addClass("active").siblings().removeClass("active");
            console.info("page click:");
            console.info(!_me.hasClass("tpl-exist"));
            console.info(catClickTimmer);
            if (!_me.hasClass("tpl-exist")) {
                if(_me.find(".download-progress-bar:visible").length) return;
                console.info(".download-progress-bar:visible " + _me.find(".download-progress-bar:visible").length);
                console.info("down");
                WisapeEditer.GetNativeData("start", [parseInt(_me.data("id")), parseInt(_me.data("type"))], function (data) {
                    _me.hasClass("tpl-exist");
                });
            } else {
                console.info("read:");
                WisapeEditer.GetNativeData("read", [localTplPath], function (data) {
                    console.info("read data:");
                    console.info(data);
                    if(data == null || data.length == 0) return;
                    WisapeEditer.currentTplData = data;
                    pageScroll.find("ul li.active").html('<span class="count">' + WisapeEditer.selectedStagetIndex + '/' + WisapeEditer.storyData.length + '</span>' + data);
                    WisapeEditer.storyData[WisapeEditer.selectedStagetIndex - 1] = data;
                    pageScroll.find(".pages-txt").removeClass("edit-area-active");
                    pageScroll.find("li.active .pages-txt").addClass("edit-area-active");
                    console.info("WisapeEditer.storyData:");
                    console.info(WisapeEditer.storyData);
                });
            };
            setTplLayout(0.6);
        });

        //stage列表事件

        var pageClickTimmer = true;
        pageScroll.delegate("li", "click", function () {

            pageClickTimmer = false;
            setTimeout(function(){
                pageClickTimmer = true;
            },500);

            var _me = $(this);
            WisapeEditer.selectedStagetIndex = _me.index() + 1;
            _me.addClass("active").siblings().removeClass("active").find(".pages-img,.pages-txt").removeClass("active");
            pageScroll.find(".pages-txt").removeClass("edit-area-active");
            pageScroll.find("li.active .pages-txt").addClass("edit-area-active");
        });


        var pagesImgTimmer = true;
        pageScroll.delegate("li.active .pages-img", "click", function (event) {
            if(!pageClickTimmer) return;
            pageClickTimmer = false;
            setTimeout(function(){
                pageClickTimmer = true;
            },500);

            var _me = $(this),wh = [];
            console.info(_me.parent().html());

            if (!_me.hasClass("active")) {
                pageScroll.find(".edit-area").removeClass("active");
                _me.addClass("active");
                if (_me.children(".ico-acitve").length == 0) _me.append('<i class="ico-acitve"></i>');
                return false;
            };
            if (_me.hasClass("pages-img-bg")) {
                wh = [800,1000]
            } else {
                wh = [parseInt(_me.find("img").width()),parseInt(_me.find("img").height())];
            }
            if(!pagesImgTimmer) return;
            pagesImgTimmer = false;
            setTimeout(function(){
                pagesImgTimmer = true;
            },500);
            console.info("wh:" + wh);
            WisapeEditer.logger("调用图片接口","");
            cordova.exec(function (retval) {
                console.info("PhotoSelector:" + retval);
                console.info("_me:" + _me.html());
                console.info("_me.hasClass:" + _me.hasClass("pages-img-bg"));
                var imgurl = retval.replace("file://","");
                console.info(imgurl);
                if (_me.hasClass("pages-img-bg")) {
                    _me.css({"background-image": "url(" + imgurl + ")"})
                } else {
                    _me.find("img").attr({"src": imgurl})
                }
                WisapeEditer.logger("调用图片接口返回",imgurl);
                WisapeEditer.storyData[WisapeEditer.selectedStagetIndex - 1] = pageScroll.find("li.active").html();

                console.info("WisapeEditer.storyData:");
                console.info(WisapeEditer.storyData);
            }, function (e) {
                //alert("Error: " + e);
            }, "PhotoSelector", "execute", wh);
            event.stopPropagation();

        });

        pageScroll.delegate("li.active .pages-txt", "click", function (event) {
            if(!pageClickTimmer) return;

            pageClickTimmer = false;
            setTimeout(function(){
                pageClickTimmer = true;
            },500);
            var _me = $(this);
            if (!_me.hasClass("active")) {
                console.info(pageScroll.find(".edit-area").length);
                pageScroll.find(".edit-area").removeClass("active");
                _me.addClass("active");
                return false;
            };
            console.info("pages-txt click:" + _me.parents(".stage-content").html());
            console.info("index:" + _me.parents(".stage-content").find(".edit-area.active"));
            WisapeEditer.UpdateSelectedStage(WisapeEditer.selectedStagetIndex);
            WisapeEditer.ShowView('main', 'editorText');
            setTimeout(function(){
                $("#editorText .pages-txt.active").click();
            },10);
            event.stopPropagation();
        });

        //新建stage事件
        AddNewStage.click(function () {

            WisapeEditer.logger("新建stage事件 WisapeEditer.currentTplData",WisapeEditer.currentTplData);
            var target = pageScroll.find("li.active");
            var tipDialog = $(".tip-dialog");

            if(WisapeEditer.storyData.length == 15 ) {
                Dialog.show(tipDialog);
                tipDialog.find(".btn-cancle").unbind("click").bind("click",function () {
                    Dialog.hide(tipDialog);
                });
                return ;
            }
            WisapeEditer.storyData.push(WisapeEditer.currentTplData);

            console.info("WisapeEditer.storyData:");
            console.info(WisapeEditer.storyData);

            //if (WisapeEditer.storyData.length > 0) {
            //    pageScroll.find("li").removeClass("active").find(".pages-img,.pages-txt").removeClass("active");
            //    target.after('<li class="active"><span class="count">' + WisapeEditer.selectedStagetIndex++ + '/' + WisapeEditer.storyData.length + '</span>' + WisapeEditer.currentTplData + '</li>');
            //} else {
            //    target.find("ul").html('<li class="active">' + WisapeEditer.currentTplData + '</li>');
            //}


            if(WisapeEditer.storyData.length == 1) {
                pageScroll.find("ul").html('<li class="active"><span class="count">1/1</span>' + WisapeEditer.currentTplData + '</li>');
                return ;
            }

            pageScroll.find("li").removeClass("active").find(".pages-img,.pages-txt").removeClass("active");
            target.after('<li class="active"><span class="count">' + WisapeEditer.selectedStagetIndex++ + '/' + WisapeEditer.storyData.length + '</span>' + WisapeEditer.currentTplData + '</li>');
            setPagesScroll();
            WisapeEditer.pagesIScroll.scrollToElement('li.active', 1000);
            pageScroll.find("li").each(function (i) {
                var me = $(this);
                me.find(".count").html((i + 1) + "/" + WisapeEditer.storyData.length);
            });
            pageScroll.find(".pages-txt").removeClass("edit-area-active");
            pageScroll.find("li.active .pages-txt").addClass("edit-area-active");
        })

        //文本编辑事件
        $("#editorText .backToMain").click(function () {//返回主界面，并保存

            preAnimation = "";

            var pagesScroll = $("#pages-scroll li").eq(WisapeEditer.selectedStagetIndex - 1);
            var editPage = $("#editorText .pages");
            editPage.find(".pages-txt").each(function(){
                var _this = $(this);
                _this.removeClass(_this.data("animation"));
            })
            console.info(editPage.find(".pages-txt").parent().html());
            console.info(editPage.html());
            var ret = editPage.html();
            console.info("back html:" + ret);
            pagesScroll.html(ret);

            WisapeEditer.storyData[WisapeEditer.selectedStagetIndex - 1] = ret;
            $('.J-textarea-word').focus();
            cordova.exec(function(retval) {
                console.info("hide keyboard");
            }, function(e) {
            }, "Keyboard", "hide", []);
            WisapeEditer.ShowView('editorText', 'main');
            pageScroll.find(".pages-txt").removeClass("edit-area-active");
            pageScroll.find("li.active .pages-txt").addClass("edit-area-active");
            setPagesScroll();
        })

        $("#TextEditerOpt").click(function () {
            var me = $(this);
            var parent = $("#editorText");
            var target = $("#editorText .pop-editer-opt");
            $('.J-textarea-word').focus();
            cordova.exec(function(retval) {
                console.info("hide keyboard");
            }, function(e) {
            }, "Keyboard", "hide", []);
            if (me.hasClass("active")) {
                me.removeClass("active");
                parent.removeClass("pop-active");
                parent.find(".pop-layer").hide();
            } else {
                me.addClass("active");
                $(".pop-editer-text").hide();
                parent.addClass("pop-active");
                target.show();
            }

        });


        $("#setFontWeight").click(function () {
            var me = $(this);
            var target = $("#editorText .pages-txt.active");
            if (me.hasClass("active")) {
                me.removeClass("active");
                target.css({"font-weight": "normal"});

            } else {
                me.addClass("active");
                target.css({"font-weight": "bolder"})
            }
            console.info(target.attr("style"));
        });

        $("#setFontStyle").click(function () {
            var me = $(this);
            var target = $("#editorText .pages-txt.active");
            if (me.hasClass("active")) {
                me.removeClass("active");
                target.css({"font-style": "normal"})
            } else {
                me.addClass("active");
                target.css({"font-style": "italic"})
            }
        });


        //文字编辑
        $("#editorText").delegate(".pages-txt.active","click",function(){
            console.info("#editorText .pages-txt.active click");
            if($("#TextEditerOpt").hasClass("active"))  $("#TextEditerOpt").click();
            $(".pop-editer-text").show();
            $(".J-textarea-word").val($.trim($(this).html().replace(/<br>/g,'\n')));
            $(".J-textarea-word")[0].select();
            $(".J-textarea-word")[0].focus();
            cordova.exec(function(retval) {
                console.info("open keyboard");

            }, function(e) {
            }, "Keyboard", "show", []);

            wordEditResize();

        })
        $(".J-btn-text-done").click(function(){
            $("#editorText .pages-txt.active").html($.trim($(".textarea-word").val().replace(/\n/g,'<br>')));
            cordova.exec(function(retval) {
                console.info("open keyboard");

            }, function(e) {
            }, "Keyboard", "show", []);
        });

        var observe;
        if (window.attachEvent) {
            observe = function (element, event, handler) {
                element.attachEvent('on'+event, handler);
            };
        }
        else {
            observe = function (element, event, handler) {
                element.addEventListener(event, handler, false);
            };
        };
        wordEditInit();
        function wordEditInit () {
            console.info("wordEditInit");
            var text = $('.J-textarea-word')[0];
            /* 0-timeout to get the already changed text */
            function delayedResize () {
                window.setTimeout(wordEditResize, 0);
            }
            observe(text, 'change',  wordEditResize);
            observe(text, 'cut',     delayedResize);
            observe(text, 'paste',   delayedResize);
            observe(text, 'drop',    delayedResize);
            observe(text, 'keydown', delayedResize);
            wordEditResize();
        };
        function wordEditResize(){
            var text = $('.J-textarea-word')[0];
            console.info("wordEditResize()");
            text.style.height = 'auto';
            text.style.height = text.scrollHeight+'px';
        }

        $(".J-font-resize .opt-right").delegate("span","click",function(){
            var target = $("#editorText .pages-txt.active");
            var htmlFontSize = parseInt($("html").css("font-size"));
            var fontSizeLim = [parseFloat((30/1080*16).toFixed(2)),parseFloat((300/1080*16).toFixed(2))];
            var me = $(this);
            var curFontSize = parseFloat(target.css("fontSize"))/htmlFontSize;
            console.info(target.css("fontSize"));
            console.info(curFontSize);
            if(me.hasClass("J-font-reduce")) {
                console.info(curFontSize <= fontSizeLim[0])
                if(curFontSize <= fontSizeLim[0]) {
                    console.info("disable");
                    me.addClass("disable") ;
                    return;
                }
                curFontSize -= 0.1;

            }
            if(me.hasClass("J-font-add")){
                if(curFontSize >= fontSizeLim[1]) {
                    console.info("disable");
                    me.addClass("disable") ;
                    return ;
                }
                console.info("add");
                curFontSize += 0.1;

            }
            if(curFontSize > fontSizeLim[0] && curFontSize < fontSizeLim[1]){
                $(".J-font-resize .opt-right span").removeClass("disable");
            }

            console.info(fontSizeLim);
            target.css({"font-size": curFontSize + "rem"});
        });

        $("#setFontLink").click(function () {
            var me = $(this);
            var target = $("#editorText .pages-txt.active");
            var mask = $(".ui-mask");
            var link = "";
            var hrefDialog = $(".href-dialog");
            if (me.hasClass("active")) {
                me.removeClass("active");
                hrefDialog.find(".input-href input").val("http://");
                target.removeAttr("data-href").removeClass("font-link");
            } else {
                Dialog.show(hrefDialog);
                hrefDialog.find(".btn-cancle").unbind("click").bind("click",function () {
                    Dialog.hide(hrefDialog);
                });
                hrefDialog.find(".btn-submit").unbind("click").bind("click",function () {
                    link = hrefDialog.find(".input-href input").val();
                    console.info(link);
                    mask.hide();
                    Dialog.hide(hrefDialog);
                    if (link == "http://" || link == "") return false;
                    me.addClass("active");
                    target.attr({"data-href" : link }).addClass("font-link");
                });
            }
            console.info(target.attr("data-href"));
        });
        $("#setFontAlign").click(function () {
            var me = $(this);
            var target = $("#editorText .pages-txt.active");
            if (me.hasClass("icon-align-left")) {
                me.removeClass("icon-align-left").addClass("icon-align-center");
                target.css({"text-align": "center"});
            } else if (me.hasClass("icon-align-center")) {
                me.removeClass("icon-align-center").addClass("icon-align-right");
                target.css({"text-align": "right"});
            } else if (me.hasClass("icon-align-right")) {
                me.removeClass("icon-align-right").addClass("icon-align-left");
                target.css({"text-align": "left"});
            }
        });

        $(".pop-editer-opt .item").click(function () {
            var popLayers = $("#editorText .pop-layer");
            var me = $(this);
            if(me.hasClass("J-font-resize")) return;
            popLayers.hide().parent().find("." + me.data("name")).show();
        });

        $(".backPopLayerOpt").click(function () {
            var popLayers = $("#editorText .pop-layer");
            popLayers.hide().parent().find(".pop-editer-opt").show();
        });

        //动画事件
        var preAnimation = "";
        $(".pop-editer-animation .anim-item").click(function () {

            var selected = $("#editorText .pages-txt.active");
            var me = $(this).find("i"), animtionClassName = "animated " + me.data("animation");
            console.info(animtionClassName);
            if (me.hasClass("active")) {
                me.removeClass("active");
                selected.removeClass(animtionClassName);
                $(".opt-animation-val i").hide();
                selected.attr({"data-animation": ""});
            } else {
                me.addClass("active");
                me.parent().siblings().find("i").removeClass("active");
                selected.removeClass(preAnimation).addClass(animtionClassName).attr({"data-animation": animtionClassName});
                selected.attr({"data-animation": animtionClassName});
                preAnimation = animtionClassName;
                $(".opt-animation-val i").attr({"class":$('i[data-animation=' + me.data("animation") + ']').attr("class")}).show();
                console.info(selected.parent().html());
            };
            console.info(selected.length);
            console.info(selected.hasClass(animtionClassName));
        })
    },

    SaveStory : function(){
        if(!WisapeEditer.saveTimmer) return;
        WisapeEditer.saveTimmer = false;
        setTimeout(function(){
            WisapeEditer.saveTimmer = true;
        },500);
        var mask = $(".mask");
        var saveDialog = $(".save-dialog");
        var pageScroll = $("#pages-scroll");
        var retHtml = '', retImg = [];
        Dialog.show(saveDialog);
        saveDialog.find(".btn-cancle").unbind("click").bind("click",function () {
            Dialog.hide(saveDialog);
        });
        saveDialog.find(".btn-saveno").unbind("click").bind("click",function () {
            WisapeEditer.GetNativeData("back", [], null);
            Dialog.hide(saveDialog);
        });
        saveDialog.find(".btn-save").unbind("click").bind("click",function () {
            mask.hide();
            Dialog.hide(saveDialog);
            $(".loading").show();
            for (var i = 0; i < WisapeEditer.storyData.length; i++) {
                retHtml += '<section class="m-page hide pages-item" > <div class="m-img" >' + WisapeEditer.storyData[i].replace("file://","") + '</div> </section>';
            }
            pageScroll.find(".pages-img").each(function () {
                var me = $(this);
                if (me.hasClass("pages-img-bg")) {
                    retImg.push((me.css("background-image").split("url(")[1].split(")")[0]+"").replace("file://",""));
                } else {
                    retImg.push(me.find("img").attr("src").replace("file://",""));
                }
            });
            WisapeEditer.logger("保存",WisapeEditer.storyData);
            WisapeEditer.GetNativeData("save", [retImg[0],retHtml, retImg], function () {
            })
        });
    },

    ImSave : function(){
        var pageScroll = $("#pages-scroll");
        var retHtml = '', retImg = [];
        for (var i = 0; i < WisapeEditer.storyData.length; i++) {
            retHtml += '<section class="m-page hide pages-item" > <div class="m-img" >' + WisapeEditer.storyData[i].replace("file://","") + '</div> </section>';
        }
        pageScroll.find(".pages-img").each(function () {
            var me = $(this);
            if (me.hasClass("pages-img-bg")) {
                retImg.push((me.css("background-image").split("url(")[1].split(")")[0]+"").replace("file://",""));
            } else {
                retImg.push(me.find("img").attr("src").replace("file://",""));
            }
        });
        WisapeEditer.logger("销毁保存",WisapeEditer.storyData);
        WisapeEditer.GetNativeData("isave", [retImg[0],retHtml, retImg], function () {
        })
    },

    //通过接口加载模板列表
    LoadTplList: function (id, cb) {
        WisapeEditer.GetNativeData("getStageList", [id], function (data) {
            console.info("getStageList");
            console.info(JSON.stringify(data));

            WisapeEditer.logger("通过接口加载模板列表",JSON.stringify(data));

            var catScroll = $("#cat-scroll");
            var source = '<ul class="tpl-page-item">'
                + '{{each list as value i}}'
                + '<li class="tpl-page-item {{if value.rec_status == 1}}tpl-exist{{/if}}"  data-id="{{value.id}}" data-exists="{{value.rec_status}}" data-type="{{value.type}}"  data-name="{{value.temp_name}}">'
                + '<i {{if value.order_type == "N" }} style="display: block" class="icon-tags-new" {{/if}} {{if value.order_type == "H" }} style="display: block" class="icon-tags-hot" {{/if}} ></i>'
                + '{{if value.rec_status == 0}} <span class="icon-download"></span> {{/if}}<div style="display:none;" class="download-progress-bar"><div class="download-progress-percent"></div></div>'
                + '<img class="stage-thumb" src="{{value.temp_img_local}}"  alt="{{value.temp_name}}"/>'
                + ' </li>'
                + '{{/each}}'
                + '</ul>';


            var render = template.compile(source);
            var html = render({
                list: JSON.parse(data)
            });

            document.getElementById('cat-scroll').innerHTML = html;
            set_wrap_width(catScroll);
            if(WisapeEditer.catIScroll) WisapeEditer.catIScroll.destroy();
            WisapeEditer.catIScroll = new iScroll('cat-scroll',{
                scrollX: true,
                scrollY: false,
                click : false,
                hScrollbar :false,
                lockDirection:true
            });
            catScroll.find("li").eq(0).addClass("active");
            if (cb !== null)cb();
        });
    },

    //加载,更新主界面stages
    LoadStageList: function (arr, cb) {
        console.info("LoadStageList:");
        console.info(arr);
        var retHtml = "";
        for (var i in arr) {
            retHtml += '<li><span class="count">' + (parseInt(i) + 1) + '/' + WisapeEditer.storyData.length + '</span>' + arr[i] + '</li>';
        };
        $("#pages-scroll ul").html(retHtml).find("li").eq("0").addClass("active");

        $("#pages-scroll ul").find("li").eq(0).siblings().find(".pages-txt").removeClass("edit-area-active");
        $("#pages-scroll ul").find("li").eq(0).find(".pages-txt").addClass("edit-area-active");
        WisapeEditer.selectedStagetIndex = 1;
        console.info($("#pages-scroll ul").html());
        setTplLayout(0.6);
        if (cb !== null)cb();
        setPagesScroll();
    },

    //更新选中的主界面的stage
    UpdateSelectedStage: function (tplIndex) {

        var curPage = $("#pages-scroll li").eq(tplIndex - 1),
            editPage = $("#editorText .pages");

        //替换html
        editPage.html(curPage.html());
        console.info("contenteditable:" + editPage.html());
        var curTxt = editPage.find(".edit-area.active"),
            curAnimation = "",
            curColor = rgb2hex(curTxt.css("color")),
            curFamily = curTxt.css("font-family").split(",")[0],
            fontWight = curTxt.css("font-weight"),
            fontStyle = curTxt.css("font-style"),
            fontAlign = curTxt.css("text-align");
        console.info(curColor);
        console.info(curFamily);
        console.info(fontWight);
        console.info(fontStyle);
        console.info(fontAlign);
        //初始化编辑菜单

        if (curTxt.attr("data-animation")) {//动画
            curAnimation = curTxt.attr("data-animation").split(" ")[1];
            console.info('i[data-animation=' + curAnimation + ']');
            console.info($('i[data-animation=' + curAnimation + ']').attr("class"));
            $('i[data-animation=' + curAnimation + ']').addClass("active").parent().siblings().find("i").removeClass("active");
            $(".opt-animation-val i").attr({"class":$('i[data-animation=' + curAnimation + ']').attr("class")}).show();
        } else {
            console.info("animation none");
            $(".opt-animation-val i").hide();
            $(".pop-editer-animation i").removeClass("active");
        }

        if ($("#TextEditerOpt").hasClass("active")) {//弹出层
            $("#TextEditerOpt").click();
        }

        if(curTxt.attr("data-href") != undefined && curTxt.attr("data-href") != "") {//链接
            $(".input-href").val(curTxt.attr("data-href"));
            $("#setFontLink").addClass("active")
        } else {
            $("#setFontLink").removeClass("active")
        }

        console.info(">500:" + (parseInt(fontWight) >500));
        if(fontWight == "bold" || fontWight == "bolder" || (parseInt(fontWight) >500)){
            $("#setFontWeight").addClass("active");
        } else {
            $("#setFontWeight").removeClass("active");
        }

        if(fontStyle == "normal") {
            $("#setFontStyle").removeClass("active");
        } else {
            $("#setFontStyle").addClass("active");
        }

        $("#setFontAlign").attr({"css":"icon-align-" + fontAlign});

        $(".opt-font-val").html(curFamily);//字体
        $(".pop-editer-font .item").removeClass("selected");
        if(!$(".pop-editer-font").find('.item[data-fontname=' + curFamily+ ']').hasClass("download")) {
            $(".pop-editer-font").find('.item[data-fontname=' + curFamily+ ']').addClass("selected");
        }


        $(".opt-color-val").css({"background-color":curColor});//字体颜色
        $(".color-sub").find("span").removeClass("selected");

        console.info("editPage:" + editPage.html());
        setTplLayout(0.875);
    },

    ShowView: function (from, to) {
        console.info(from + " to " + to);
        $("#" + from).hide();
        $("#" + to).show();
    },

    GetNativeData: function (fn, params, cb) {
        cordova.exec(function (retval) {
            console.info(fn + "exec: " + retval);
            if(cb) cb(retval);
        }, function (e) {
            console.info(fn + " Error: " + e);
        }, "StoryTemplate", fn, params);

        console.info("fn:" + fn);
        console.info("params:");
        console.info(params);
    }
};

var browser = {
    versions: function () {
        var u = navigator.userAgent, app = navigator.appVersion;
        return {//移动终端浏览器版本信息
            trident: u.indexOf('Trident') > -1, //IE内核
            presto: u.indexOf('Presto') > -1, //opera内核
            webKit: u.indexOf('AppleWebKit') > -1, //苹果、谷歌内核
            gecko: u.indexOf('Gecko') > -1 && u.indexOf('KHTML') == -1, //火狐内核
            mobile: !!u.match(/AppleWebKit.*Mobile.*/)
            || !!u.match(/AppleWebKit/), //是否为移动终端
            ios: !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), //ios终端
            android: u.indexOf('Android') > -1 || u.indexOf('Linux') > -1, //android终端或者uc浏览器
            iPhone: u.indexOf('iPhone') > -1 || u.indexOf('Mac') > -1, //是否为iPhone或者QQHD浏览器
            iPad: u.indexOf('iPad') > -1, //是否iPad
            webApp: u.indexOf('Safari') == -1,//是否web应该程序，没有头部与底部
            google: u.indexOf('Chrome') > -1
        };
    }(),
    language: (navigator.browserLanguage || navigator.language).toLowerCase()
};

function loadjscssfile(filename, filetype) {

    if (filetype == "js") {
        var fileref = document.createElement('script');
        fileref.setAttribute("type", "text/javascript");
        fileref.setAttribute("src", filename);
    } else if (filetype == "css") {

        var fileref = document.createElement('link');
        fileref.setAttribute("rel", "stylesheet");
        fileref.setAttribute("type", "text/css");
        fileref.setAttribute("href", filename);
    }
    if (typeof fileref != "undefined") {
        document.getElementsByTagName("head")[0].appendChild(fileref);
    }

}

function set_wrap_width(dom){
    var wid = 0;
    var ul = dom.find('ul');
    var li = dom.find('li');
    li.each(function(){
        wid += $(this).outerWidth(true);
    });
    ul.css('width',wid+20);
    console.info("scroll width:" + (wid+1));
    console.info("scroll length:" + li.length);
}

function zero_fill_hex(num, digits) {
    var s = num.toString(16);
    while (s.length < digits)
        s = "0" + s;
    return s;
}

function rgb2hex(rgb) {

    if (rgb.charAt(0) == '#')
        return rgb;

    var ds = rgb.split(/\D+/);
    var decimal = Number(ds[1]) * 65536 + Number(ds[2]) * 256 + Number(ds[3]);
    return "#" + zero_fill_hex(decimal, 6);
}

function setPagesScroll() {
    set_wrap_width($("#pages-scroll"));
    if(WisapeEditer.pagesIScroll) WisapeEditer.pagesIScroll.destroy();
    WisapeEditer.pagesIScroll = new iScroll('pages-scroll',{
        scrollX: true,
        scrollY: false,
        click : true,
        hScrollbar :false,
        lockDirection:true
        //onScrollMove : function(){
        //    console.info("scrollMove");
        //    console.info(WisapeEditer.pagesIScroll.x);
        //    console.info(parseInt(Math.abs(WisapeEditer.pagesIScroll.x/200)) + 1);
        //    //if(len / myScroll.x * 200 )
        //    $("#pages-scroll li ").removeClass("active").eq(parseInt(Math.abs(WisapeEditer.pagesIScroll.x/liWidth)) + 1).addClass("active");
        //},
        //onScrollEnd : function(){
        //    console.info("scrollEnd");
        //    $("#pages-scroll li ").removeClass("active").eq(parseInt(Math.abs(WisapeEditer.pagesIScroll.x/liWidth)) + 1).addClass("active");
        //}
    });
}

var Dialog = {

    mask : $(".ui-mask"),

    init : function(target){
        this.mask.css({"width": $(window).width(),"height": $(window).height()});
        target.css({"margin-left" : -target.width()/2,"margin-top" : -target.height()/2});
    },
    show : function(target){
        this.init(target);
        this.mask.show();
        target.show();
    },
    hide : function(target){
        this.mask.hide();
        target.hide();
    }
};

function setTplLayout(sc){
    setTimeout(function(){
        $(".j-vmiddle").each(function(){
            var _this = $(this),
                parent = _this.parents(".stage-content");
            console.info("xx");
            console.info(_this.parents("div[symbol=top]"));
            scale = sc;

            //if(_this.css("transform").split("matrix(")[1]) {
            //    scale = parseFloat(_this.css("transform").split("matrix(")[1].split(",")[0]);
            //}

            console.info(scale);
            console.info(_this.css("transform"));
            console.info(_this.parents(".stage-content").height());
            console.info(_this.height());
            _this.css({
                "margin-top": (parent.height() - _this.height()*scale)/2
            })
        })
    },50)
}




