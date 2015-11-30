'use strict';

/**
 * @ngdoc directive
 * @name yoApp.directive:vmConfigForm
 * @description
 * # Directive which includes all the form fields to choose the desired configuration for the VM.
 */
angular.module('yoApp')
  .directive('vmConfigForm', function () {
    return {
      templateUrl: 'views/directives_views/vm-config-form.html',
      restrict: 'E'
    };
  });
