'use strict';

/**
 * @ngdoc function
 * @name sposApp.controller:GeneralCtrl
 * @description
 * # GeneralCtrl
 * Controller of the sposApp
 */
angular.module('sposApp')
  .controller('GeneralCtrl', function ($scope, $route) {
    $scope.$route = $route;
  });
