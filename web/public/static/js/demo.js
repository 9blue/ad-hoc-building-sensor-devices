(function() {
    'use strict';

    madAPP.controller('demoViewController', demoViewController);

    function demoViewController($timeout) {
        var vm = this;
        vm.text = 'Firebase rulez!';

        var dbRef = firebase.database().ref();
        var appid = "-JglJnGDXcqLq6m844pZ";
        vm.app_config = null;
        var instances = null;
        var appRef = dbRef.child('apps').child(appid);

        // var devicesRef = dbRef.child('devices');
        var sDataRef = dbRef.child('sensor_data');
        var aDataRef = dbRef.child('actuator_data');
        vm.light_reading = null;
        vm.flash_on = null;

        appRef.once('value').then(function(snapshot) {
            vm.app_config = snapshot.val();
            instances = _.keys(vm.app_config.instances);
        });

        appRef.child('devices').on('child_added', function(data) {
            var new_instance = data.key;
            var sRef = sDataRef.child(new_instance);
            var aRef = aDataRef.child(new_instance);
            var actuator = null;
            aRef.child('flash').once("value").then(function(snapshot) {
                actuator = _.keys(snapshot.val())[0];
            });
            sRef.child('light').on('value', function(snapshot) {
                $timeout(function() {
                    var device_id = _.keys(snapshot.val())[0];
                    vm.light_reading = _.map(_.keys(snapshot.val()), function(x){
                        return snapshot.val()[x];
                    });
                    console.log(vm.light_reading);
                    var flash = _.some(vm.light_reading, function(x){ return x < 100;});
                    console.log(flash);
                    if(flash){
                        vm.flash_on = true;
                        aRef.child('flash').child(actuator).set(true);
                    } else {
                        vm.flash_on = false;
                        aRef.child('flash').child(actuator).set(false);
                    }
                }, 0);
            });
        });
    }
})();
