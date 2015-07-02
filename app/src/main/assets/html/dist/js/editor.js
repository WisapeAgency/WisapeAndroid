(function(window,$,Hammer){
    $.fn.hScroll = function(child){
        return this.each(function(){
            var alim = 5;
            var me = $(this);
            function getScrollWidth(){
                var w=0;
                me.find(child).each(function(){
                    w += parseInt($(this).css('margin-left'))+parseInt($(this).css('margin-right'))+$(this).width();
                });
                return w;
            }
            me.css('width',getScrollWidth()+alim);

            var ml = 0;
            var mov = 8;
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
    function Symbol(obj){
        this.stage = obj.stage;
        this.dom = obj.dom;
        this.name = this.dom.attr('name');
        this.type = this.dom.attr('type');
        if(this.type == 'text'){
            Text.call(this);
        }
        function Text(){
            var me = this;
            var dom = me.dom;
            me.value = dom.text();

            var hammer = new Hammer(dom.get(0));
            hammer.on('tap',function(event){
                dom.hasClass('focus')?dom.removeClass('focus'):dom.addClass('focus');
            });
        }
    }
    function Stage(obj){
        this.editor = obj.editor;
        this.path = obj.path;
        this.dom = null;
        this.symbols = new Array();
    }
    Stage.prototype.load = function(after){
        var me = this;
        var con = this.editor.stageContainer;
        $.get(this.path,function(data){
            me.dom = $(data);
            me.dom.css('width',$(window).width());
            con.append(me.dom);
            me.dom.find('symbol').each(function(){
                var symbol = new Symbol({
                    stage:me,
                    dom:$(this)
                });
                me.symbols.push(symbol);
            });
            if(typeof after == 'function'){
                after.call(me);
            }
        });
    };
    function WisapeEditor(){
        this.stageContainer = $('#stageContainer');
        this.topbar = $('#topbar');
        this.stageLib = $('#stage-lib');
        this.commandBtns = $('[we-command]');
        this.stages = new Array();
        this.currentIndex = 0;
        this.defualtsStagePath = '../stage/Blog/01/stage-sample.html';
    }
    WisapeEditor.prototype.ini = function(){
        var me = this;
        var bar = me.topbar;
        var con =  me.stageContainer;
        var btns = me.commandBtns;

        btns.click(function(){
            var command = $(this).attr('we-command');
            switch (command){
                case 'back':me.save();me.toStoryList();break;
                case 'preview':me.toPreview();break;
                case 'category':me.stageSelect('toggle');break;
                case 'list':me.toStageList();break;
                case 'settings':me.toSettings();break;
                case 'addStage':me.add(me.defualtsStagePath,function(){
                    me.stageSelect('open');
                });break;
            }
        });

        if(con.html() == ''){
            me.add(me.defualtsStagePath);
        }

        /*初始化模板选择*/
        var slib = me.stageLib;
        var cates = $('.stage-category');
        slib.find('.stage-tabnav [hscroll]').hScroll('.stage-category');
        cates.click(function(){
            if(!$(this).hasClass('active')){
                cates.removeClass('active');
                $(this).addClass('active');
            }
        });

        var conHammer = new Hammer(con.get(0));
        conHammer.on('swipeleft',function(event){
            if(me.currentIndex < me.stages.length - 1){
                me.currentIndex++;
                me.goto();
            }
        });
        conHammer.on('swiperight',function(event){
            if(me.currentIndex > 0){
                me.currentIndex--;
                me.goto();
            }
        });
    };
    /*获取本地已下载模板的缩略信息,需要安卓来配合*/
    WisapeEditor.prototype.getLocaleStage = function(){
        return [{
            id:1,
            category:'Blog', /*所属分类*/
            name:'01', /*模板名称*/
            path:'../stage/Blog/01/stage-sample.html', /*模板HTML文件路径*/
            thumbImg:'../stage/Blog/01/01.jpg' /*模板缩略图路径*/
        }];
    };
    /*获取远程服务器未下载模板的缩略信息,需要PHP来配合*/
    WisapeEditor.prototype.getRemoteStage = function(){
        return [{
            id:1,
            category:'Blog', /*所属分类*/
            name:'01', /*模板名称*/
            thumbImg:'http://fasdkfj.com/stage/Blog/01/01.jpg' /*模板缩略图路径*/
        }];
    };
    /*添加默认模板*/
    WisapeEditor.prototype.add = function(path,after){
        var me = this;
        var con = me.stageContainer;
        var stage = new Stage({
            editor: me,
            path: path
        });
        me.stages.push(stage);
        stage.load(function(){
            con.css('width',$(window).width() * me.stages.length);
            me.currentIndex = me.stages.length - 1;
            if(after && typeof after == 'function'){
                after.call(this);
            }
        });
    };
    /*更换模板*/
    WisapeEditor.prototype.swap = function(){

    };
    /*保存故事*/
    WisapeEditor.prototype.save = function(){

    };
    /*跳转到首页-故事列表页*/
    WisapeEditor.prototype.toStoryList = function(){

    };
    /*跳转到预览页*/
    WisapeEditor.prototype.toPreview = function(){

    };
    /*打开模板分类-选择模板*/
    WisapeEditor.prototype.goto = function(){
        var me = this;
        var con = me.stageContainer;
        if($('body').hasClass('topbar-opened')){
            if(me.stages.length > 1){
                $('stage').css('margin',0);
                me.stages[me.currentIndex].dom.css('margin-left',-0.3*$(window).width());
                me.stages[me.currentIndex].dom.css('margin-right',-0.3*$(window).width());
                con.css('left',-70-(100*(me.currentIndex-1))+'%');
            }else{
                me.stages[0].dom.css('margin',0);
                con.css('left',0);
            }
        }else{
            if(me.stages.length > 1){
                $('stage').css('margin',0);
                me.stages[me.currentIndex].dom.css('margin-left',-0.18*$(window).width());
                me.stages[me.currentIndex].dom.css('margin-right',-0.18*$(window).width());
                con.css('left',-82.5-(100*(me.currentIndex-1))+'%');
            }else{
                me.stages[0].dom.css('margin',0);
                con.css('left',0);
            }
        }
    };
    WisapeEditor.prototype.stageSelect = function(flag){
        var me = this;
        var bar = me.topbar;
        var con = me.stageContainer;
        if(flag == 'open'){
            open();
        }else if(flag == 'toggle'){
            if(!bar.attr('opened')){
                open();
            }else{
                close();
            }
        }
        function open(){
            $('body').addClass('topbar-opened');
            bar.attr('opened','opened');
            $('[we-command=category]').addClass('active');
            me.goto('open');
        }
        function close(){
            $('body').removeClass('topbar-opened');
            bar.removeAttr('opened');
            $('[we-command=category]').removeClass('active');
            me.goto('close');
        }
    };
    /*跳转到故事已有模板管理列表*/
    WisapeEditor.prototype.toStageList = function(){

    };
    /*跳转到故事设置页*/
    WisapeEditor.prototype.toSettings = function(){

    };
    window.WisapeEditor = WisapeEditor.prototype.constructor;
})(window,jQuery,Hammer);