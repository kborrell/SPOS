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

angular.module('sposApp').directive('uploadFileForm', function () {
    return {
      templateUrl: 'views/directives_views/upload-file-form.html',
      restrict: 'E'
    };
  });
