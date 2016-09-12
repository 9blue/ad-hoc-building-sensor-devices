(function() {
    'use strict';

    angular
        .module('datacenter', [])
        .controller('MainController', MainController);

    function MainController($timeout) {
        var vm = this;
        var dbRef = firebase.database().ref();
        var device_list = dbRef.child('connected_devices');
        vm.devices = [];

        device_list.on('value', function(snapshot) {
            $timeout(function() {
                var tmp = snapshot.val();
                var holder = [];
                for (var d in tmp) {
                    holder.push(tmp[d]);
                }
                vm.devices = holder;
                console.log(vm.devices);
            }, 0);
        });
        vm.text = 'Firebase rulez!';
    }
})();
