(function() {
    'use strict';
    madAPP.controller('logicBuilderController', logicBuilderController);

    function logicBuilderController(sensorService, $window) {
        var lb = this;
        var dbRef = firebase.database().ref();
        var appRef = dbRef.child('apps');

        lb.sensor_list = sensorService.getSensors();
        lb.new_sensor = lb.sensor_list[0];
        lb.added_sensors = [];

        lb.actuator_list = sensorService.getActuators();
        lb.new_actuator = lb.actuator_list[0];
        lb.added_actuators = [];

        lb.addSensor = function() {
            lb.new_sensor.disable = true;
            lb.added_sensors.push(lb.new_sensor);
            console.log(lb.new_sensor);
            console.log(lb.sensor_list);
        };

        lb.addActuator = function() {
            lb.new_actuator.disable = true;
            lb.added_actuators.push(lb.new_actuator);
        };

        lb.removeSensor = function(idx) {
            lb.added_sensors[idx].disable = false;
            lb.added_sensors.splice(idx, 1);
            console.log(idx);
            console.log(lb.sensor_list);
            console.log(lb.added_sensors[idx]);
        };

        lb.removeActuator = function(idx) {
            lb.added_actuators[idx].disable = false;
            lb.added_actuators.splice(idx, 1);
            console.log(idx);
        };

        lb.createdApp = function() {
            if (!lb.new_app.default_config) {
                alert("Please add atleast one sensor");
                return;
            }
            lb.new_app.created_at = new Date().toLocaleString();
            var newApp = appRef.push(lb.new_app, function(err) {
                if (err) {
                    console.log(err);
                } else {
                    $window.location.hash = "";
                }
            });
        };
    }
})();
