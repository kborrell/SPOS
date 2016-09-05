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
      return $resource('http://193.144.12.55/models/:action/:search/:id', null,
          {
            'query': { method:'GET', isArray: false },
            'update': { method:'PUT' }
          });
    }]);

angular.module('sposApp')
    .factory('MethodInfo', ['$resource', function($resource) {
        return $resource('http://193.144.12.55/methods/:action/:search/:id', null,
            {
                'query': { method:'GET', isArray: false },
                'update': { method:'PUT' }
            });
    }]);

angular.module('sposApp')
    .factory('Parameters', ['$resource', function($resource) {
        return $resource('http://193.144.12.55/parameters/:id', null,
            {
                'query': { method:'GET', isArray: false },
                'update': { method:'PUT' }
            });
    }]);

angular.module('sposApp')
    .factory('Session', ['$resource', function($resource) {
        return $resource('http://193.144.12.55/session/:id', null,
            {
                'query': { method:'GET', isArray: false },
                'update': { method:'PUT' }
            });
    }]);

angular.module('sposApp')
    .factory('VirtualMachine', ['$resource', function($resource) {
        return $resource('http://193.144.12.55/virtualmachine/:id', null,
            {
                'query': { method:'GET', isArray: false },
                'update': { method:'PUT' }
            });
    }]);


