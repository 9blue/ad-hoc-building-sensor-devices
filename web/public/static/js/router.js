(function() {
    'use strict';

    adhocAPP.config(function($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.otherwise('/home');

        $stateProvider
        // main view
        .state('home', {
            url: '/home',
            templateUrl: '/template/main.html',
            controller: 'mainController',
            controllerAs: 'mc'
        })

        // configuration view
        .state('config', {
            url: '/config',
            templateUrl: '/template/config.html',
            controller: 'configController',
            controllerAs: 'cc'
        })

        // project template view
        .state('app', {
            url: '/app/:id',
            templateUrl: '/template/app_view.html',
            controller: 'appViewController',
            controllerAs: 'av'
        })

        .state('create_project', {
            url: '/create_project',
            templateUrl: '/template/config.html',
            controller: 'createProjController',
            controllerAs: 'cp'
        });

    });
})();
