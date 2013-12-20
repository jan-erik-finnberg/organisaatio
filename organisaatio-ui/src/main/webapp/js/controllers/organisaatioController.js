function OrganisaatioController($scope, $location, $routeParams, OrganisaatioModel) {
    $scope.oid = $routeParams.oid;
    $scope.model = OrganisaatioModel;
    $scope.model.refreshIfNeeded($scope.oid);
    $scope.model.mode = "show";
    
    if (/new$/.test($location.path())) {
        $scope.model.mode = "new";
    } else if (/edit$/.test($location.path())) {
        $scope.model.mode = "edit";
        $scope.model.refreshKoodisto($scope.oid);
    }

    $scope.save = function() {
        $scope.model.persistOrganisaatio();
    }
    $scope.cancel = function() {
        $location.path("/");
    }

    $scope.edit = function () {
      $scope.model.mode = "edit";
      $scope.model.refreshKoodisto($scope.oid);  
      $location.path($location.path() + "/edit");
    };
    
}
