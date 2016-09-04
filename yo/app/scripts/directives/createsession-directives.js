'use strict';


angular.module('sposApp')
  .directive('vmConfigForm', function () {
    return {
      templateUrl: 'views/directives_views/vm-config-form.html',
      restrict: 'E'
    };
  });

angular.module('sposApp')
  .directive('contactForm', function () {
    return {
      templateUrl: 'views/directives_views/contact-form.html',
      restrict: 'E'
    };
  });

angular.module('sposApp')
  .directive('methodForm', function () {
    return {
      templateUrl: 'views/directives_views/method-form.html',
      restrict: 'E'
    };
  });

angular.module('sposApp').directive('modelForm', function () {
    return {
      templateUrl: 'views/directives_views/model-form.html',
      restrict: 'E'
    };
  });

angular.module('sposApp').directive('problemForm', function () {
    return {
      templateUrl: 'views/directives_views/problem-form.html',
      restrict: 'E'
    };
  });

angular.module('sposApp').directive('sessionSummary', function () {
    return {
      templateUrl: 'views/directives_views/session-summary.html',
      restrict: 'E'
    };
  });

angular.module('sposApp').directive('solutionTypeForm', function () {
    return {
      templateUrl: 'views/directives_views/solution-type-form.html',
      restrict: 'E'
    };
  });

angular.module('sposApp').directive('solverForm', function () {
    return {
      templateUrl: 'views/directives_views/solver-form.html',
      restrict: 'E'
    };
  });

angular.module('sposApp').directive('uploadFileFormAdvanced', function () {
    return {
      templateUrl: 'views/directives_views/upload-file-form-advanced.html',
      restrict: 'E'
    };
  });

angular.module('sposApp').directive('uploadFileFormSimple', function () {
  return {
    templateUrl: 'views/directives_views/upload-file-form-simple.html',
    restrict: 'E'
  };
});

angular.module('sposApp').directive('fileModel', ['$parse', function ($parse) {
  return {
    restrict: 'A',
    link: function(scope, element, attrs) {
      var model = $parse(attrs.fileModel);
      var modelSetter = model.assign;

      element.bind('change', function(){
        scope.$apply(function(){
          modelSetter(scope, element[0].files[0]);
        });
      });
    }
  };
}]);
