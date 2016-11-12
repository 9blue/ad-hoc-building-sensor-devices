(function() {
    'use strict';
    madAPP.controller('logicBuilderController', logicBuilderController);

    function logicBuilderController(sensorService, $window) {
        var lb = this;
        var dbRef = firebase.database().ref();
        var appRef = dbRef.child('apps');

        lb.sensor_types = ["light", "camera", "sound"];
        lb.added_sensors = [];

        lb.sensor_list = sensorService.getSensors();

        lb.new_sensor = lb.sensor_list[0];

        lb.addSensor = function() {
            lb.added_sensors.push(lb.new_sensor);
            console.log(lb.new_sensor);
        };

        lb.acturator_list = sensorService.getActurators();
        lb.new_acturator = lb.acturator_list[0];

        lb.addActurator = function() {
            lb.added_acturator = lb.new_acturator.tpl_url;
        };

        lb.createdApp = function() {
            if (!lb.new_app.default_config) {
                alert("Please add atleast one sensor");
                return;
            }
            lb.new_app.created_at = new Date().toLocaleString();
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