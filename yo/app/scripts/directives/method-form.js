'use strict';

/**
 * @ngdoc directive
 * @name sposApp.directive:methodForm
 * @description
 * # Directive which includes all the form fields to choose the desired method used during the execution.
 */
angular.module('sposApp')
  .directive('methodForm', function () {
    return {
      templateUrl: 'views/directives_views/method-form.html',
      restrict: 'E'
    };
  });
