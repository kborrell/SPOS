'use strict';

/**
 * @ngdoc service
 * @name sposApp.virtualmachine
 * @description
 * # virtualmachine
 * Service in the sposApp.
 */
angular.module('sposApp')
  .factory('Parameters', ['$resource', function($resource) {
    return $resource('http://127.0.0.1:8080/parameters/:id', null,
      {
        'query': { method:'GET', isArray: false },
        'update': { method:'PUT' }
      });
  }]);
