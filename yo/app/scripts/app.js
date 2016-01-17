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
        controllerAs: 'main',
        activeTab: 'main'
      })
      .when('/session/new', {
        templateUrl: 'views/create_session.html',
        controller: 'CreatesessionCtrl',
        controllerAs: 'createsession',
        activeTab: 'createsession'
      })
      .when('/session', {
        templateUrl: 'views/session-access.html',
        controller: 'ShowSessionCtrl',
        controllerAs: 'showsession',
        activeTab: 'showsession'
      })
      .when('/session/:id?key', {
        templateUrl: 'views/show-session.html',
        controller: 'ShowSessionCtrl',
        controllerAs: 'showsession',
        activeTab: 'showsession'
      })
      .when('/about', {
        templateUrl: 'views/about.html',
        controller: 'AboutCtrl',
        controllerAs: 'about',
        activeTab: 'about'
      })
      .when('/contact', {
        templateUrl: 'views/contact.html',
        controller: 'ContactCrl',
        controllerAs: 'contact',
        activeTab: 'contact'
      })
      .otherwise({
        redirectTo: '/'
      });
  });
