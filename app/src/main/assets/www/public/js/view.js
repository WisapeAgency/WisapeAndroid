(function(window,$,Hammer){
    var win_w = $(window).width();
    var win_h = $(window).height();
    $.fn.hScroll = function(child){
        return this.each(function(){
            var alim = 5;
            var me = $(this);
            function getScrollWidth(){
                var w=0;
                me.find(child).each(function(){
                    w += parseInt($(this).css('margin-left'))+
                        parseInt($(this).css('margin-right'))+
                        parseInt($(this).css('border-left-width'))+
                        parseInt($(this).css('border-right-width'))+
                        $(this).width();
                });
                return w;
            }
            me.css('width',getScrollWidth()+alim);

            var ml = 0;
            var mov = 10;
            var hammer = new Hammer(me.get(0));
            hammer.on('pan',function(event){
                if(event.direction == 2){
                    if(ml-mov>-(me.width()-me.parent().width())){
                        ml-=mov;
                    }else{
                        ml=-(me.width()-me.parent().width());
                    }
                }else if(event.direction == 4){
                    if(ml+mov<0){
                        ml+=mov;
                    }else{
                        ml=0;
                    }
                }
                me.css('margin-left',ml);
            });
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
    /*模板选择*/
    function StageSelectView(){
        var me = this;
        var rootView = this.rootView = $('#stageSelect');
        var topbar = this.topbar = $('#stageSelect .topbar');
        var stageContainer = this.stageContainer = $('#stageContainer');
        var stages = this.stages = stageContainer.find('stage');
        var sci = this.stageCurrentIndex = 0;

        var view_textEditor = this.view_textEditor = new TextEditView(me);

        rootView.css('z-index',3);
        rootView.find('.imgIco-addStage').click(function(){
            /*添加模板*/
        });

        /*initialize topbar*/
        topbar.find('.tabnav').hScroll('.tabnav-item');
        topbar.find('.tabcon-item').hScroll('.stage-thumb');
        topbar.find('.tabnav-item').click(function(){
            /*切换模板*/
            $(this).addClass('active').siblings().removeClass('active');
        });
        topbar.find('.tab').tab();
        topbar.find('.imgIco-back').click(function(){
            /*To首页*/
        });
        topbar.find('.imgIco-preview').click(function(){
            /*To预览*/
        });
        topbar.find('.imgIco-category').click(function(){
            /*打开/关闭模板库*/
            if(topbar.hasClass('expand')){
                topbar.trigger('close');
            }else{
                topbar.trigger('open');
            }
        });
        topbar.find('.imgIco-list').click(function(){
            /*To模板删除管理*/
        });
        topbar.find('.imgIco-settings').click(function(){
            /*To故事设置*/
        });
        topbar.on('open',function(){
            topbar.find('.imgIco-category').addClass('active');
            topbar.addClass('expand');
            stageContainer.addClass('expand');
            scaleStage('small');
        });
        topbar.on('close',function(){
            topbar.find('.imgIco-category').removeClass('active');
            topbar.removeClass('expand');
            stageContainer.removeClass('expand');
            scaleStage('big');
        });

        /*initialize stageContainer*/
        stages.each(function(idx){
            var stage = $(this);
            var btn = $('<span class="imgIco-changeStageBg"></span>');
            stage.attr('style',stage.attr('init-style'));
            stage.css('width',win_w);
            stage.css('height',win_h);
            stage.find('symbol').each(function(){
                var symbol = $(this);
                var symbol_type = symbol.attr('type');
                symbol.attr('style',symbol.attr('init-style'));
                if(symbol_type == 'text'){
                    symbol.click(function(){
                        view_textEditor.open(stage,symbol.attr('name'));
                    });
                }
            });
            stage.append(btn);
            btn.click(function(){
                /*To图片编辑*/
            });
        });
        stages.eq(sci).addClass('current').css('margin-right',-win_w * 0.18);
        stageContainer.css('width',win_w*stageContainer.find('stage').size());
        stageContainer.click(function(){
            topbar.trigger('close');
        });
        var hammer_stageContainer = new Hammer(stageContainer.get(0));
        hammer_stageContainer.get('swipe').set({
            velocity: 0.25
        });
        hammer_stageContainer.on('swipeleft',function(){
            if(sci < stages.size()-1){
                sci = me.stageCurrentIndex = sci + 1;
                movetoStage();
            }
        });
        hammer_stageContainer.on('swiperight',function(){
            if(sci > 0){
                sci = me.stageCurrentIndex = sci - 1;
                movetoStage();
            }
        });

        function movetoStage(){
            if(stageContainer.hasClass('expand')){
                scaleStage('small');
            }else{
                scaleStage('big');
            }
        }

        function scaleStage(flag){
            stages.removeClass('current');
            stages.eq(sci).addClass('current');
            stages.css('margin-right',0);
            stages.css('margin-left',0);
            if(flag == 'small'){
                if(sci == 0){
                    stages.eq(sci).css('margin-right',-win_w * 0.3);
                    stageContainer.css('left',0);
                }else if(sci == stages.size()-1){
                    stages.eq(sci).css('margin-left',-win_w * 0.3);
                    stageContainer.css('left',-(100*sci)+30+'%');
                }else{
                    stages.eq(sci).css('margin-right',-win_w * 0.3);
                    stages.eq(sci).css('margin-left',-win_w * 0.3);
                    stageContainer.css('left',-(100*sci)+30+'%');
                }
            }else if(flag == 'big'){
                if(sci == 0){
                    stages.eq(sci).css('margin-right',-win_w * 0.18);
                    stageContainer.css('left',0);
                }else if(sci == stages.size()-1){
                    stages.eq(sci).css('margin-left',-win_w * 0.18);
                    stageContainer.css('left',-(100*sci)+18+'%');
                }else{
                    stages.eq(sci).css('margin-right',-win_w * 0.18);
                    stages.eq(sci).css('margin-left',-win_w * 0.18);
                    stageContainer.css('left',-(100*sci)+18+'%');
                }
            }
        }
    }
    /*文字编辑*/
    function TextEditView(mainView){
        var root = this.rootView = $('#textEdit');
        var container = this.textEditContainer = $('#textEditContainer');
        var topbar = this.topbar = root.find('.topbar');
        topbar.find('.imgIco-back').click(function(){
            /*回到主编辑页面_更新模板*/
            root.css('z-index','1');
        });
    }
    TextEditView.prototype.open = function(stage,focus){
        if(this.stage!=null){
            this.stage.remove();
        }
        var root = this.rootView;
        var container = this.textEditContainer;
        var stage = this.stage = stage.clone();
        container.append(stage);
        stage.find('.imgIco-changeStageBg').remove();
        stage.removeAttr('style');
        stage.attr('style',stage.attr('init-style'));
        root.css('z-index','6');
    };
    $(document).ready(function(){
        var view_ssv = new StageSelectView();
    });
})(window,jQuery,Hammer);