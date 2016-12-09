(function() {
    'use strict';

    madAPP.controller('demoQueueController', demoQueueController);

    function demoQueueController($timeout) {
        var vm = this;

        var dbRef = firebase.database().ref();
        var appid = "-KXx95FbeR_907E1GxQe";
        vm.app_config = null;
        var instances = null;
        var appRef = dbRef.child('apps').child(appid);
        var install_sensors = dbRef.child('install_sensors');
        var install_actuators = dbRef.child('install_actuators');
        var ACTIVATION_VAL = 2;

        vm.light_reading = null;

        appRef.once('value').then(function(snapshot) {
            vm.app_config = snapshot.val();

            instances = _.keys(vm.app_config.instances);
        });

        var app_idsRef = dbRef.child('app_ids');

        var colorMap = ['white', 'green', 'yellow', 'brown','red'];

        app_idsRef.orderByChild("app_id").equalTo(appid).on('child_added', function(data) {
            console.log('new_instance', data.key);
            var new_instance = data.key;
            var install_sensorRef = install_sensors.child(new_instance);
            var install_actuatorRef = install_actuators.child(new_instance);
            var actuator_id = null;
            install_actuatorRef.child('screen').once("value").then(function(snapshot) {
                actuator_id = _.keys(snapshot.val())[0];
                console.log('flash', actuator_id);
            });

            install_sensorRef.child('camera').on('value', function(snapshot) {
                $timeout(function() {
                    // var accelerometers = _.keys(snapshot.val());
                    var deviceid = _.keys(snapshot.val())[0];
                    var num_faces = snapshot.val()[deviceid].value;
                    console.log(num_faces);

                    if (num_faces > 4) {
                        install_actuatorRef.child('screen').child(actuator_id).child('display/display_color').set('red');
                    } else {
                        install_actuatorRef.child('screen').child(actuator_id).child('display/display_color').set(colorMap[num_faces]);
                    }
                }, 0);
            });
        });
    }
})();
