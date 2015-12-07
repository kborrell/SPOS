angular.module('sposApp')
  .directive('sessionSummary', function () {
    return {
      templateUrl: 'views/directives_views/session-summary.html',
      restrict: 'E'
    };
  });
