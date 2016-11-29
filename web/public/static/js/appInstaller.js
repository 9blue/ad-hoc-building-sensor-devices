(function() {
    'use strict';
    madAPP.controller('appInstallerController', appInstallerController);

    function appInstallerController($timeout, $scope) {
        var vm = this;
        var dbRef = firebase.database().ref();
        var appsRef = dbRef.child('apps');
        var lookUp = dbRef.child('app_ids');

        vm.showEdit = true;
        vm.apps = [];

        function getAppsPromise() {
            return appsRef.once('value').then(function(snapshot) {
                return snapshot;
            });
        }

        function updateInstalls() {
            getInstallsPromise(vm.selected_id).then(function(data) {
                processInstalls(data);
                $scope.$apply();
            });
        }

        function getInstallsPromise(id) {
            return lookUp.orderByChild("app_id").equalTo(id).once('value').then(function(snapshot) {
                return snapshot;
            });
        }

        function processInstalls(data) {
            vm.all_installs = [];
            data.forEach(function(child) {
                var key = child.key;
                var name = child.val().install_name;

                vm.all_installs.push({
                    id: key,
                    name: name
                });
            });
        }

        getAppsPromise().then(function(data) {
            vm.apps = data.val();
            angular.forEach(vm.apps, function(app) {
                app.components = _.keys(app.default_config);
            });
            console.log(vm.apps);
            if (!vm.selected_id) {
                vm.selected_id = _.keys(vm.apps)[0];
                vm.selected_app = vm.apps[vm.selected_id];
            }
            updateInstalls();
        });

        vm.showDetail = function(id) {
            vm.selected_id = id;
            vm.selected_app = vm.apps[vm.selected_id];
            getInstallsPromise(vm.selected_id).then(function(data) {
                processInstalls(data);
                $scope.$apply();
            });
        };

        vm.openModal = function(install) {
            if (install) {
                vm.showEdit = false;
                vm.inst_hash = install.id;
                vm.modal_title = "Installation " + install.name;
            } else {
                vm.showEdit = true;
                vm.modal_title = "Install " + vm.selected_app.app_name;
                vm.inst_hash = lookUp.push().key;
                lookUp.child(vm.inst_hash).set({
                    app_id: vm.selected_id,
                    install_name: 'New install'
                });
            }

        };

        vm.saveName = function() {
            lookUp.child(vm.inst_hash).child('install_name').set(vm.new_install_name);
            $('#install_modal').modal('hide');
            vm.new_install_name = null;
            updateInstalls();
        };

        vm.removeInstall = function(id) {
            lookUp.child(id).remove();
            dbRef.child('devices/' + id).remove();
            dbRef.child('install_actuators／' + id).remove();
            dbRef.child('install_sensors／' + id).remove();
            updateInstalls();
        };
    }
})();
