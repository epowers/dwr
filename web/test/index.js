
function init() {
  DWREngine.setErrorHandler(function(message) { alert(message); });
  DWREngine.setWarningHandler(function(message) { alert(message); });
  DWRUtil.useLoadingMessage();
}

function test1() {
  Test.doNothing();
}

function results(message) {
  DWRUtil.addRows("results", message);
}
