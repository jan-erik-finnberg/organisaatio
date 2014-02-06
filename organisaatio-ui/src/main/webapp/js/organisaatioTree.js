app.factory('HakuehdotModel', function($filter, $log, Alert,
                                       KoodistoPaikkakunnat,
                                       KoodistoOrganisaatiotyypit,
                                       KoodistoOppilaitostyypit,
                                       KoodistoKoodi) {
    var model = {
        refreshed: false,
        nimiTaiTunnus: "",
        kunta: "",
        organisaatiotyyppi: "",
        oppilaitostyyppi: "",
        tila: "kaikki",
        kaikki: "kaikki",
        vainAktiiviset: "vainAkviiviset",
        vainLakkautetut: "vainLakkautetut",
        paikkakunnat: [],
        organisaatiotyypit: [],
        oppilaitostyypit: [],

        refresh: function() {
            $log.log('refresh()');
            model.refreshed = false;
            model.refreshIfNeeded();
        },

        isEmpty: function() {
            if (model.nimiTaiTunnus ||
                    model.kunta ||
                    model.organisaatiotyyppi ||
                    model.oppilaitostyyppi) {
                return false;
            }   
            return true;
        },
        
        refreshIfNeeded: function() {
            $log.log('refreshIfNeeded()');
            if (model.refreshed === false) {
                model.refreshed = true;
                KoodistoPaikkakunnat.get(function(result) {
                    result.forEach(function(kuntaKoodi) {
                        var paikkakunta = {"uri": kuntaKoodi.koodiUri,
                            "arvo":kuntaKoodi.koodiArvo};
                        
                        paikkakunta.nimi = KoodistoKoodi.getLocalizedName(kuntaKoodi);
                        model.paikkakunnat.push(paikkakunta);
                    });
                    $log.log('paikkakunnat: ' +  model.paikkakunnat.length);
                }, 
                // Error case
                function(response) {
                    $log.error("KoodistoPaikkakunnat response: " + response.status);
                    Alert.add("error", $filter('i18n')("Organisaatiot.koodistoVirhe", ""), true);
                    model.refreshed = false;
                });
                
                KoodistoOrganisaatiotyypit.get(function(result) {
                    result.forEach(function(orgTyyppiKoodi) {
                        var organisaatioTyyppi = {"uri": orgTyyppiKoodi.koodiUri,
                            "arvo":orgTyyppiKoodi.koodiArvo};

                        organisaatioTyyppi.nimi = KoodistoKoodi.getLocalizedName(orgTyyppiKoodi);
                        model.organisaatiotyypit.push(organisaatioTyyppi);
                    });
                    $log.log('organisaatiotyypit: ' +  model.organisaatiotyypit.length);
                }, 
                // Error case
                function(response) {
                    $log.error("KoodistoPaikkakunnat response: " + response.status);
                    Alert.add("error", $filter('i18n')("Organisaatiot.koodistoVirhe", ""), true);
                    model.refreshed = false;
                });
                
                KoodistoOppilaitostyypit.get(function(result) {
                    result.forEach(function(oplTyyppiKoodi) {
                        var oppilaitosTyyppi = {"uri": oplTyyppiKoodi.koodiUri,
                            "arvo":oplTyyppiKoodi.koodiArvo};

                        oppilaitosTyyppi.nimi = KoodistoKoodi.getLocalizedName(oplTyyppiKoodi);
                        model.oppilaitostyypit.push(oppilaitosTyyppi);
                    });
                    $log.log('oppilaitostyypit: ' +  model.oppilaitostyypit.length);
                }, 
                // Error case
                function(response) {
                    $log.error("KoodistoPaikkakunnat response: " + response.status);
                    Alert.add("error", $filter('i18n')("Organisaatiot.koodistoVirhe", ""), true);
                    model.refreshed = false;
                });
            }          
        },

        resetTarkemmatEhdot: function () {
            $log.log('resetTarkemmatEhdot()');
            model.kunta= "";
            model.organisaatiotyyppi= "";
            model.oppilaitostyyppi= "";
            model.tila = "kaikki";
        },

        resetAll: function () {
            $log.log('resetAll()');
            model.nimiTaiTunnus="";
            model.kunta= "";
            model.organisaatiotyyppi= "";
            model.oppilaitostyyppi= "";
            model.tila = "kaikki";
        }
    };

    return model;
});


app.factory('OrganisaatioTreeModel', function($filter, $log, Alert, Organisaatiot) {
// organisaatiot[]
//     {
//        "oid" : "1.2.246.562.10.71103955986",
//        "alkuPvm" : 694216800000,
//        "parentOid" : "1.2.246.562.10.45506210314",
//        "parentOidPath" : "1.2.246.562.10.71103955986/1.2.246.562.10.45506210314/1.2.246.562.10.45754497167/1.2.246.562.10.00000000001",
//        "match" : true,
//        "nimi" : {
//          "fi" : "Kappelimäen koulu"
//        },
//        "children" : [ ],
//        "organisaatiotyypit" : [ "OPETUSPISTE" ],
//        "aliOrganisaatioMaara" : 0
//      }

    var tree = {id: "ROOT", expanded: false, children: []};

    var model = {
        count: 0,

        getRootNodes: function () {
            return tree.children;
        },

        expand: function (node) {
            if (node.expanded !== true) {
                node.expanded = true;
            } else {
                node.expanded = false;
            }
        },

        isExpanded: function (data) {
            return data.expanded;
        },
        
        isCollapsed: function (data) {
            return !this.isExpanded(data);
        },
        
        isLeaf: function (data) {
            return data.children.length === 0;
        },

        getStatus: function (node) {
           var today = +new Date(); 
           
           if ('alkuPvm' in node) {
              var alkuPvm = new Date(node.alkuPvm);
              
              if (alkuPvm > today) {
                  return ($filter('i18n')("Organisaatiot.suunnitteilla",""));
              }
           }
           if ('lakkautusPvm' in node) {
              var lakkautusPvm = new Date(node.lakkautusPvm);
              
              if (lakkautusPvm < today) {
                  return ($filter('i18n')("Organisaatiot.lakkautettu",""));
              }
           }
           return null;
        },

        getTila: function(node) {
           var today = +new Date(); 
           
           if ('alkuPvm' in node) {
              var alkuPvm = new Date(node.alkuPvm);
              
              if (alkuPvm > today) {
                  return ($filter('i18n')("Organisaatiot.suunniteltu",""));
              }
          }
           if ('lakkautusPvm' in node) {
              var lakkautusPvm = new Date(node.lakkautusPvm);
              
              if (lakkautusPvm < today) {
                  return ($filter('i18n')("Organisaatiot.passivoitu",""));
              }
           }
           return ($filter('i18n')("Organisaatiot.aktiivinen",""));
        },

        getNimi: function (node) {
            if ('fi' in node.nimi) {
                return node.nimi.fi;
            }
            if ('sv' in node.nimi) {
                return node.nimi.sv;
            }
            return "--";
        },
        
        getTunnus: function (node) {
            if ('oppilaitosKoodi' in node) {
                return node.oppilaitosKoodi;
            }
            if ('ytunnus' in node) {
                return node.ytunnus;
            }
            return "\u00A0";
        },
        
        getTyyppi: function (node) {
            function capitalize (text) {
                        return text.charAt(0).toUpperCase() + text.slice(1).toLowerCase();
            }

            if ('organisaatiotyypit' in node) {
                return capitalize(node.organisaatiotyypit[0]);
            }
            return "\u00A0";
        },
              
        buildHakuParametrit: function(hakuehdot) {
            $log.log('buildHakuParametrit()');

            var hakuParametrit = {};

            // Lisää hakuun käyttäjän kirjoittama osa organisaation nimest' / tunnuksesta
            hakuParametrit.searchstr = hakuehdot.nimiTaiTunnus;
            
            // Lisää hakuun mahdollinen paikkakunta
            if (hakuehdot.kunta) {
                hakuParametrit.kunta = hakuehdot.kunta;
            }

            // Lisää hakuun mahdollinen organisaatiotyyppi
            if (hakuehdot.organisaatiotyyppi) {
                hakuParametrit.organisaatiotyyppi = hakuehdot.organisaatiotyyppi;
            }

            // Lisää hakuun mahdollinen oppilaitostyyppi
            if (hakuehdot.oppilaitostyyppi) {
                hakuParametrit.oppilaitostyyppi = hakuehdot.oppilaitostyyppi + "#*";
            }

            // Vain aktiiviset tain vain lakkautetut
            if (hakuehdot.tila === hakuehdot.vainAktiiviset) {
                hakuParametrit.vainAktiiviset = true;
            }
            else if (hakuehdot.tila === hakuehdot.vainLakkautetut) {
                hakuParametrit.vainLakkautetut = true;
            }

            // TODO: oidrestrictionlist??

            $log.log(hakuParametrit);

            return hakuParametrit;
        },

        updateTree: function(numHits, organisaatiot) {
            $log.log('updateTree()');
            this.count = numHits;
            tree.children = organisaatiot;
            
            if (this.count === 0) {
                Alert.add("warning", $filter('i18n')("Organisaatiot.eiHakutuloksia", ""), true);
            }
            
            updateSubtree = function(node, level, expanded) {
                node.i18nNimi = model.getNimi(node);
                if (model.getStatus(node)) {
                    node.i18nNimi += " (" + model.getStatus(node) + ")";
                }
                node.tunnus = model.getTunnus(node);
                node.tyyppi = model.getTyyppi(node);
                node.tila   = model.getTila(node);
                node.level  = level;

                if (level === 2 && model.count > 20) {
                    expanded = false;
                }
                for(var i=0; i < node.children.length; i++) {
                    this.updateSubtree(node.children[i], level + 1, expanded);
                }
                node.expanded = expanded;
            };
            
            tree.children.forEach(function(node) {
                var expanded = true;
                
                // Jos ylimmällä tasolla on paljon noodeja, niin ei laajenneta puuta
                if (tree.children.length > 20) {
                    expanded = false;
                }
                this.updateSubtree(node, 1, expanded);
            });
        },

        refresh: function(hakuehdot) {
            $log.log('refresh()');
            var start = +new Date();
            
            var parametrit = this.buildHakuParametrit(hakuehdot);

            Organisaatiot.get(parametrit, function(result) {
                //$log.log("Solar returned: " + result.response.numFound + " entries for query: " + '"' + query + '"');

                var end = +new Date();  // log end timestamp
                var diff = end - start;
                $log.log("Haku kesti: " +diff);

                model.updateTree(result.numHits, result.organisaatiot);
            }, 
            // Error case
            function(response) {
                $log.error("Organisaatiot response: " + response.status);
                Alert.add("error", $filter('i18n')("Organisaatiot.hakuVirhe", ""), true);
            });
        }
    };

    return model;
});


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