(function() {
    'use strict';

    adhocAPP.controller('mainController', mainController);

    function mainController($timeout) {
        var vm = this;
        vm.modal_template = "template/_add_app.html";
        var dbRef = firebase.database().ref();
        var organizations = dbRef.child('organizations');
        var apps = dbRef.child('applications');
        vm.organizations = [];

        vm.selected_org = null;
        // vm.apps = [];
        organizations.on('child_added', function(snapshot) {
            $timeout(function() {
                if (!vm.selected_org) {
                    vm.selected_org = snapshot.val();
                }
                var tmp = snapshot.key;
                vm.organizations.push(snapshot.val());
            }, 0);
        });

        vm.select_org = function(org) {
            vm.selected_org = org;
            // for (var k in org.applications) {
            //     vm.apps.push(org.applications[k]);
            // }
            // console.log(vm.apps);
        };

        vm.add_app = function() {
            if (!vm.selected_org) {
                alert("Please select organizations.");
                return;
            }
            var new_app = apps.push({
                'name': vm.app_name,
                'devices': true,
                'dev_num': vm.devices_num,
                'owner': vm.selected_org.id
            }, function(err) {
                if (err) {
                    console.log(err);
                }
            });
            var key = new_app.key;
            organizations.child(vm.selected_org.id).child('applications/' + new_app.key).set(vm.app_name);
            $('#add_app').modal('hide');
        };
    }
})();
