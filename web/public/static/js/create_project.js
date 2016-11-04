(function() {
    'use strict';

    madAPP.controller('createProjController', createProjController);

    function createProjController() {
        var vm = this;
        this.text = 'Firebase rulez!';
        var qr_url = 'https://chart.googleapis.com/chart?cht=qr&chs=300x300&chl=';

        this.createProject = function(){
            vm.qr_src = qr_url + "Hello, world";
        };
    }
})();
