(function() {
    'use strict';
    madAPP.controller('appInstallerController', appInstallerController);

    function appInstallerController() {
        var vm = this;
        vm.apps = ['app1', 'app2', 'app3']; // Retrieve that from firebase

        vm.selected_app = vm.apps[0];
        vm.showDetail = function(app){
            vm.selected_app = app;
        };

        vm.installApp = function(){
            // logic to be decided here
        };
    }
})();
