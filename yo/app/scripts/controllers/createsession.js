'use strict';

/**
 * @ngdoc function
 * @name sposApp.controller:CreatesessionCtrl
 * @description
 * # CreatesessionCtrl
 * Controller of the sposApp
 */

angular.module('sposApp')
  .controller('CreatesessionCtrl', function ($scope, $http, VirtualMachine, Parameters, Session, fileReader) {
    $scope.vmConfig = {};
    $scope.parameters = {};
    $scope.session = {};


    $scope.predefinedVM = "";
    $scope.compatibleMethods = {};
    $scope.firstStepActive = true;
    $scope.uploadMessage = "";
    $scope.sessionCreated = false;

    $scope.clearPredefinedVM = function(){
        $scope.predefinedVMForm.$setPristine();
        $scope.predefinedVMForm.$setValidity();
        $scope.predefinedVMForm.$setUntouched();
        $scope.predefinedVM = "";
    };

    $scope.clearVMConfig = function(){
      $scope.vmConfig = {};
      $scope.vmConfigForm.$setPristine();
      $scope.vmConfigForm.$setValidity();
      $scope.vmConfigForm.$setUntouched();
    };

    $scope.getCompatibleMethods = function() {
        $http({
          method: 'GET',
          url: 'http://127.0.0.1:8080/models/search/findByModel?modelName=' + $scope.parameters.model
        }).then(function successCallback(response) {
            var model = angular.fromJson(response.data);
            $scope.compatibleMethods = model._embedded.models[0].compatibleMethods;
        });
    };

    $scope.createSession = function() {
      createSession();
      $scope.sessionCreated = true;
    };

    $scope.completeFirstStep = function() {
      $scope.firstStepActive = false;
      createVMConfig();
    };

    $scope.getFile = function () {
      fileReader.readAsText($scope.file, $scope)
        .then(function(result) {
          $scope.parameters.infoFile = result;
        });
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
      $scope.vmConfig = VirtualMachine.query({id:id});
    };

    var createVMConfig = function () {
      if ($scope.predefinedVM === ""){
        $scope.vmConfig.realPercentage = $scope.vmConfig.realPercentage / 100;
        VirtualMachine.save($scope.vmConfig);
        clearVMConfig();
      } else {
        getPredefinedVM();
      }
    };

    var createParameters = function () {
      $scope.parameters.isParallel = false;
      $scope.parameters.groupSize = 10;
      Parameters.save($scope.parameters);
    };

    var createSession = function () {
      $scope.session.vmConfig = $scope.vmConfig;
      $scope.session.info = $scope.parameters;
      if ($scope.session.type == 'Optimal'){
        $scope.session.maximumDuration = -1;
      }
      Session.save($scope.session);
    };
  });
