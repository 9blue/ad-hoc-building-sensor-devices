(function() {
    'use strict';

    adhocAPP.controller('demoViewController', demoViewController);

    function demoViewController($timeout) {
        var vm = this;
        vm.text = 'Firebase rulez!';

        var dbRef = firebase.database().ref();
        var appid = "-JglJnGDXcqLq6m844pZ";
        vm.app_config = null;
        var instances = null;
        var appRef = dbRef.child('apps').child(appid);

        // var devicesRef = dbRef.child('devices');
        var dataRef = dbRef.child('data');
        vm.light_reading = null;
        vm.flash_on = null;

        appRef.once('value').then(function(snapshot) {
            vm.app_config = snapshot.val();
            instances = _.keys(vm.app_config.instances);
        });

        appRef.child('instances').on('child_added', function(data) {
            var new_instance = data.key;
            var instanceRef = dataRef.child(new_instance);
            var actuator = null;
            instanceRef.child('flash').once("value").then(function(snapshot) {
                actuator = _.keys(snapshot.val())[0];
            });
            instanceRef.child('light').on('value', function(snapshot) {
                $timeout(function() {
                    var device_id = _.keys(snapshot.val())[0];
                    vm.light_reading = snapshot.val()[device_id];
                    if (vm.light_reading < 100) {
                        vm.flash_on = true;
                        instanceRef.child('flash').child(actuator).set(true);
                    } else {
                        vm.flash_on = false;
                        instanceRef.child('flash').child(actuator).set(false);
                    }
                }, 0);
            });
        });
    }
})();
