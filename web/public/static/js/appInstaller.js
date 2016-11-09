(function() {
    'use strict';
    madAPP.controller('appInstallerController', appInstallerController);

    function appInstallerController($timeout) {
        var ai = this;
        var dbRef = firebase.database().ref();
        var appsRef = dbRef.child('apps');

        //vm.apps = ['app1', 'app2', 'app3']; // Retrieve that from firebase
        ai.apps = [];

        appsRef.once('value').then(function(snapshot) {
            $timeout(function() {
            var apps = snapshot.val();
            for (var k in apps) {
                ai.apps.push(apps[k]);
            }
            ai.selected_app = ai.apps[0];
            console.log(ai.apps);
            }, 0);
        });

        ai.showDetail = function(app){
            ai.selected_app = app;
        };

        ai.installApp = function(){
            // logic to be decided here
        };
    }
})();
