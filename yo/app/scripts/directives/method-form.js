'use strict';

/**
 * @ngdoc directive
 * @name yoApp.directive:methodForm
 * @description
 * # Directive which includes all the form fields to choose the desired method used during the execution.
 */
angular.module('yoApp')
  .directive('methodForm', function () {
    return {
      templateUrl: 'views/directives_views/method-form.html',
      restrict: 'E'
    };
  });
