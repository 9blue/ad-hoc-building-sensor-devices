(function() {
    'use strict';

    adhocAPP.controller('logicBuilderController', logicBuilderController);

    function logicBuilderController($stateParams,$scope) {
        var ai = this;

        ai.modalTemplate = "template/thresholdInfo.html";
        var dbRef = firebase.database().ref();
        var organizations = dbRef.child('organizations');
        var apps = dbRef.child('applications');
        ai.organizations = [];

        ai.selectedOrg = null;

        // to be replaced by sensor list
        organizations.once('value').then(function(snapshot) {
            $timeout(function() {
                if (!ai.selectedOrg) {
                    var orgs = snapshot.val();
                    for (var k in orgs) {
                        ai.organizations.push(orgs[k]);
                    }
                    ai.selectedOrg = ai.organizations[0];
                }
                console.log(ai.organizations);
            }, 0);
        });

        ai.id = $stateParams.id;
        ai.text = 'This is app installer!';
  		ai.choices = [{id: 'choice1'}];
  
  		$scope.addNewChoice = function() {
    		var newItemNo = ai.choices.length+1;
    		ai.choices.push({'id':'choice'+newItemNo});
  		};
    
  		$scope.removeChoice = function() {
  			if(ai.choices.length == 1){
  				return;
  			}
    		var lastItem = ai.choices.length-1;
    		ai.choices.splice(lastItem);
	  	};


    }
})();
