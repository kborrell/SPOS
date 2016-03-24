'use strict';

/**
 * @ngdoc function
 * @name sposApp.controller:CreatesessionCtrl
 * @description
 * # CreatesessionCtrl
 * Controller of the sposApp
 */

angular.module('sposApp')
  .controller('CreateSessionCtrl', function ($scope, $q, $http, $location, VirtualMachine, Parameters, Session, fileReader, ModelInfo, MethodInfo) {
    $scope.vmConfig = {};
    $scope.parameters = {isClustered: false, files: []};
    $scope.session = {};

    $scope.CreateState = {
      FIRSTSTEP: 1,
      SECONDSTEP: 2,
      CREATING: 3,
      CREATED: 4,
      ERROR: 5
    };

    $scope.MethodLoadState = {
      NONLOADED: 1,
      LOADING: 2,
      LOADED: 3,
      ERROR: 4
    };

    $scope.location = $location;
    $scope.predefinedVM = "";
    $scope.state = $scope.CreateState.FIRSTSTEP;
    $scope.methodLoadState = $scope.MethodLoadState.NONLOADED;
    $scope.uploadMessage = "";
    $scope.selectedModel = "";
    $scope.selectedMethod = "";
    $scope.errorFile = "";
    $scope.problemType = "";

    $scope.sessionKey = "";
    $scope.sessionId = "";

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
      $scope.vmConfig = {};
      $scope.vmConfigForm.$setPristine();
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
          var file = {extension: $scope.file.name.split('.').pop(), name: $scope.file.name, content: toUTF8Array(result)};
          $scope.parameters.files.push(file);
        });

      $("#input-" + $scope.fileType).fileinput('disable');
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
        $scope.vmConfig.realPercentage = $scope.vmConfig.realPercentage / 100;
        $http.post('http://127.0.0.1:8080/virtualmachine', $scope.vmConfig)
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
      $scope.session.info = $scope.parameters;
      if ($scope.session.type == 'Optimal') {
        $scope.session.maximumDuration = -1;
      }

      Session.save($scope.session).$promise.then(function (session) {
        $scope.sessionId = session.id;
        $scope.sessionKey = session.key;
        $scope.state = $scope.CreateState.CREATED;
      }).catch(function (error) {
        $scope.state = $scope.CreateState.ERROR;
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

      if ($scope.parameters.files.length == 0)
        return false;

      if ($scope.parameters.files[0].name == "")
        return false;

      if ($scope.parameters.files[0].extension == "dat" && $scope.parameters.files.length < 2)
        return false;

      if ($scope.parameters.files[0].extension == "dat" && $scope.parameters.files[1].name == "")
        return false;

      return true;
    }

    $scope.setDeterminist = function () {
      if ($scope.problemType == "Determinist") {
        ModelInfo.query({action: 'search', search: 'findByModel', modelName: "Determinist"})
          .$promise.then(function (modelResponse) {
            $scope.parameters.model = modelResponse._embedded.models[0];
          }
        );
      }
    }
  });
