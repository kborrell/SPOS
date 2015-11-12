'use strict';

/**
 * @ngdoc service
 * @name yoApp.virtualmachine
 * @description
 * # virtualmachine
 * Service in the yoApp.
 */
angular.module('yoApp')
  .factory('VirtualMachine', ['$resource', function($resource) {
    return $resource('http://127.0.0.1:8080/virtualmachine/:id', null,
      {
        'query': { method:'GET', isArray: false },
        'update': { method:'PUT' }
      });
  }]);
