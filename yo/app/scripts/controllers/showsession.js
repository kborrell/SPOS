'use strict';

/**
 * @ngdoc function
 * @name sposApp.controller:ShowSessionCtrl
 * @description
 * # ShowSessionCtrl
 * Controller of the sposApp
 */
angular.module('sposApp')
  .controller('ShowSessionCtrl', function ($scope, $http, $stateParams, Session) {
      $scope.sessionKey = $stateParams.key;
      $scope.sessionId = $stateParams.id;
      $scope.session = null;
      $scope.logged = false;

      $scope.init = function () {
        $scope.logged = $scope.sessionKey && $scope.sessionId;
        if ($scope.logged)
          GetSession();
      };

      $scope.logInSession = function(){
        $scope.logged = true;
        GetSession();
      };

      var GetSession = function () {
        Session.query({id: $scope.sessionId, key: $scope.sessionKey})
          .$promise.then(function (session) {
            $scope.session = session;
        });
      };

      $scope.init();
  });
