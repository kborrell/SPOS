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
      $scope.shortResults = "";
      $scope.fullResults = "";

      $scope.init = function () {
        ClearSession();
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


              $http.post('http://127.0.0.1:8080/session/' + $scope.sessionId + "/getFile?key=" + $scope.sessionKey, "")
                .success(function (rawData, status) {
                  var files = rawData.split("^");
                  for (var i=0; i<files.length; i++){
                    var file = {name: "", content: ""};
                    var fileData = files[i].split('@');
                    file.name = fileData[0];
                    file.content = fileData[1];
                    $scope.files.push(file);
                  }

                    $http.post('http://127.0.0.1:8080/session/' + $scope.sessionId + "/getResults?key=" + $scope.sessionKey, "")
                      .success(function (resultData, status) {
                        $scope.shortResults = resultData[0];
                        $scope.fullResults = resultData[1];
                        var blob = new Blob([$scope.fullResults], { type : 'text/plain' });
                        $scope.url = (window.URL || window.webkitURL).createObjectURL(blob);
                        GetSessionStatus();
                      });
                  GetSessionStatus();
                });
            }
        }).catch(function (error) {
            $scope.loginError = "Incorrect ID or key. Please try again";
        });
      };

      var GetSessionStatus = function () {
        var result;

        if ($scope.session == null)
          result = "---------";

        if ($scope.session.ip == null && $scope.shortResults == "")
          result = $sce.trustAsHtml("<span style=\"color: #ff7f02;\"> Preparing </span>");

        if ($scope.session.ip != null && $scope.shortResults == "")
          result = $sce.trustAsHtml("<span style=\"color: #FFC107;\"> Executing </span>");

        if ($scope.shortResults != "")
          result = $sce.trustAsHtml("<span style=\"color: #4CAF50;\"> Finished </span>");

        if ($scope.fullResults != "" && ($scope.fullResults.toLowerCase().indexOf("error") != -1
          || $scope.fullResults.toLowerCase().indexOf("fatal") != -1)) {

          result = $sce.trustAsHtml("<span style=\"color: #ff0011;\"> Error </span>");
          $scope.shortResults = "An error has occurred during the execution. Please download the full result to check the solver output";
        }

        $scope.sessionStatus = result;
      };

      var ClearSession = function () {
        $scope.sessionKey = $stateParams.key;
        $scope.sessionId = $stateParams.id;
        $scope.session = null;
        $scope.logged = false;
        $scope.loginError = "";
        $scope.sessionStatus = "---------";
        $scope.files = [];
        $scope.shortResults = "";
        $scope.fullResults = "";
      };

      $scope.refresh = function () {
        var $icon = document.getElementsByClassName("icon-refresh"),
          animateClass = "icon-refresh-animate";
        ClearSession();
        GetSession();
        $(".icon-refresh").addClass( animateClass );
        window.setTimeout( function() {
          $(".icon-refresh").removeClass( animateClass );
        }, 2000 );
      };

      $scope.init();
  })
  .config(['$compileProvider',
    function ($compileProvider) {
      $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|ftp|mailto|tel|file|blob):/);
    }]);
