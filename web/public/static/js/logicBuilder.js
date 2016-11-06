(function() {
    'use strict';
    madAPP.controller('logicBuilderController', logicBuilderController);

    function logicBuilderController(sensorService, $window) {
        var lb = this;
        var dbRef = firebase.database().ref();
        var appRef = dbRef.child('apps');

        lb.added_sensors = [];

        lb.sensor_list = sensorService.getSensors();

        lb.new_sensor = lb.sensor_list[0];
        lb.addSensor = function() {
            lb.added_sensors.push(lb.new_sensor.tpl_url);
            console.log(lb.new_sensor);
        };

        lb.acturator_list = sensorService.getActurators();
        lb.new_acturator = lb.acturator_list[0];

        lb.addActurator = function() {
            lb.added_acturator = lb.new_acturator.tpl_url;
        };

        lb.createdApp = function() {
            var newApp = appRef.push(
                lb.new_app,
                function(err) {
                    if (err) {
                        console.log(err);
                    } else {
                        $window.location.hash = "";
                    }
                });
        };
    }
})();
