'use strict';

/**
 * @ngdoc function
 * @name yoApp.controller:CreatesessionCtrl
 * @description
 * # CreatesessionCtrl
 * Controller of the yoApp
 */
angular.module('yoApp')
  .controller('CreatesessionCtrl', function ($scope, VirtualMachine) {
    $scope.vmConfig = {};
    $scope.parameters = {};
    $scope.predefinedVM = "";

    var clearPredefinedVM = function(){
        $scope.predefinedVMForm.$setPristine();
        $scope.predefinedVMForm.$setValidity();
        $scope.predefinedVMForm.$setUntouched();
        $scope.predefinedVM = "";
    };

    var clearVMConfig = function(){
      $scope.vmConfig = {};
      $scope.vmConfigForm.$setPristine();
      $scope.vmConfigForm.$setValidity();
      $scope.vmConfigForm.$setUntouched();
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
      $scope.vmConfig = VirtualMachine.query({id:id}).$promise.then(function(vm) {
        alert(vm.cpuCount);
      });
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

    $scope.createSession = function() {
      createVMConfig();
    };
  });
