
function success(message) {
  var output = $('output').innerHTML + "<br/>\n" + message;
  DWRUtil.setValue('output', output);
}

function failure(message) {
  alert(message);
}

function runTests() {
  DWRUtil.setValue('output', "");

  testSyncNesting();
  testAsyncNesting();
}

function testAsyncNesting() {
  Test.slowStringParam("AsyncNesting-2", 1000, function(data1) {
    success(data1);
    Test.slowStringParam("AsyncNesting-4", 1000, function(data2) {
      success(data2);
    });
    success("AsyncNesting-3");
  });
  success("AsyncNesting-1");
}

function testSyncNesting() {
  DWREngine.setAsync(false);
  Test.slowStringParam("SyncNesting-1", 1000, function(data1) {
    success(data1);
    Test.slowStringParam("SyncNesting-2", 1000, function(data2) {
      success(data2);
    });
    success("SyncNesting-3");
  });
  success("SyncNesting-4");
  DWREngine.setAsync(true); 
}
