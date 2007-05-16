
function init() {
  var prop;
  for (prop in window) {
    if (prop.match(/test/) && typeof window[prop] == "function" && prop != "testEquals") {
      dwr.util.addOptions("buttons", [ prop ], function(data) {
        return "<button onclick='dwr.util.setValue(\"output\", \"\"); " + prop + "()'>" + prop.substring(4) + "</button>";
        // return "<input name='submit' type='image' src='imagebutton.png' onclick='dwr.util.setValue(\"output\", \"\"); " + prop + "()' />";
      }, { escapeHtml:false });
    }
  }
}

function success(message) {
  var output = $('output').innerHTML + "<br/>\n" + message;
  dwr.util.setValue('output', output, { escapeHtml:false });
}

function failure(message) {
  alert(message);
}

function assertTrue(value) {
  if (!value) {
    try { throw new Error(); }
    catch (ex) { alert("Failue at: " + ex.stack); }
  }
}

function assertFalse(value) {
  if (value) {
    try { throw new Error(); }
    catch (ex) { alert(ex.stack); }
  }
}

function testXss() {
  assertTrue(dwr.util.containsXssRiskyCharacters("dd<"));
  assertFalse(dwr.util.containsXssRiskyCharacters("dd"));
}

function testError1() {
  var counter = 0;
  var exceptionHandler = function(message, ex) {
    if (message == null) failure("message is null");
    else if (ex == null) failure("ex is null");
    else if (ex.message == null) failure("ex.message is null");
    else if (ex.message != message) failure("ex.message [" + ex.message + "] != message [" + message + "]");
    else if (ex.javaClassName != "java.lang.NullPointerException") failure("ex.javaClassName is not NPE [" + ex.javaClassName + "]");
    else counter++;
  };
  var warningHandler = function(data) {
    failure("Wrong call of warningHandler with data: " + data);
  };
  var callback = function(data) {
    failure("Wrong call of callback with data: " + data);
  };
  var errorHandler = function(data) {
    failure("Wrong call of errorHandler with data: " + data);
  };
  var callData = {
    callback:callback,
    errorHandler:errorHandler,
    warningHandler:warningHandler,
    exceptionHandler:exceptionHandler
  };

  callData.httpMethod = "POST";
  callData.rpcType = dwr.engine.XMLHttpRequest;
  Test.throwNPE(callData);

  callData.rpcType = dwr.engine.IFrame;
  Test.throwNPE(callData);

  callData.rpcType = dwr.engine.ScriptTag;
  Test.throwNPE(callData);

  callData.httpMethod = "GET";
  callData.rpcType = dwr.engine.XMLHttpRequest;
  Test.throwNPE(callData);

  callData.rpcType = dwr.engine.IFrame;
  Test.throwNPE(callData);

  callData.rpcType = dwr.engine.ScriptTag;
  callData.exceptionHandler = function(message, ex) {
    exceptionHandler(message, ex);
    if (counter != 5) failure("Some errors did not complete (Sync issue?) counter = " + counter);
    else success("Error test passed");
  };
  Test.throwNPE(callData);
}

function testError2() {
  var incorrectHandler = function(data) {
    failure("Wrong call of warningHandler with data: " + data + ". See console for more");
    dwr.engine._debug("Wrong call of warningHandler with data: " + data, true);
  }
  dwr.engine.setWarningHandler(incorrectHandler);
  var counter = 0;
  var correctHandler = function(data) {
    counter++;
  };
  var temp = dwr.engine._errorHandler;

  // Test exceptions bouncing to global errors
  dwr.engine.setErrorHandler(correctHandler);
  Test.throwNPE();

  // Test exceptions bouncing to batch errors
  dwr.engine.setErrorHandler(incorrectHandler);
  dwr.engine.beginBatch();
  Test.throwNPE();
  dwr.engine.endBatch({
    errorHandler:correctHandler,
    warningHandler:incorrectHandler
  });

  // Test exceptions bouncing to call errors
  Test.throwNPE({
    errorHandler:function(data) {
      correctHandler(data);
      if (counter != 3) failure("Some errors did not complete (Sync issue?) counter = " + counter);
      else success("Error test passed");
    },
    warningHandler:incorrectHandler
  });

  dwr.engine.setErrorHandler(temp);
}

function test404Handling() {
  var oldPath = Test._path;
  Test._path = "/thisPathDoesNotExist/dwr";
  Test.intParam(1, {
    callback:function(data) {
      failure("New path did not take effect");
    },
    errorHandler:function(message, ex) {
      if (ex.name != "dwr.engine.http.404") failure("Error name is: " + ex.name);
      else if (ex.message != message) failure("errorHandler message != ex.message");
      else if (!message.match(/<html>/)) failure("Expected HTML in response");
      else success("Correct 404 handling");
    }
  });
  Test._path = oldPath;
  Test.intParam(1, function(data) {
    success("Path handling returned to normal");
  });
}

function testAsyncNesting() {
  Test.slowStringParam("AsyncNesting-2", 100, function(data1) {
    success(data1);
    Test.slowStringParam("AsyncNesting-End", 200, function(data2) {
      success(data2);
    });
    success("AsyncNesting-3");
  });
  success("AsyncNesting-1");
}

function testSyncNesting() {
  dwr.engine.setAsync(false);
  Test.slowStringParam("SyncNesting-1", 100, function(data1) {
    success(data1);
    Test.slowStringParam("SyncNesting-2", 100, function(data2) {
      success(data2);
    });
    success("SyncNesting-3");
  });
  success("SyncNesting-End");
  dwr.engine.setAsync(true); 
}

function testSyncInCallMetaData() {
  Test.slowStringParam("SyncInCallMetaData-1", 100, {
    async:false,
    callback:function(data) {
      success(data);
    }
  });
  success("SyncInCallMetaData-End");
}

function testAsyncInCallMetaData() {
  Test.slowStringParam("AsyncInCallMetaData-End", 100, {
    async:true,
    callback:function(data) {
      success(data);
    }
  });
  success("AsyncInCallMetaData-1");
}

function testDefaultAsyncInCallMetaData() {
  Test.slowStringParam("DefaultAsyncInCallMetaData-End", 100, {
    callback:function(data) {
      success(data);
    }
  });
  success("DefaultAsyncInCallMetaData-1");
}

function testParameterPassing() {
  Test.listParameters({
    callback:function(data) {
      if (data.param1 != 'value1') failure("Parameter passing failure: data.param1=" + data.param1);
      else if (data.param2 != 'value2') failure("Parameter passing failure: data.param2=" + data.param2);
      else success("ParameterPassing-1");
    },
    parameters:{
      'param1':'value1',
      'param2':'value2'
    }
  });
}

function testHeaderPassing() {
  Test.listHeaders({
    callback:function(data) {
      if (data.param1 != 'value1') failure("Header passing failure: data.param1=" + data.param1);
      else if (data.param2 != 'value2') failure("Header passing failure: data.param2=" + data.param2);
      else success("HeaderPassing-1");
    },
    headers:{
      'param1':'value1',
      'param2':'value2'
    }
  });
}

function testServerChecks() {
  Test.serverChecks({
    callback:function(data) { success(data); },
    exceptionHandler:function(ex) { fail(ex.message); }
  });
}

function testExceptionDetail1() {
  Test.throwNPE({
    callback:function(data) { fail(data); },
    exceptionHandler:function(message, ex) {
      if (message != "NullPointerException") failure("message is not 'NullPointerException': " + message);
      else if (ex == null) failure("ex is null");
      else if (ex.message != "NullPointerException") failure("ex.message is not 'NullPointerException': " + ex.message);
      else if (ex.javaClassName != "java.lang.NullPointerException") failure("ex.javaClassName is not NPE [" + ex.javaClassName + "]");
      else success("testExceptionDetail1");
    }
  });
}

function testExceptionDetail2() {
  Test.throwIAE({
    callback:function(data) { fail(data); },
    exceptionHandler:function(message, ex) {
      if (message != "Error") failure("message is not 'Error': " + message);
      else if (ex == null) failure("ex is null");
      else if (ex.message != "Error") failure("ex.message is not 'Error': " + ex.message);
      else if (ex.javaClassName != "java.lang.Throwable") failure("ex.javaClassName is not Throwable [" + ex.javaClassName + "]");
      else success("testExceptionDetail2");
    }
  });
}

function testExceptionDetail3() {
  Test.throwSPE({
    callback:function(data) { fail(data); },
    exceptionHandler:function(message, ex) {
      if (message != "SAXParseException") failure("message is not 'SAXParseException': " + message);
      else if (ex == null) failure("ex is null");
      else if (ex.message != "SAXParseException") failure("ex.message is not 'SAXParseException': " + ex.message);
      else if (ex.lineNumber != 42) failure("ex.lineNumber is not 42: " + ex.lineNumber);
      else if (ex.javaClassName != "org.xml.sax.SAXParseException") failure("ex.javaClassName is not SPE [" + ex.javaClassName + "]");
      else if (ex.exception.javaClassName != "java.lang.NullPointerException") failure("ex.exception.javaClassName is not NPE [" + ex.exception.javaClassName + "]");
      else success("testExceptionDetail3");
    }
  });
}