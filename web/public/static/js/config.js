(function() {
    'use strict';

    madAPP.controller('configController', configController);

    function configController($stateParams) {
        var vm = this;
        vm.id = $stateParams.id;
        vm.text = 'Firebase rulez!';
    }
})();
