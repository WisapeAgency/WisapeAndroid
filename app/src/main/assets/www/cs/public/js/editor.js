var viewport = $('[name=viewport]');
viewport.attr('content','width=640,minimum-scale=0.5,maximum-scale=1.0,user-scalable=no,initial-scale='+$(window).width()/640);
window.setTimeout(function(){
    $('body').removeClass('hide');
},200);

var editor = angular.module('WisapeEditor',[]);
editor.controller('angularTest',function($scope){
    $scope.text = 'welcome!'
});
