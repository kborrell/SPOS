'use strict';

angular.module('sposApp')
  .directive("ngFileSelect", function () {
    return {
      link: function($scope,el){
        el.bind("change", function(e){
          $scope.file = (e.srcElement || e.target).files[0];
          if ((e.srcElement || e.target).accept.split(".").pop() == $scope.file.name.split(".").pop()){
            $scope.getFile();
          } else {
            $scope.errorFile = "Invalid Extension. Try again.";
          }
        })
      }
    }
  });
