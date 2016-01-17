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
      $scope.key = $stateParams.key;
      $scope.id = $stateParams.id;
  });
