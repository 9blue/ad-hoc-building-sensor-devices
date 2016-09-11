(function() {
    'use strict';

    angular
        .module('datacenter', [])
        .controller('MainController', MainController);

    function MainController() {
        var vm = this;
        vm.dbRef = firebase.database().ref();
        vm.repos = [];


        vm.text = "Firebase rulez!";
    }
})();
