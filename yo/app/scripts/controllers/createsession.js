'use strict';

/**
 * @ngdoc function
 * @name sposApp.controller:CreatesessionCtrl
 * @description
 * # CreatesessionCtrl
 * Controller of the sposApp
 */

angular.module('sposApp')
  .controller('CreateSessionCtrl', function ($scope, $q, $state, $http, $location, VirtualMachine, Parameters, Session, fileReader, ModelInfo, MethodInfo) {
    $scope.vmConfig = {virtualCPUs:0, realCPUs:0, ram:0};
    $scope.parameters = {isClustered: false, files: []};
    $scope.session = {};

    $scope.CreateState = {
      FIRSTSTEP: 1,
      SECONDSTEP: 2,
      CREATING: 3,
      CREATED: 4,
      ERROR: 5
    };

    $scope.CreateStep = {
      CREATING_SESSION: 1,
      UPLOADING_FILE: 2
    };

    $scope.MethodLoadState = {
      NONLOADED: 1,
      LOADING: 2,
      LOADED: 3,
      ERROR: 4
    };

    $scope.location = $location;
    $scope.predefinedVM = "";
    $scope.createStep = $scope.CreateStep.CREATING_SESSION;
    $scope.state = $scope.CreateState.FIRSTSTEP;
    $scope.methodLoadState = $scope.MethodLoadState.NONLOADED;
    $scope.uploadMessage = "";
    $scope.selectedModel = "";
    $scope.selectedMethod = "";
    $scope.errorFile = "";
    $scope.problemType = "";

    $scope.sessionKey = "";
    $scope.sessionId = "";

    var resultsMod =  "main {\n" +
      "    var before = new Date();\n" +
      "\tvar temp = before.getTime();\n" +
      "\t\tthisOplModel.generate();\n" +
      "    \tif (cplex.solve()) {\n" +
      "    \t\t\twriteln(\"+-+-+-+\");\n" +
      "    \t\t\twriteln(\"-- Objective = \", cplex.getObjValue());\n" +
      "    \t\t\twriteln(\"-- Solution: \");\n" +
      " \t\t\t\twriteln(thisOplModel.printSolution());\n" +
      "                var after = new Date();\n" +
      "\t\t\t\twriteln(\"-- Solving time ~= \",after.getTime()-temp, \"ms\");   \t\n" +
      "        }else{\n" +
      "        \t\twriteln(\"ERROR - No Solution for this model!\");\n" +
      "        }\n" +
      "    writeln(\"+-+-+-+\"); \n" +
      "}";

    function toUTF8Array(str) {
      var utf8 = [];
      for (var i = 0; i < str.length; i++) {
        var charcode = str.charCodeAt(i);
        if (charcode < 0x80) utf8.push(charcode);
        else if (charcode < 0x800) {
          utf8.push(0xc0 | (charcode >> 6),
            0x80 | (charcode & 0x3f));
        }
        else if (charcode < 0xd800 || charcode >= 0xe000) {
          utf8.push(0xe0 | (charcode >> 12),
            0x80 | ((charcode >> 6) & 0x3f),
            0x80 | (charcode & 0x3f));
        }
        // surrogate pair
        else {
          i++;
          // UTF-16 encodes 0x10000-0x10FFFF by
          // subtracting 0x10000 and splitting the
          // 20 bits of 0x0-0xFFFFF into two halves
          charcode = 0x10000 + (((charcode & 0x3ff) << 10)
            | (str.charCodeAt(i) & 0x3ff));
          utf8.push(0xf0 | (charcode >> 18),
            0x80 | ((charcode >> 12) & 0x3f),
            0x80 | ((charcode >> 6) & 0x3f),
            0x80 | (charcode & 0x3f));
        }
      }
      return utf8;
    }

    $scope.clearPredefinedVM = function () {
      $scope.predefinedVM = "";
    };

    $scope.clearVMConfig = function () {
      $scope.vmConfig = {virtualCPUs:0, realCPUs:0, ram:0};
    };

    $scope.getCompatibleMethods = function () {
      $scope.methodLoadState = $scope.MethodLoadState.LOADING;
      ModelInfo.query({action: 'search', search: 'findByModel', modelName: $scope.selectedModel})
        .$promise.then(function (modelResponse) {
        $scope.parameters.model = modelResponse._embedded.models[0];
        $scope.methodLoadState = $scope.MethodLoadState.LOADED;
      }).catch(function (error) {
        $scope.methodLoadState = $scope.MethodLoadState.ERROR;
      });
    };

    $scope.loadMethod = function () {
      if ($state.current.name == "createSession") {
        SetSimpleVM();
        $scope.state = $scope.CreateState.SECONDSTEP;
      }
      MethodInfo.query({action: 'search', search: 'findByMethod', methodName: $scope.selectedMethod})
        .$promise.then(function (methodResponse) {
        $scope.parameters.method = methodResponse._embedded.methods[0];
      });
    };

    $scope.createSession = function () {
      $scope.state = $scope.CreateState.CREATING;
      createSession();
    };

    $scope.completeFirstStep = function () {
      $scope.state = $scope.CreateState.SECONDSTEP;
      createVMConfig();
    };

    $scope.getFile = function () {

      fileReader.readAsText($scope.file, $scope)
        .then(function (result) {
          if ($scope.file.name.split('.').pop() == "mod" && ((result.indexOf("main") == -1) || (result.indexOf("execute") == -1) )){
            result += "\n" + resultsMod;
          }
          var file = {extension: $scope.file.name.split('.').pop(), name: $scope.file.name, content: toUTF8Array(result)};
          //$scope.parameters.files.push(file);
        });

      $("#input-" + $scope.fileType).fileinput('disable');
    };



    var uploadFileToUrl = function(files, uploadUrl, success, error){
      var fd = new FormData();

      for (var i=0; i < files.length; i++)
      {
        fd.append('file' + i, files[i]);
      }

      $http.post(uploadUrl, fd, {
        transformRequest: angular.identity,
        headers: {'Content-Type': undefined}
      })
        .success(function(){
          success();
        })
        .error(function(){
          error();
        });
    };

    $scope.uploadFiles = function (url, success, error) {
      var files = [$scope.file1];
      if ($scope.file2)
        files.push($scope.file2);
      uploadFileToUrl(files, url, success, error);
    };

    var getPredefinedVM = function () {
      var id = -1;
      switch ($scope.predefinedVM) {
        case "High":
          id = 3;
          break;
        case "Medium":
          id = 2;
          break;
        case "Low":
          id = 1;
          break;
      }
      $scope.session.vmConfig = VirtualMachine.query({id: id});
    };

    var createVMConfig = function () {
      if ($scope.predefinedVM === "") {
        $http.post('http://193.144.12.55/virtualmachine', $scope.vmConfig)
          .success(function (data, status, headers, config) {
            VirtualMachine.get({id: headers('Location').split('/').pop()}).$promise.then(function (vm) {
              $scope.session.vmConfig = vm;
            });

          });
        $scope.clearVMConfig();
      } else {
        getPredefinedVM();
      }
    };

    var createSession = function () {
      CreateSession();
    };

    var SetSimpleVM = function() {
      $scope.session.vmConfig = VirtualMachine.query({id: 3});
    };

    var CreateSession = function() {
      $scope.session.info = $scope.parameters;
      if ($scope.session.type == 'Optimal') {
        $scope.session.maximumDuration = -1;
      }

      Session.save($scope.session).$promise.then(function (session) {
        $scope.sessionId = session.id;
        $scope.sessionKey = session.key;

        $scope.uploadFiles("http://193.144.12.55/session/" + $scope.sessionId + "/uploadFiles?key=" + $scope.sessionKey, function () {
          $scope.state = $scope.CreateState.CREATED;
        }, function() {
          $scope.state = $scope.CreateState.ERROR;
        });
      }).catch(function (error) {
        $scope.state = $scope.CreateState.ERROR;
        if (error.data.message.substring(0, 7) == "VMERROR")
        {
          $scope.errorType = "VM";
        }
      });
    };

    $scope.activateInput = function (format) {
      $scope.fileType = format;
      $scope.parameters.files = [];
      $('#input-mps').fileinput('reset');
      $('#input-lp').fileinput('reset');
      $('#input-dat').fileinput('reset');
      $('#input-mod').fileinput('reset');

      $('#input-mps').fileinput('enable');
      $('#input-lp').fileinput('enable');
      $('#input-dat').fileinput('enable');
      $('#input-mod').fileinput('enable');

      $('#input-mps').fileinput('clear');
      $('#input-lp').fileinput('clear');
      $('#input-dat').fileinput('clear');
      $('#input-mod').fileinput('clear');
    }

    $scope.validateFiles = function () {

      if (!$scope.file1)
        return false;

      if (($scope.file1.name.split('.').pop() == "dat" || $scope.file1.name.split('.').pop() == "mod") && !$scope.file2)
        return false;

      return true;
    };

    $scope.setDeterminist = function () {
      if ($scope.problemType == "Determinist") {
        $scope.methodLoadState = $scope.MethodLoadState.LOADING;
        ModelInfo.query({action: 'search', search: 'findByModel', modelName: "Determinist"})
          .$promise.then(function (modelResponse) {
          $scope.parameters.model = modelResponse._embedded.models[0];
          $scope.methodLoadState = $scope.MethodLoadState.LOADED;
        }).catch(function (error) {
          $scope.methodLoadState = $scope.MethodLoadState.ERROR;
        });
      }
    }

    $scope.vCpuSlider = {
      options: {
        floor: 1,
        ceil: 10,
        showTicks: true,
        onChange: function(id) {
          $scope.clearPredefinedVM();
        },
        translate: function(value) {
          return value + " vCPUs";
        }
      }
    };

    $scope.rCpuSlider = {
      options: {
        floor: 0.5,
        ceil: 10,
        step: 0.5,
        precision: 1,
        showTicks: 1,
        onChange: function(id) {
          $scope.clearPredefinedVM();
        },
        translate: function(value) {
          return value + " CPUs";
        }
      }
    };

    $scope.memSlider = {
      options: {
        floor: 512,
        ceil: 10240,
        step:  256,
        precision: 0,
        showTicks: 1024,
        onChange: function(id) {
          $scope.clearPredefinedVM();
        },
        translate: function(value) {
          return value + " Mb";
        }
      }
    };
  });
