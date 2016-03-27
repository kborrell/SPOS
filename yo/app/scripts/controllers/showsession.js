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
      $scope.loginError = "";
      $scope.sessionStatus = "---------";
      $scope.files = [];

      $scope.init = function () {
        $scope.logged = $scope.sessionKey && $scope.sessionId;
        if ($scope.logged)
          GetSession();
      };

      $scope.logInSession = function(){
        GetSession();
      };

      var GetSession = function () {
        Session.query({id: $scope.sessionId, key: $scope.sessionKey})
          .$promise.then(function (session) {
            if (session){
              $scope.logged = true;
              $scope.session = session;


              $http.post("http://127.0.0.1:8080/session/" + $scope.sessionId + "/getFile?key=" + $scope.sessionKey, "")
                .success(function (data, status) {
                  var fileContent = JSON.parse(data);
                  for (var i=0; i<fileContent["files"].length; i++){
                    var file = {name: "", content: ""};
                    file.name = fileContent["files"][i]["name"];
                    file.content = fileContent["files"][i]["content"];
                    $scope.files.push(file);
                  }

                  $http.post("http://127.0.0.1:8080/session/" + $scope.sessionId + "/getResults?key=" + $scope.sessionKey, "")
                    .success(function (data, status) {
                      var fileContent = JSON.parse(data);
                      session.results = fileContent["results"];
                      GetSessionStatus();
                    });
                });
            }
        }).catch(function (error) {
            $scope.loginError = "Incorrect ID or key. Please try again";
        });
      };

      var GetSessionStatus = function () {
        if ($scope.session.vmConfig.ip == null && $scope.session.results == null)
          $scope.sessionStatus = $sce.trustAsHtml("<span style=\"color: #FF5722;\"> Not started </span>");

        if ($scope.session.vmConfig.ip != null)
          $scope.sessionStatus = $sce.trustAsHtml("<span style=\"color: #FFC107;\"> Executing </span>");

        if ($scope.session.results != null)
          $scope.sessionStatus = $sce.trustAsHtml("<span style=\"color: #4CAF50;\"> Finished </span>");
      };

      $scope.init();
  });
