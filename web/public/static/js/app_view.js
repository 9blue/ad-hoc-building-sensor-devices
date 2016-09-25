(function() {
    'use strict';

    adhocAPP.controller('appViewController', appViewController);

    function appViewController($stateParams, $timeout) {
        var vm = this;
        vm.id = $stateParams.id;
        vm.text = 'Firebase rulez!';
        vm.add_device_template = "template/_add_device.html";

        var dbRef = firebase.database().ref('applications');
        var app = dbRef.child(vm.id);

        vm.cur_app = null;
        app.on('value', function(snapshot) {
            $timeout(function() {
                vm.cur_app = snapshot.val();
                console.log(vm.cur_app)
            }, 0);
        });


    }
})();
