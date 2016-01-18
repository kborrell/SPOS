'use strict';

/**
 * @ngdoc directive
 * @name sposApp.directive:sessionLogin
 * @description
 * # sessionLogin
 */
angular.module('sposApp')
  .directive('sessionLogin', function () {
    return {
      templateUrl: 'views/directives_views/session-login.html',
      restrict: 'E'
    };
  });
