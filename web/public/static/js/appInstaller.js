(function() {
    'use strict';
    madAPP.controller('appInstallerController', appInstallerController);

    function appInstallerController($timeout) {
        var vm = this;
        var dbRef = firebase.database().ref();
        var appsRef = dbRef.child('apps');
        var lookUp = dbRef.child('app_ids');

        //vm.apps = ['app1', 'app2', 'app3']; // Retrieve that from firebase
        vm.apps = [];

        appsRef.once('value').then(function(snapshot) {
            $timeout(function() {
                vm.apps = snapshot.val();
                if (!vm.selected_id) {
                    vm.selected_id = _.keys(vm.apps)[0];
                    vm.selected_app = vm.apps[vm.selected_id];
                }
            }, 0);
        });


        vm.showDetail = function(id) {
            vm.selected_id = id;
            vm.selected_app = vm.apps[vm.selected_id];
        };

        vm.installApp = function() {
            vm.inst_hash = lookUp.push().key;
            lookUp.child(vm.inst_hash).set(vm.selected_id);
            // logic to be decided here
        };
    }
})();
