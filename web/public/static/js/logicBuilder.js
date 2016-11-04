(function() {
    'use strict';

    madAPP.controller('logicBuilderController', logicBuilderController);

    function logicBuilderController($stateParams) {
        var lb = this;

        lb.added_sensors = [];

        lb.sensor_list = [
            { name: 'light', label: 'Light Sensor', type: 'dark' },
            { name: 'sound', label: 'Sound Sensor', type: 'light'},
            { name: 'camera', label: 'Camera', type: 'dark' },
        ];
        lb.new_sensor = lb.sensor_list[1];
        lb.addSensor = function() {
            lb.added_sensors.push(lb.new_sensor.name + '.html');
            console.log(lb.new_sensor);
        };

        lb.modalTemplate = "template/thresholdInfo.html";




        var dbRef = firebase.database().ref();
        var organizations = dbRef.child('organizations');
        var apps = dbRef.child('applications');
        lb.organizations = [];

        lb.selectedOrg = null;

        // to be replaced by sensor list
        // organizations.once('value').then(function(snapshot) {
        //     $timeout(function() {
        //         if (!lb.selectedOrg) {
        //             var orgs = snapshot.val();
        //             for (var k in orgs) {
        //                 lb.organizations.push(orgs[k]);
        //             }
        //             lb.selectedOrg = lb.organizations[0];
        //         }
        //         console.log(lb.organizations);
        //     }, 0);
        // });

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
