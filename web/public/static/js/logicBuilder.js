(function() {
    'use strict';

    madAPP.controller('logicBuilderController', logicBuilderController);

    function logicBuilderController($stateParams) {
        var lb = this;

        lb.added_sensors = [];

        lb.sensor_list = [
            { name: 'light', label: 'Light Sensor', type: 'dark' },
            { name: 'sound', label: 'Sound Sensor', type: 'light' },
            { name: 'camera', label: 'Camera', type: 'dark' },
        ];
        lb.new_sensor = lb.sensor_list[1];
        lb.addSensor = function() {
            lb.added_sensors.push('/template/sensors_tpl/' + lb.new_sensor.name + '.html');
            console.log(lb.new_sensor);
        };

        lb.acturator_list = [
            { name: 'flash', label: 'Flash'},
            { name: 'screen_light', label: 'Screen Light'}
        ];
        lb.new_acturator = lb.acturator_list[1];

        lb.addActurator = function(){
            lb.added_acturator = '/template/acturator_tpl/' + lb.new_acturator.name + '.html';
        }

        lb.createdApp = function() {
            var dbRef = firebase.database().ref();
            var appRef = dbRef.child('apps');
            console.log(lb.new_app);
            var newApp = appRef.push(
                lb.new_app
            , function(err) {
                if (err) {
                    console.log(err);
                }
            });
        };
        lb.modalTemplate = "template/thresholdInfo.html";
    }
})();
