(function() {
    'use strict';

    adhocAPP.service('QRService', QRService);

    function QRService($http, $q) {
        var qr_url = 'https://chart.googleapis.com/chart?cht=qr&chs=300x300&chl=';

        this.getQRCode = function(info) {
            var deferred = $q.defer();
            $http.get(qr_url + info).then(function(response) {
                deferred.resolve(response.data);
            }, function(response) {
                deferred.reject(response.data);
            });
            return deferred.promise;
        };
    }
})();
