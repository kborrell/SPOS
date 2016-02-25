'use strict';

/**
 * @ngdoc directive
 * @name sposApp.directive:solverForm
 * @description
 * # solverForm
 */
angular.module('sposApp')
  .directive('solverForm', function () {
    return {
      templateUrl: 'views/directives_views/solver-form.html',
      restrict: 'E'
    };
  });

