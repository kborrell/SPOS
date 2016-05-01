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
    'ngTouch',
    'ui.router',
    'hm.readmore'
  ])
  .config(function ($stateProvider) {
    $stateProvider
      .state('home', {
        url: '/',
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
      })
      .state('createSession', {
        url: '/session/new',
        templateUrl: 'views/create_session.html',
        controller: 'CreateSessionCtrl'
      })
      .state('viewSession', {
        url: '/session/view/:id?key',
        templateUrl: 'views/show-session.html',
        controller: 'ShowSessionCtrl'
      })
      .state('contact', {
        url: '/contact',
        templateUrl: 'views/contact.html',
        controller: 'ContactCtrl'
      })
      .state('about', {
        url: '/about',
        templateUrl: 'views/about.html',
        controller: 'AboutCtrl'
      })
  })
      .run(function ($state) {
        $state.go('home');
      });
