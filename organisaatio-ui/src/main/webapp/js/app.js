var app = angular.module('organisaatio', ['ngResource', 'loading', 'ngRoute', 'localisation','localization', 'ui.bootstrap', 'ngSanitize', 'ui.tinymce', 'ngCookies', 'ngIdle']);

angular.module('localization', [])
.filter('i18n', ['$rootScope','$locale', '$window', '$http', 'UserInfo', 'LocalisationService', function ($rootScope, $locale, $window, $http, UserInfo, LocalisationService) {
    var initialized = false;

    UserInfo.then(function(s) {

        LocalisationService.setLocale(s.lang.toLowerCase());
        initialized = true;
    });

    return function (localisationKey) {
        return initialized ? LocalisationService.t(localisationKey) : '...';
    };
}]);

app.filter('fixHttpLink',function () {
    return function (text) {
        proto = text.split("://");
        return (proto.length>1 ? proto[0] : "http") + "://" + proto[proto.length-1];
    };
});

app.filter('decodeAmp',function () {
    return function (text) {
        if (text===null) {
            return null;
        }
        return text.replace(/&amp;/g, '&');
    };
});

////////////
//
// Configuration from config/properties files
//
////////////
var UI_URL_BASE = UI_URL_BASE || "http://localhost:8180/organisaatio-ui/";
var SERVICE_URL_BASE = SERVICE_URL_BASE || "";
var TEMPLATE_URL_BASE = TEMPLATE_URL_BASE || "";
var KOODISTO_URL_BASE = KOODISTO_URL_BASE || "";
var LOKALISAATIO_URL_BASE = LOKALISAATIO_URL_BASE || "";
var AUTHENTICATION_URL_BASE = AUTHENTICATION_URL_BASE || "";
var ROOT_ORGANISAATIO_OID = ROOT_ORGANISAATIO_OID || "";
var CAS_ME_URL = CAS_ME_URL || "/cas/me";
var SESSION_KEEPALIVE_INTERVAL_IN_SECONDS = SESSION_KEEPALIVE_INTERVAL_IN_SECONDS || 30;
var MAX_SESSION_IDLE_TIME_IN_SECONDS = MAX_SESSION_IDLE_TIME_IN_SECONDS || 1800;

////////////
//
// Route configuration
//
////////////
app.config(function($routeProvider, $httpProvider) {
        $httpProvider.interceptors.push('NoCacheInterceptor');

        $routeProvider.

        // front page
        when('/organisaatiot', {controller:OrganisaatioTreeController, templateUrl:TEMPLATE_URL_BASE + 'organisaatiot.html'}).

        // read one
        when('/organisaatiot/:oid', {controller:OrganisaatioController, templateUrl:TEMPLATE_URL_BASE + 'organisaationtarkastelu.html'}).

        // edit one
        when('/organisaatiot/:oid/edit', {controller:OrganisaatioController, templateUrl:TEMPLATE_URL_BASE + 'organisaationmuokkaus.html'}).

        // create new
        when('/organisaatiot/:parentoid/new', {controller:OrganisaatioController, templateUrl:TEMPLATE_URL_BASE + 'organisaationmuokkaus.html'}).

        // yhteystietojen tyypit
        when('/yhteystietotyypit', {controller:YhteystietojentyyppiController, templateUrl:TEMPLATE_URL_BASE + 'yhteystietojentyyppi.html'}).

        // manage groups
        when('/organisaatiot/:parentoid/groups', {controller:RyhmienHallintaController, templateUrl:TEMPLATE_URL_BASE + 'ryhmienhallinta.html'}).

            //else
        otherwise({redirectTo:'/organisaatiot'});
});

// https://github.com/angular/angular.js/issues/2614
app.config(['$provide', function($provide) {
    $provide.decorator('$sniffer', ['$delegate', function($sniffer) {
            var msie = parseInt((/msie (\d+)/.exec(angular.lowercase(navigator.userAgent)) || [])[1], 10);
            var _hasEvent = $sniffer.hasEvent;
            $sniffer.hasEvent = function(event) {
                if (event === 'input' && msie === 10) {
                    return false;
                }
                _hasEvent.call(this, event);
            };
            return $sniffer;
        }]);
}]);

app.run(function(OrganisaatioInitAuth, UserInfo) {
    // Tehdään autentikoitu get servicelle
    // Näin kierretään ongelma: "CAS + ensimmäinen autentikoitia vaativa POST kutsu"
    OrganisaatioInitAuth.init();
});

////////////
//
// Services
//
////////////
app.service('KoodistoKoodi', function($locale, $window, $http, UserInfo) {
    var language = 'FI';
    UserInfo.then(function(s) {
        language = s.lang;
    });

    this.getLocalizedName = function(koodi) {
        var nimi = koodi.metadata[0].nimi;
        koodi.metadata.forEach(function(metadata){
            if(metadata.kieli === language) {
                nimi = metadata.nimi;
            }
        });
        return nimi;
    };

    // lang = FI tai SV
    this.getLangName = function(koodi, lang) {
        var nimi = koodi.metadata[0].nimi;
        koodi.metadata.forEach(function(metadata){
            if(metadata.kieli === lang) {
                nimi = metadata.nimi;
            }
        });
        return nimi;
    };

    this.getLanguage = function() {
        return language;
    };

    this.isValid = function(koodi) {
        if (koodi.voimassaAlkuPvm) {
            if (new Date() < new Date(koodi.voimassaAlkuPvm)) {
                // Ei vielä voimassa
                return false;
            }
        }
        if (koodi.voimassaLoppuPvm) {
            if (new Date() > new Date(koodi.voimassaLoppuPvm)) {
                // Ei enää voimassa
                return false;
            }
        }
        return true;
    };

});

// Esimerkki: Alert.add("warning", $filter('i18n')("YritysValinta.virheViesti", ""), true);
app.factory('Alert', ['$rootScope', '$timeout', function($rootScope, $timeout) {
        var alertService;
        $rootScope.alerts = [];
        alertService = {
            add: function(type, msg, usetimeout, hideOnTopLevel) {
                var alert = {
                    type: type,
                    msg: msg,
                    showOnTopLevel: !hideOnTopLevel,
                    close: function() {
                        return alertService.closeAlert(this);
                    }
                };
                // Tarkistetaan onko tälläistä virhettä jo, jos on niin ei luoda uutta
                for (var i = 0; i < $rootScope.alerts.length; i++) {
                    if ($rootScope.alerts[i].type === alert.type &&
                        $rootScope.alerts[i].message === alert.message) {
                       return $rootScope.alerts[i];
                    }
                }
                if (usetimeout) {
                    alert.timeout = $timeout(function() {
                        alertService.closeAlert(this);
                    }, 8000);
                }
                return $rootScope.alerts.push(alert);
            },
            closeAlert: function(alert) {
                return this.closeAlertIdx($rootScope.alerts.indexOf(alert));
            },
            closeAlertIdx: function(index) {
                return $rootScope.alerts.splice(index, 1);
            },
            clear: function(){
                $rootScope.alerts = [];
            }
        };
        $rootScope.$on('$locationChangeStart', function() {
            alertService.clear();
        });
        return alertService;
    }
]);

app.factory('UserInfo', ['$q', '$http', '$log', function($q, $http, $log) {
    var deferred = $q.defer();

    (function() {
        var instance = {};
        instance.lang = 'FI';
        $http.get(CAS_ME_URL).success(function(result) {
            $log.debug(result);
            var lang = angular.fromJson(result).lang;
            if (lang) {
                // Toistaiseksi vain SV on tuettu FI:n lisäksi
                instance.lang = (lang.toUpperCase()==="SV" ? "SV" : "FI");
                deferred.resolve(instance);
            } else {
                $log.debug('failed parsing result, defaulting to FI');
                deferred.resolve(instance);
            }
        }).error(function(data, status, headers, config) {
            deferred.resolve(instance);
        });
    })();

    return deferred.promise;
}]);

app.factory('OrganisaatioInitAuth', ['$log', 'Alert', 'OrganisaatioAuthGET', '$timeout', '$filter', function($log, Alert, OrganisaatioAuthGET, $timeout, $filter) {
        var initAuthService;
        return initAuthService = {
            init: function() {
                OrganisaatioAuthGET.get({}, function(result) {
                    $log.log("Organisaatio Auth Init.");
                },
                // Error case
                function(response) {
                    $timeout(function() {
                        OrganisaatioAuthGET.get({}, function(result) {
                            $log.log("Organisaatio Auth Init, second try.");
                        }, function(response) {
                            Alert.add("error", $filter('i18n')("Organisaatiot.yleinenVirhe", ""), true);
                            $log.error("Organisaatio Auth Init failed, response: " + response.status);
                        });
                    }, 1000);
                });
            }
        };
    }
]);

// http://stackoverflow.com/questions/16098430/angular-ie-caching-issue-for-http#19771501
// Cachen voi sallia yksittäisille URLeille parametrilla '?allowCache=true'
app.factory('NoCacheInterceptor', function() {
    return {
        request: function(config) {
            if (config.method && config.method === 'GET' &&
                    config.url.indexOf('html') === -1 &&
                    config.url.indexOf("?allowCache=true") === -1 &&
                    (config.url.indexOf(SERVICE_URL_BASE) !== -1 ||
                            (config.url.indexOf(KOODISTO_URL_BASE) !== -1))) {
                var separator = config.url.indexOf('?') === -1 ? '?' : '&';
                config.url = config.url + separator + 'noCache=' + new Date().getTime();
            }
            return config;
        }
    };
});

////////////
//
// REST resources
//
////////////

// Organisaation haku / päivitys organisaatiopalveluun
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/1.2.246.562.10.23198065932
app.factory('Organisaatio', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/:oid", {oid: "@oid"}, {
        get: {method:   "GET"},
        post:{method:   "POST"},
        delete:{method: "DELETE"}
    });
});

// Organisaation luonti organisaatiopalveluun
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/1.2.246.562.10.23198065932
app.factory('UusiOrganisaatio', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio", {}, {
        put: {method:   "PUT"}
    });
});

// Aliorganisaatioiden haku organisaatiopalvelulta
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/hae?oidRestrictionList=1.2.246.562.10.59347432821
app.factory('Aliorganisaatiot', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/hae?oidRestrictionList=:oid", {oid: "@oid"}, {
        get: {method: "GET"}
    });
});

// Organisaatioiden haku puunäkymää varten organisaatiopalvelulta
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/hae?searchstr=lukio&lakkautetut=true
app.factory('Organisaatiot', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/v2/hierarkia/hae", {}, {
        get: {method: 'GET'}
    });
});

// Autentikoitu get kutsu organisaatiopalveluun
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/auth
app.factory('OrganisaatioAuthGET', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/auth", {}, {
        get: {method:   "GET"}
    });
});

// Kuntien haku koodistopalvelulta
// Esim: https://localhost:8503/koodisto-service/rest/json/kunta/koodi
app.factory('KoodistoPaikkakunnat', function($resource) {
return $resource(KOODISTO_URL_BASE + "json/kunta/koodi?onlyValidKoodis=true", {}, {
    get: {method: "GET", isArray: true}
  });
});

// Kuntien haku koodistopalvelulta
// Esim: https://localhost:8503/koodisto-service/rest/json/kunta/koodi
app.factory('KoodistoPaikkakunta', function($resource) {
return $resource(KOODISTO_URL_BASE + "json/kunta/koodi/:uri", {uri: "@uri"}, {
    get: {method: "GET"}
  });
});

// Organisaatiotyyppien haku koodistopalvelulta
// Esim: https://localhost:8503/koodisto-service/rest/json/organisaatiotyyppi/koodi
app.factory('KoodistoOrganisaatiotyypit', function($resource) {
    return $resource(KOODISTO_URL_BASE + "json/organisaatiotyyppi/koodi", {}, {
        get: {method: "GET", isArray: true}
    });
});

// Oppilaitostyyppien haku koodistopalvelulta
// Esim: https://localhost:8503/koodisto-service/rest/json/oppilaitostyyppi/koodi
app.factory('KoodistoOppilaitostyypit', function($resource) {
    return $resource(KOODISTO_URL_BASE + "json/oppilaitostyyppi/koodi", {}, {
        get: {method: "GET", isArray: true}
    });
});

// Usean koodin haku koodistopalvelulta
// Esim. http://localhost:8081/koodisto-service/rest/json/searchKoodis?koodiUris=posti_52200&koodiUris=maatjavaltiot1_fin
app.factory('KoodistoSearchKoodis', function($resource) {
    return $resource(KOODISTO_URL_BASE + "json/searchKoodis?:uris", {params: "@uris"}, {
        get: {method: "GET", isArray: true}
    });
});

// Maiden haku koodistopalvelulta
// Esim: https://localhost:8503/koodisto-service/rest/json/maatjavaltiot1/koodi
app.factory('KoodistoMaat', function($resource) {
return $resource(KOODISTO_URL_BASE + "json/maatjavaltiot1/koodi?onlyValidKoodis=true", {}, {
    get: {method: "GET", isArray: true}
  });
});

// ISO-kielilistan haku koodistopalvelulta
// Esim: https://localhost:8503/koodisto-service/rest/json/kieli/koodi
app.factory('KoodistoKieli', function($resource) {
return $resource(KOODISTO_URL_BASE + "json/kieli/koodi?onlyValidKoodis=true", {}, {
    get: {method: "GET", isArray: true}
  });
});

// Opetuskielten haku koodistopalvelulta
// Esim: https://localhost:8503/koodisto-service/rest/json/oppilaitoksenopetuskieli/koodi
app.factory('KoodistoOpetuskielet', function($resource) {
return $resource(KOODISTO_URL_BASE + "json/oppilaitoksenopetuskieli/koodi?onlyValidKoodis=true", {}, {
    get: {method: "GET", isArray: true}
  });
});

// YTJ tiedot yhden yrityksen osalta organisaatiopalvelun kautta
// Esim: http://localhost:8180/organisaatio-service/rest/ytj/2397998-7
app.factory('YTJYritysTiedot', function($resource) {
    return $resource(SERVICE_URL_BASE + "ytj/:ytunnus", {ytunnus: "@ytunnus"}, {}, {
        get: {method: 'GET'}
    });
});

// YTJ tietojen haku nimen perusteella
// Esim: http://localhost:8180/organisaatio-service/rest/ytj/hae?nimi=yliopiston
app.factory('YTJYritystenTiedot', function($resource) {
    return $resource(SERVICE_URL_BASE + "ytj/hae", {}, {
        get: {method: 'GET', isArray: true}
    });
});

// Postinumerokoodiston version haku koodistopalvelulta
// Esim: https://localhost:8503/koodisto-service/rest/json/posti
app.factory('KoodistoPostiVersio', function($resource) {
return $resource(KOODISTO_URL_BASE + "json/posti", {}, {
    get: {method: "GET"}
  });
});

// Postinumeroiden haku koodistopalvelulta
// Esim: https://localhost:8503/koodisto-service/rest/json/posti/koodi
app.factory('KoodistoPosti', function($resource) {
return $resource(KOODISTO_URL_BASE + "json/posti/koodi", {}, {
    get: {method: "GET", isArray: true}
  });
});

// Postinumeroiden haku koodistopalvelulta tai selaimen cachesta
// Esim: https://localhost:8503/koodisto-service/rest/json/posti/koodi
app.factory('KoodistoPostiCached', function($resource) {
return $resource(KOODISTO_URL_BASE + "json/posti/koodi?allowCache=true", {}, {
    get: {method: "GET", isArray: true}
  });
});

// Vuosiluokkien haku koodistopalvelulta
// Esim: https://localhost:8503/koodisto-service/rest/json/vuosiluokat/koodi
app.factory('KoodistoVuosiluokat', function($resource) {
return $resource(KOODISTO_URL_BASE + "json/vuosiluokat/koodi", {}, {
    get: {method: "GET", isArray: true}
  });
});

// Muokattavien yhteystietojen haku organisaatiopalvelulta
// Esim. https://localhost:8180/organisaatio-service/rest/yhteystietojentyyppi
app.factory('Yhteystietojentyyppi', function($resource) {
    return $resource(SERVICE_URL_BASE + "yhteystietojentyyppi", {}, {
        get: {method: 'GET', isArray: true},
        post: {method: 'POST'},
        put: {method: 'PUT'}
    });
});

// Yhteystietotyypin poisto organisaatiopalvelulta
app.factory('YhteystietojentyypinPoisto', function($resource) {
    return $resource(SERVICE_URL_BASE + "yhteystietojentyyppi/:oid", { oid: "@oid" }, {
        delete: {method: 'DELETE'}
    });
});

// Virkailijoiden haku organisaatiolle käyttäjähallinnasta
// Esim. https://localhost:8508/authentication-service/resources/henkilo?count=200&ht=VIRKAILIJA&index=0&org=1.2.246.562.10.67019405611
app.factory('HenkiloVirkailijat', function($resource) {
    return $resource(AUTHENTICATION_URL_BASE + "henkilo?count=200&ht=VIRKAILIJA&index=0&org=:oid", { oid: "@oid"}, {
        get: {method: 'GET'}
    });
});

// Henkilön haku käyttäjähallinnasta
// Esim. https://localhost:8508/authentication-service/resources/henkilo/1.2.246.562.24.91121139885
app.factory('Henkilo', function($resource) {
    return $resource(AUTHENTICATION_URL_BASE + "henkilo/:hlooid", { hlooid: "@hlooid"}, {
        get: {method: 'GET'}
    });
});

// Käyttöoikeuden haku henkilölle organisaatiossa
// Esim. https://localhost:8508/authentication-service/resources/kayttooikeusryhma/henkilo/1.2.246.562.24.91121139885?ooid=1.2.246.562.10.82388989657
app.factory('HenkiloKayttooikeus', function($resource) {
    return $resource(AUTHENTICATION_URL_BASE + "kayttooikeusryhma/henkilo/:hlooid?ooid=:orgoid", { hlooid: "@hlooid", orgoid: "@orgoid"}, {
        get: {method: 'GET', isArray: true}
    });
});

// Ryhmien haku organisaatioplavelulta
// Esim. https://itest-virkailija.oph.ware.fi/organisaatio-service/rest/organisaatio/1.2.246.562.10.00000000001/ryhmat
app.factory('Ryhmat', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/:oid/ryhmat", {oid: "@oid"}, {
        get: {method: 'GET', isArray: true}
    });
});

// Viimeisimman päivityksen tietojen haku organisaatioplavelulta
// Esim. https://itest-virkailija.oph.ware.fi/organisaatio-service/rest/organisaatio/v2/1.2.246.562.10.00000000001/paivittaja
app.factory('Paivittaja', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/v2/:oid/paivittaja", {oid: "@oid"}, {
        get: {method: 'GET'}
    });
});

// Nimihistorian haku organisaatioplavelulta
// Lisäksi operaatiot: uuden nimen luonti, vanhan päivitys ja ajastetun nimen poistaminen
// Esim. http://localhost:8180/organisaatio-service/rest/organisaatio/v2/1.2.246.562.10.00000000001/nimet
app.factory('Nimet', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/v2/:oid/nimet/:alkuPvm", {oid: "@oid", alkuPvm: "@alkuPvm"}, {
        get: {method: 'GET', isArray: true},
        post: {method: 'POST'},
        put: {method: 'PUT'},
        delete: {method: 'DELETE'}
    });
});

// Koodiston haku koodistopalvelulta koodistoUrin perusteella
app.factory('KoodistoArrayByUri', function($resource) {
    return $resource(KOODISTO_URL_BASE + "json/:uri/koodi", {params: "@uri"}, {
        get: {method: "GET", isArray: true}
    });
});
