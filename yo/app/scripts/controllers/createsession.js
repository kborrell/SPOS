'use strict';

/**
 * @ngdoc function
 * @name sposApp.controller:CreatesessionCtrl
 * @description
 * # CreatesessionCtrl
 * Controller of the sposApp
 */

angular.module('sposApp')
    .controller('CreateSessionCtrl', function ($scope, $q, $http, $location, VirtualMachine, Parameters, Session, fileReader, ModelInfo, MethodInfo) {
        $scope.vmConfig = {};
        $scope.parameters = {};
        $scope.session = {};

        $scope.location = $location;
        $scope.predefinedVM = "";
        $scope.firstStepActive = true;
        $scope.uploadMessage = "";
        $scope.sessionCreated = false;
        $scope.selectedModel = "";
        $scope.selectedMethod = "";

        $scope.sessionKey = "";
        $scope.sessionId = "";

        $scope.clearPredefinedVM = function () {
            $scope.predefinedVMForm.$setPristine();
            $scope.predefinedVMForm.$setValidity();
            $scope.predefinedVMForm.$setUntouched();
            $scope.predefinedVM = "";
        };

        $scope.clearVMConfig = function () {
            $scope.vmConfig = {};
            $scope.vmConfigForm.$setPristine();
            $scope.vmConfigForm.$setValidity();
            $scope.vmConfigForm.$setUntouched();
        };

        $scope.getCompatibleMethods = function () {
            ModelInfo.query({action: 'search', search: 'findByModel', modelName: $scope.selectedModel})
                .$promise.then(function (modelResponse) {
                $scope.parameters.model = modelResponse._embedded.models[0];
            });
        };

        $scope.loadMethod = function () {
            MethodInfo.query({action: 'search', search: 'findByMethod', methodName: $scope.selectedMethod})
                .$promise.then(function (methodResponse) {
                $scope.parameters.method = methodResponse._embedded.methods[0];
            });
        };

        $scope.createSession = function () {
            createSession();
            $scope.sessionCreated = true;
        };

        $scope.completeFirstStep = function () {
            $scope.firstStepActive = false;
            createVMConfig();
        };

        $scope.getFile = function () {
            fileReader.readAsText($scope.file, $scope)
                .then(function (result) {
                    $scope.parameters.infoFileContent = result;
                });
            $scope.parameters.infoFileName = $scope.file.name;
        };

        var getPredefinedVM = function () {
            var id = -1;
            switch ($scope.predefinedVM) {
                case "High":
                    id = 3;
                    break;
                case "Medium":
                    id = 2;
                    break;
                case "Low":
                    id = 1;
                    break;
            }
            $scope.session.vmConfig = VirtualMachine.query({id: id});
        };

        var createVMConfig = function () {
            if ($scope.predefinedVM === "") {
                $scope.vmConfig.realPercentage = $scope.vmConfig.realPercentage / 100;
                $http.post('http://127.0.0.1:8080/virtualmachine', $scope.vmConfig)
                    .success(function (data, status, headers, config) {
                        VirtualMachine.get({id: headers('Location').split('/').pop()}).
                        $promise.then(function (vm) {
                            $scope.session.vmConfig = vm;
                        });

                    });
                $scope.clearVMConfig();
            } else {
                getPredefinedVM();
            }
        };

        var createParameters = function () {
            Parameters.save($scope.parameters);
        };

        var createSession = function () {
            $scope.session.info = $scope.parameters;
            if ($scope.session.type == 'Optimal') {
                $scope.session.maximumDuration = -1;
            }
            Session.save($scope.session).$promise.then(function (session) {
                $scope.sessionId = session.id;
                $scope.sessionKey = session.key;
            }).catch(function (error) {
                //TODO: Show error page
            });
        };
    });
