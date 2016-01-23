'use strict';

/**
 * @ngdoc function
 * @name sposApp.controller:ShowSessionCtrl
 * @description
 * # ShowSessionCtrl
 * Controller of the sposApp
 */
angular.module('sposApp')
  .controller('ShowSessionCtrl', function ($scope, $sce, $http, $stateParams, Session) {
      $scope.sessionKey = $stateParams.key;
      $scope.sessionId = $stateParams.id;
      $scope.session = null;
      $scope.logged = false;
      $scope.sessionStatus = "";

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
            GetSessionStatus();
        });
      };

      var GetSessionStatus = function () {
        if ($scope.session.vmConfig.IP == null && $scope.session.results == null)
          $scope.sessionStatus = $sce.trustAsHtml("<span style=\"color: #FF5722;\"> Not started </span>");

        if ($scope.session.vmConfig.IP != null && $scope.session.results == null)
          $scope.sessionStatus = $sce.trustAsHtml("<span style=\"color: #FFC107;\"> Executing </span>");

        if ($scope.session.vmConfig.IP == null && $scope.session.results != null)
          $scope.sessionStatus = $sce.trustAsHtml("<span style=\"color: #4CAF50;\"> Finished </span>");
      };

      $scope.init();
  });
