(function(){
    $.fn.iScroll = function(options){
        function set_wrap_width(dom){
            var wid = 0;
            var ul = dom.find('ul');
            var li = dom.find('li');
            li.each(function(){
                wid += $(this).outerWidth()
            });
            ul.css('width',wid+1);
        }
        return this.each(function(){
            set_wrap_width($(this));
            this.iScroll = new IScroll(this,options);
        });
    };

    var View = Class.create({
        initialize:function(root){
            this.root = root;
        }
    });
    window.View = View.prototype.constructor;

    var StageSelectView = View.extend({
        initialize: function(root) {
            StageSelectView.superclass.initialize.call(this, root);
            this.tabnav = root.find('.tabnav');
            this.tabnav_item = root.find('.tabnav li a');
            this.tabcon_item = root.find('.tabcon-item');
            this.tabnav.iScroll({
                scrollX:true,
                scrollY:false
            });
            this.tabcon_item.iScroll({
                scrollX:true,
                scrollY:false
            });
        }
    });
    window.StageSelectView = StageSelectView.prototype.constructor;

    function onDeviceReady() {
        window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, gotFS, fail);
    }
    function gotFS(fileSystem) {
        window.FS = fileSystem;
        console.log(FS.root.name);
        console.log(FS.root.fullPath);
        console.log(FS.root.toURL());
        $('[html-include]').each(function(){

        });
    }
    function fail(evt) {
        console.log(evt.target.error.code);
    }
    document.addEventListener('touchmove', function (e) { e.preventDefault(); }, false);
    document.addEventListener("deviceready", onDeviceReady, false);
})();