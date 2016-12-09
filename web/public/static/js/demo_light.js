(function() {
    'use strict';

    madAPP.controller('demoLightController', demoLightController);

    function demoLightController($timeout) {
        var vm = this;
        var dbRef = firebase.database().ref();
        var appid = "-KXwk9l34wEwHLVKZ6fr";
        vm.app_config = null;
        var instances = null;
        var appRef = dbRef.child('apps').child(appid);
        var install_sensors = dbRef.child('install_sensors');
        var install_actuators = dbRef.child('install_actuators');

        vm.light_reading = null;
        vm.flash_on = null;

        appRef.once('value').then(function(snapshot) {
            vm.app_config = snapshot.val();

            instances = _.keys(vm.app_config.instances);
        });

        var app_idsRef = dbRef.child('app_ids');

        app_idsRef.orderByChild("app_id").equalTo(appid).on('child_added', function(data) {
            console.log('new_instance', data.key);
            var new_instance = data.key;
            var install_sensorRef = install_sensors.child(new_instance);
            var install_actuatorRef = install_actuators.child(new_instance);
            var actuator = null;
            install_actuatorRef.child('flash').once("value").then(function(snapshot) {
                actuator = _.keys(snapshot.val())[0];
                console.log('flash', actuator);
            });

            var threshold = null;
            var light_deviceid = null;
            dbRef.child('devices').child(new_instance).orderByKey().once('value').then(function(data){
                console.log(data.val());
                var configs = data.val();
                for (var key in configs) {
                    if(configs[key].config.light){
                        threshold = parseFloat(configs[key].config.light.threshold);
                        light_deviceid = key;
                    }
                }
            });

            install_sensorRef.child('light').on('value', function(snapshot) {
                $timeout(function() {
                    console.log(snapshot.val());
                    if(light_deviceid){
                        vm.light_reading = snapshot.val()[light_deviceid].value;
                    }
                    console.log(vm.light_reading);
                    console.log(threshold);
                    if (vm.light_reading < threshold) {
                        vm.flash_on = true;
                        install_actuatorRef.child('flash').child(actuator).set(true);
                    } else {
                        vm.flash_on = false;
                        install_actuatorRef.child('flash').child(actuator).set(false);
                    }
                }, 0);
            });
        });
    }
})();
