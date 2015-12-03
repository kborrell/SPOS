'use strict';

/**
 * @ngdoc function
 * @name yoApp.controller:CreatesessionCtrl
 * @description
 * # CreatesessionCtrl
 * Controller of the yoApp
 */
angular.module('yoApp')
  .controller('CreatesessionCtrl', function ($scope, $http, VirtualMachine) {
    $scope.vmConfig = {};
    $scope.parameters = {};
    $scope.predefinedVM = "";
    $scope.compatibleMethods = {};

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
      alert($scope.parameters.model);
      createVMConfig();
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


  });
