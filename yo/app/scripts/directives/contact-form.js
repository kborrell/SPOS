angular.module('sposApp')
  .directive('contactForm', function () {
    return {
      templateUrl: 'views/directives_views/contact-form.html',
      restrict: 'E'
    };
  });
