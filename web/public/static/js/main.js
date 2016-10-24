(function() {
    'use strict';

    adhocAPP.controller('mainController', mainController);

    function mainController($timeout) {
        var vm = this;
        vm.modalTemplate = "template/_add_app.html";
        var dbRef = firebase.database().ref();
        var organizations = dbRef.child('organizations');
        var apps = dbRef.child('applications');
        vm.organizations = [];

        vm.selectedOrg = null;
        // vm.apps = [];
        organizations.once('value').then(function(snapshot) {
            $timeout(function() {
                if (!vm.selectedOrg) {
                    var orgs = snapshot.val();
                    for (var k in orgs) {
                        vm.organizations.push(orgs[k]);
                    }
                    vm.selectedOrg = vm.organizations[0];
                }
                console.log(vm.organizations);
            }, 0);
        });


        vm.selectOrg = function(org) {
            vm.selectedOrg = org;
            // for (var k in org.applications) {
            //     vm.apps.push(org.applications[k]);
            // }
            // console.log(vm.apps);
        };

        vm.addApp = function() {
            if (!vm.selectedOrg) {
                alert("Please select organizations.");
                return;
            }
            var newApp = apps.push({
                'name': vm.appName,
                'devices': {},
                'devNum': vm.devNum,
                'owner': vm.selectedOrg.id
            }, function(err) {
                if (err) {
                    console.log(err);
                }
            });
            var key = newApp.key;
            organizations.child(vm.selectedOrg.id).child('applications/' + newApp.key).set(vm.appName);
            vm.selectedOrg.applications[newApp.key] = vm.appName;
            $('#add_app').modal('hide');

            vm.appName = null;
            vm.devNum = null;
        };
    }
})();
