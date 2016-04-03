'use strict';

/**
 * @ngdoc service
 * @name sposApp.factories
 * @description
 * # factories
 * Service in the sposApp.
 */
angular.module('sposApp')
    .factory('ModelInfo', ['$resource', function($resource) {
      return $resource('/models/:action/:search/:id', null,
          {
            'query': { method:'GET', isArray: false },
            'update': { method:'PUT' }
          });
    }]);

angular.module('sposApp')
    .factory('MethodInfo', ['$resource', function($resource) {
        return $resource('/methods/:action/:search/:id', null,
            {
                'query': { method:'GET', isArray: false },
                'update': { method:'PUT' }
            });
    }]);

angular.module('sposApp')
    .factory('Parameters', ['$resource', function($resource) {
        return $resource('/parameters/:id', null,
            {
                'query': { method:'GET', isArray: false },
                'update': { method:'PUT' }
            });
    }]);

angular.module('sposApp')
    .factory('Session', ['$resource', function($resource) {
        return $resource('/session/:id', null,
            {
                'query': { method:'GET', isArray: false },
                'update': { method:'PUT' }
            });
    }]);

angular.module('sposApp')
    .factory('VirtualMachine', ['$resource', function($resource) {
        return $resource('/virtualmachine/:id', null,
            {
                'query': { method:'GET', isArray: false },
                'update': { method:'PUT' }
            });
    }]);


