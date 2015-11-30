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

    //var clearPredefinedVM = function(){
    //    $scope.predefinedVMForm.$setPristine();
    //    $scope.predefinedVMForm.$setValidity();
    //    $scope.predefinedVMForm.$setUntouched();
    //    $scope.predefinedVM = "";
    //};

    var clearVMConfig = function(){
      $scope.vmConfig = {};
      $scope.vmConfigForm.$setPristine();
      $scope.vmConfigForm.$setValidity();
      $scope.vmConfigForm.$setUntouched();
    };

    var createVMConfig = function () {

    };

    $scope.createSession = function() {

      createVMConfig();
      if ($scope.predefinedVM === ""){
        $scope.vmConfig.realPercentage = $scope.vmConfig.realPercentage / 100;
        VirtualMachine.save($scope.vmConfig);
        clearVMConfig();
      }
    };
  });
