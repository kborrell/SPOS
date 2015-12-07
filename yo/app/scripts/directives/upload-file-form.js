angular.module('sposApp')
  .directive('uploadFileForm', function () {
    return {
      templateUrl: 'views/directives_views/upload-file-form.html',
      restrict: 'E'
    };
  });
