(function() {
    'use strict';
    madAPP.service('sensorService', sensorService);

    function sensorService($stateParams) {
        var ss = this;
        var sensor_list = [{
            name: 'light',
            label: 'Light Sensor',
            disable: false,
            tpl_url: '/template/sensors_tpl/light.html',
            config: {
                type: 'LIGHT',
                desc: '',
                sampling_rate: 10,
                threshold_lower: true,
                threshold_upper: true
            }
        }, {
            name: 'accelerator',
            label: 'Accelerator',
            disable: false,
            tpl_url: '/template/sensors_tpl/accelerator.html',
            config: {
                type: 'ACCELERATOR',
                desc: '',
                sampling_rate: 20,
                threshold_lower: true,
                threshold_upper: true
            }
        }, {
            name: 'camera',
            label: 'Camera',
            disable: false,
            tpl_url: '/template/sensors_tpl/camera.html',
            config: {
                type: 'CAMERA',
                desc: '',
                sampling_rate: 30,
                threshold_lower: true,
                threshold_upper: true
            }
        }];

        var acturator_list = [{
            name: 'flash',
            label: 'Flash',
            disable: false,
            tpl_url: '/template/acturator_tpl/flash.html',
            config: {
                type: 'FLASH',
                desc: ''
            }
        }, {
            name: 'screen',
            label: 'Screen',
            disable: false,
            tpl_url: '/template/acturator_tpl/screen.html',
            config: {
                type: 'SCREEN',
                desc: ''
            }
        }, {
            name: 'speaker',
            label: 'Speaker',
            disable: false,
            tpl_url: '/template/acturator_tpl/speaker.html',
            config: {
                type: 'SPEAKER',
                desc: ''
            }
        }];

        ss.getActuators = function() {
            return angular.copy(acturator_list);
        };

        ss.getSensors = function() {
            return angular.copy(sensor_list);
        };
    }
})();
