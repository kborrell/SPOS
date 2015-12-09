'use strict';

/**
 * @ngdoc overview
 * @name sposApp
 * @description
 * # sposApp
 *
 * Main module of the application.
 */
angular
  .module('sposApp', [
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngTouch'
  ])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl',
        controllerAs: 'main'
      })
      .when('/session/new', {
        templateUrl: 'views/create_session.html',
        controller: 'CreatesessionCtrl',
        controllerAs: 'createsession'
      })
      .when('/session', {
        templateUrl: 'views/session-access.html',
        controller: 'ShowSessionCtrl',
        controllerAs: 'showsession'
      })
      .when('/session/:id?key', {
        templateUrl: 'views/show-session.html',
        controller: 'ShowSessionCtrl',
        controllerAs: 'showsession'
      })
      .when('/about', {
        templateUrl: 'views/about.html',
        controller: 'AboutCtrl',
        controllerAs: 'about'
      })
      .when('/contact', {
        templateUrl: 'views/contact.html',
        controller: 'ContactCrl',
        controllerAs: 'contact'
      })
      .otherwise({
        redirectTo: '/'
      });
  });
