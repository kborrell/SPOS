'use strict';

/**
 * @ngdoc directive
 * @name sposApp.directive:problemForm
 * @description
 * # problemForm
 */
angular.module('sposApp')
  .directive('problemForm', function () {
    return {
      templateUrl: 'views/directives_views/problem-form.html',
      restrict: 'E'
    };
  });
