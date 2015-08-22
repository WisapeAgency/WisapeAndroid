function getStageCategory(){
    cordova.exec(function(retval) {
        showMessage(retval);
    }, function(e) {
        alert("Error: "+e);
    }, "StoryTemplate", "getStageCategory", []);
}
function getStageList(id){
    cordova.exec(function(retval) {
        showMessage(retval);
    }, function(e) {
        alert("Error: "+e);
    }, "StoryTemplate", "getStageList", [id]);
}
function getStagePath(id){
    cordova.exec(function(retval) {
        showMessage(retval);
    }, function(e) {
        alert("Error: "+e);
    }, "StoryTemplate", "getStagePath", [id]);
}
function getStoryPath(id){
    cordova.exec(function(retval) {
        showMessage(retval);
    }, function(e) {
        alert("Error: "+e);
    }, "StoryTemplate", "getStoryPath", [id]);
}
function start(id){
    cordova.exec(function(retval) {
        showMessage(retval);
    }, function(e) {
        alert("Error: "+e);
    }, "StoryTemplate", "start", [id]);
}
function read(tempName){
    cordova.exec(function(retval) {
        showMessage(retval);
    }, function(e) {
        alert("Error: "+e);
    }, "StoryTemplate", "read", [tempName]);
}
function setting(id){
    cordova.exec(function(retval) {
        <!--showMessage(retval);-->
    }, function(e) {
        alert("Error: "+e);
    }, "StoryTemplate", "setting", [id]);
}
function onDownloading(progress){
    var message = "progress:" + progress;
    showMessage(message);
}
function onCompleted(path){
    showMessage(path);
}
function onError(){
    alert("download error!");
}
function showMessage(message){
    console.log(JSON.stringify(message));
}
