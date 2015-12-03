'use strict';

/**
 * @ngdoc directive
 * @name sposApp.directive:modelForm
 * @description
 * # Directive which includes all the form fields to choose the desired model for the execution.
 */
angular.module('sposApp')
  .directive('modelForm', function () {
    return {
      templateUrl: 'views/directives_views/model-form.html',
      restrict: 'E'
    };
  });
