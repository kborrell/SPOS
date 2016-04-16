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
      return $resource('http://127.0.0.1:8080/models/:action/:search/:id', null,
          {
            'query': { method:'GET', isArray: false },
            'update': { method:'PUT' }
          });
    }]);

angular.module('sposApp')
    .factory('MethodInfo', ['$resource', function($resource) {
        return $resource('http://127.0.0.1:8080/methods/:action/:search/:id', null,
            {
                'query': { method:'GET', isArray: false },
                'update': { method:'PUT' }
            });
    }]);

angular.module('sposApp')
    .factory('Parameters', ['$resource', function($resource) {
        return $resource('http://127.0.0.1:8080/parameters/:id', null,
            {
                'query': { method:'GET', isArray: false },
                'update': { method:'PUT' }
            });
    }]);

angular.module('sposApp')
    .factory('Session', ['$resource', function($resource) {
        return $resource('http://127.0.0.1:8080/session/:id', null,
            {
                'query': { method:'GET', isArray: false },
                'update': { method:'PUT' }
            });
    }]);

angular.module('sposApp')
    .factory('VirtualMachine', ['$resource', function($resource) {
        return $resource('http://127.0.0.1:8080/virtualmachine/:id', null,
            {
                'query': { method:'GET', isArray: false },
                'update': { method:'PUT' }
            });
    }]);


