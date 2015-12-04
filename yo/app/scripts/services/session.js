'use strict';

/**
 * @ngdoc service
 * @name sposApp.virtualmachine
 * @description
 * # virtualmachine
 * Service in the sposApp.
 */
angular.module('sposApp')
  .factory('Session', ['$resource', function($resource) {
    return $resource('http://127.0.0.1:8080/session/:id', null,
      {
        'query': { method:'GET', isArray: false },
        'update': { method:'PUT' }
      });
  }]);
