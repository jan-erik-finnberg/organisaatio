function OrganisaatioTreeController($scope, $location, $filter,
                                    $modal, $log, Alert, Organisaatio, 
                                    HakuehdotModel, OrganisaatioTreeModel) {
    $scope.hakuehdot = HakuehdotModel;
    $scope.model     = OrganisaatioTreeModel;
    $scope.tarkemmatHakuehdotVisible = false;
    $scope.currentOid = '';
   
    $scope.getTimes=function(n){
        return new Array(n);
    };   
   
    $scope.setCurrentOid = function(oid) {
        $scope.currentOid = oid;
    };

    $scope.isCurrentOid = function(oid) {
        return $scope.currentOid === oid;
    };

    $scope.isDeleteAllowed = function(node) {
        // Tarkistetaan ettei ole aliorganisaatioita
        return $scope.model.isLeaf(node);
    };

    $scope.deleteOrganisaatio = function (node) {
        var modalInstance = $modal.open({
            templateUrl: 'organisaationpoisto.html'
        });
        
        modalInstance.result.then(function () {
            $log.info('Organisaatio poisto vahvistettu: ' + node.oid);
                        
            Organisaatio.delete({oid: node.oid}, function(result) {
                $log.log(result);
                // TODO: Pitäskö tehdä refresh
            }, 
            // Error case
            function(response) {
                $log.error("Organisaatio delete response: " + response.status);
                Alert.add("error", $filter('i18n')("Organisaationpoisto.poistoVirhe", ""), true);
            });
            
        }, function () {
            $log.info('Organisaation poistoa ei vahvistettu: ' + node.oid);
        });
    };

    $scope.search = function() {
        if ($scope.hakuehdot.isEmpty()) {
            $log.warn("Hakuehdon tyhjät!");
            Alert.add("warning", $filter('i18n')("Organisaatiot.tarkennaHakuehtoja", ""), true);
            return;
        }
        $scope.model.refresh($scope.hakuehdot);
    };

    $scope.resetHakuehdot = function() {
        $scope.hakuehdot.resetAll();
    };

    $scope.hideTarkemmatHakuehdot = function() {
        $scope.tarkemmatHakuehdotVisible = false;

        // Tarkempien ehtojen piilotus tyhjentää tarkemmat hakukentät
        $scope.hakuehdot.resetTarkemmatEhdot();
    };

    $scope.showTarkemmatHakuehdot = function() {
        $scope.hakuehdot.refreshIfNeeded();
        $scope.tarkemmatHakuehdotVisible = true;
    };
   
    $scope.luoYlinTaso = function () {
        var modalInstance = $modal.open({
            templateUrl: 'yritysvalinta.html',
            controller: YritysValintaController,
            windowClass:'modal-wide',
            resolve: {
                // return undefined --> ei ytunnuksen esivalintaa
                ytunnus: function () {
                    return;
                }
            }
        });
        
        modalInstance.result.then(function (ytunnus) {
            if (ytunnus) {
                $log.log('Luodaan uusi organisaatio YTynnuksella: ' + ytunnus);
                $location.search('ytunnus',ytunnus).path($location.path() + 
                        "/" + ROOT_ORGANISAATIO_OID +"/new");
            }
            else {
                $location.path($location.path() + "/" + ROOT_ORGANISAATIO_OID + "/new");  
            }
        }, function () {
            $log.log('Modal dismissed at: ' + new Date());
        });
    };
   
}
