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

        lb.createdApp = function() {
            console.log(lb.new_app);
        };
        lb.modalTemplate = "template/thresholdInfo.html";

        var dbRef = firebase.database().ref();
        var appRef = dbRef.child('apps');

        var organizations = dbRef.child('organizations');
        var apps = dbRef.child('applications');
        lb.organizations = [];

        lb.selectedOrg = null;


        lb.id = $stateParams.id;
        lb.text = 'This is app installer!';
        lb.choices = [{ id: 'choice1' }];

        lb.addNewChoice = function() {
            var newItemNo = lb.choices.length + 1;
            lb.choices.push({ 'id': 'choice' + newItemNo });
        };

        lb.removeChoice = function() {
            if (lb.choices.length == 1) {
                return;
            }
            var lastItem = lb.choices.length - 1;
            lb.choices.splice(lastItem);
        };


    }
})();
