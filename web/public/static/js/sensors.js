(function() {
    'use strict';
    madAPP.service('sensorService', sensorService);

    function sensorService($stateParams) {
        var ss = this;
        var sensor_list = [
            { name: 'light', label: 'Light Sensor', type: 'dark', tpl_url: '/template/sensors_tpl/light.html' },
            { name: 'sound', label: 'Sound Sensor', type: 'light', tpl_url: '/template/sensors_tpl/sound.html' },
            { name: 'camera', label: 'Camera', type: 'dark', tpl_url: '/template/sensors_tpl/camera.html' },
        ];

        var acturator_list = [
            { name: 'flash', label: 'Flash', tpl_url: '/template/acturator_tpl/camera.html' },
            { name: 'screen_light', label: 'Screen Light', tpl_url: '/template/acturator_tpl/camera.html' }
        ];

        ss.getActurators = function() {
            return acturator_list;
        };

        ss.getSensors = function() {
            return sensor_list; };
    }
})();
