'use strict';

/**
 * @ngdoc function
 * @name sposApp.controller:ShowSessionCtrl
 * @description
 * # ShowSessionCtrl
 * Controller of the sposApp
 */
angular.module('sposApp')
  .controller('ShowSessionCtrl', function ($scope, $http, $stateParams, Session) {
      $scope.sessionKey = $stateParams.key;
      $scope.sessionId = $stateParams.id;

      $scope.logged = false;

      $scope.init = function () {
        $scope.logged = $scope.sessionKey && $scope.sessionId;
      };

      $scope.logInSession = function(){
        $scope.logged = true;
      };

      $scope.init();
  });
