'use strict';

/**
 * @ngdoc service
 * @name sposApp.modelinfo
 * @description
 * # modelinfo
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
