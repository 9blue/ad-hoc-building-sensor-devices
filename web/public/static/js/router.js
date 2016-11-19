(function() {
    'use strict';

    madAPP.config(function($stateProvider, $urlRouterProvider) {
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
        .state('logic_builder', {
            url: '/logic_builder',
            templateUrl: '/template/logicBuilder.html',
            controller: 'logicBuilderController',
            controllerAs: 'lb'
        })

        .state('app_installer', {
            url: '/app_installer',
            templateUrl: '/template/appInstaller.html',
            controller: 'appInstallerController',
            controllerAs: 'ai'
        })

        // project template view
        // .state('app', {
        //     url: '/app/:id',
        //     templateUrl: '/template/app_view.html',
        //     controller: 'appViewController',
        //     controllerAs: 'av'

        .state('demo', {
            url: '/demo',
            templateUrl: '/template/demo.html',
            controller: 'demoViewController',
            controllerAs: 'dv'
        });

    });
})();
