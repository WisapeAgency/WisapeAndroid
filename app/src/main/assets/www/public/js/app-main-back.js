
    var WisapePlayer = angular.module('WisapePlayer',[]);

    var currentTplData = "";

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

    //获取接口数据
    WisapePlayer.service('GetNativeData', function(){

        this.get = function(fn,params,cb){
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
    });

    function ShowViewFn(from,to) {
        $("#" + from).hide();
        $("#" + to).show();
    }

    //切换页面
    WisapePlayer.value('ShowView',ShowViewFn)

    WisapePlayer.directive('onLastRepeat', function() {
        return function(scope, element, attrs) {
            if (scope.$last) setTimeout(function(){
                scope.$emit('onRepeatLast', element, attrs);
            }, 1);
        };
    })

    WisapePlayer.controller('MainCtrl',['$scope','browser','ShowView','$compile',function($scope,browser,ShowView,$compile){
        if(browser.versions.ios || browser.versions.android){
            $scope.mainHtmlUrl = 'editor_index.html';
            //移动端meta处理
            $('head').append('<meta name="viewport" content="width=device-width,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no"/>');
            $('head').append('<meta name="format-detection"content="telephone=no, email=no" />');

        }else{
            $scope.mainHtmlUrl = 'pc.html';
        }

        //切换页面
        $scope.showView = function(from,to){
            ShowView(from,to);
        };

        //检测DeviceReady，成功则$broadcast 'DeviceReady'
        var Timer = setInterval(function(){
                console.info("DeviceReady:" + DeviceReady);
                if(DeviceReady) {
                    $scope.$broadcast('DeviceReady', "");
                    clearInterval(Timer);
                }
            },1);

        //新增stage
        $scope.addStage = function(){
            var target = angular.element("#pages-scroll li.active");
            //var data= '<div class="stage"> <div class="stage-content" style="background: url(img/bg.jpg);background-size: 100% 100%;width:100%;height:100%;" ng-click="imgEdit($event)"> <div class="symbol" style="z-index: 2;" > <img class="img" class="pages-img" data-name="img1" ng-click="imgEdit($event)" style="" src="img/icon-earth.png" /> </div> <div class="symbol" style="z-index: 3;" > <div class="pages-txt" ng-click="textEdit($event)" style="margin:70px auto;width:150px;color:#fff;text-align: center;">As c update of the classic translation corpus, the combination of network technology and language essence</div> </div> </div> </div>';
            target.parent().find("li").removeClass("active");
            target.after($compile('<li class="active">' + currentTplData + '</li>')($scope));
            target.parent().find("li").bind("click",function(){
                var _this = $(this);
                //if(_this.hasClass("active")) {
                //    $("#editorText .pages").html(currentTplData);
                //    ShowView('mainHtml','editorText');
                //}
                _this.addClass("active").siblings().removeClass("active");
            });
            $("#pages-scroll").iScroll( {scrollX: true, scrollY: false, preventDefault: false });

        }

        //$scope.pageClick = function($event){
        //    var me = $($event.target);
        //    if(me.hasClass("active") || me.parents("li").hasClass("active")) {
        //        ShowView('mainHtml','editorText');
        //        return false;
        //    }
        //}

        $scope.imgEdit = function($event){
            ShowView('mainHtml','editorImage')
        }

        $scope.textEdit = function($event){
            var me = $($event.target);

            if(!me.hasClass("active")) {
                me.addClass("active");
                return false;
            }
            ShowView('mainHtml','editorText');
        }
    }]);

    //模版分类Ctrl
    WisapePlayer.controller('GetStageCategoryCtrl',['$scope','GetNativeData',function($scope,GetNativeData){
        $scope.StageCategory = "";

        $scope.$on("DeviceReady",function(d,data){

            //通过service 获取模版分类
            GetNativeData.get("getStageCategory", [],function(data){
                console.info("getStageCategory");
                console.info(data);
               $scope.$apply(function () {
                   $scope.StageCategory = data;
               });
            });

            //绑定事件
            $scope.$on('onRepeatLast', function(scope, element, attrs){
                var menuScroll = $("#menu-scroll");
                menuScroll.iScroll( { eventPassthrough: true,scrollX: true, scrollY: false, preventDefault: false });
                menuScroll.find("li").click(function(){
                    _me = $(this);
                    _me.addClass("active").siblings().removeClass("active");
                }).eq(0).click()
            });

           $scope.changeStageCategory = function(){
               var _me = this;
               console.info(_me.item.id);
               $scope.$emit('changeStageCategoryEvent', _me.item.id);
           }
        })
    }]);

    //模版列表Ctrl
    WisapePlayer.controller('GetStageListCtrl',['$scope','GetNativeData','$compile',function($scope,GetNativeData,$compile){

        $scope.GetStageList = "";
        $scope.$on('changeStageListEvent', function(d,data) {
            var id = data;
            GetNativeData.get("getStageList", [id],function(data){
                 $scope.$apply(function () {
                       console.info("GetStageList");
                       console.info(data);
                       console.info(JSON.parse(data));
                       $scope.GetStageList = JSON.parse(data);
                   });
            });

            $scope.$on('onRepeatLast', function(scope, element, attrs){
                var catScroll = $("#cat-scroll");

                catScroll.iScroll( { eventPassthrough: true,scrollX: true, scrollY: false, preventDefault: false });
                catScroll.find("li").click(function(){
                    _me = $(this);
                    _me.addClass("active").siblings().removeClass("active");
                })
            });
        });

        $scope.ChangeStage = function(){
            var _me = this;
            var tplName = _me.item.temp_name;
            var target = angular.element("#pages-scroll li.active");
            console.info("tplname:");
            console.info(tplName);
　          if(!_me.item.exists) {
                console.info("down");
                GetNativeData.get("start", [parseInt(_me.item.id),parseInt(_me.item.type)],function(data){
                    console.info("down data:");
                    console.info(data);
                });
            } else {
                //console.info("read path:" + me.data("path"));
                GetNativeData.get("read", ["/mnt/sdcard/wisape/com.wisape.android/data/template/" + tplName + "/stage.html"],function(data){
                    console.info("read data:");
                    console.info(data);
                    //data= '<div class="stage"> <div class="stage-content" style="background: url(img/bg.jpg);background-size: 100% 100%;width:100%;height:100%;" ng-click="showView(' + "'mainHtml'" + ',' + "'editorText'" +')"> <div class="symbol" style="z-index: 2;" > <img class="img" data-name="img1" style="" src="img/icon-earth.png" /> </div> <div class="symbol" style="z-index: 3;" > <div class="pages-txt" style="margin:70px auto;width:150px;color:#fff;text-align: center;">As c update of the classic translation corpus, the combination of network technology and language essence</div> </div> </div> </div>';
                    currentTplData = data;
                    target.html(data);
                    //target.after('<li class="active">' + data + '</li>');
                    //appendTarget.find("img").each(function(){
                    //    console.info("/mnt/sdcard/wisape/com.wisape.android/data/template/" + tplName + "/" +$(this).attr("original-src"));
                    //})
                });

               //target.parent().find("li").click(function(){
               //    var _this = $(this);
               //    if(_this.hasClass("active")) {
               //        ShowView('mainHtml','editorText');
               //    }
               //    _this.addClass("active").siblings().removeClass("active");
               //});

            }
        }
    }]);

    WisapePlayer.controller('EditerTextCtrl',['$scope',function($scope){
        $scope.popTextEditerOpt = function($event){
            var me = $($event.target);
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
        };
        $scope.setFontWeight = function($event){
            var me = $($event.target);
            var target = $("#editorText .pages-txt.selected");
            if(me.hasClass("active")) {
                me.removeClass("active");
                target.css({"font-weight":"normal"})
            } else {
                me.addClass("active");
                target.css({"font-weight":"bold"})
            }
        };
        $scope.setFontStyle = function($event){
            var me = $($event.target);
            var target = $("#editorText .pages-txt.selected");
            if(me.hasClass("active")) {
                me.removeClass("active");
                target.css({"font-style":"normal"})
            } else {
                me.addClass("active");
                target.css({"font-style":"italic"})
            }
        };
        $scope.setFontLink = function($event){
            var me = $($event.target);
            var target = $("#editorText .pages-txt.selected");
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
        };
        $scope.setFontAlign = function($event){
            var me = $($event.target);
            var target = $("#editorText .pages-txt.selected");
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
        };

        $scope.changePopLayer = function(target){
            var popLayers = $("#editorText .pop-layer");
            popLayers.hide().parent().find("."+target).show();
        };
        $scope.backPopLayerOpt = function(){
            var popLayers = $("#editorText .pop-layer");
            popLayers.hide().parent().find(".pop-editer-opt").show();
        }
    }])

    WisapePlayer.controller('EditerIndexCtrl',['$scope',function($scope){
        $scope.toggleLayout = function(){
            $(".tpl-page-cat").toggle();
        };
        $scope.$on('changeStageCategoryEvent', function(d,data) {
            $scope.$broadcast('changeStageListEvent', data);
        });
    }]);


