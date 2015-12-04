'use strict';

/**
 * @ngdoc directive
 * @name sposApp.directive:methodForm
 * @description
 * # Directive which includes all the form fields to choose the desired method used during the execution.
 */
angular.module('sposApp')
  .directive("fileread", [function () {
    return {
      scope: {
        fileread: "="
      },
      link: function (scope, element, attributes) {
        element.bind("change", function (changeEvent) {
          var reader = new FileReader();
          reader.onload = function (loadEvent) {
            scope.$apply(function () {
              scope.fileread = new Uint8Array(loadEvent.target.result);
            });
          };
          reader.readAsBinaryString(changeEvent.target.files[0]);
        });
      }
    }
  }]);
