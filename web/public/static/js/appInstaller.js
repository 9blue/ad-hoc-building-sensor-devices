(function() {
    'use strict';
adhocAPP.controller('appInstallerController', appInstallerController);

    function appInstallerController($stateParams) {
        var vm = this;
        vm.id = $stateParams.id;
        vm.text = 'test text!';
    }
})();

