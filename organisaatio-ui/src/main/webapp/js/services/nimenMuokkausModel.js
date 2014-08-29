app.factory('NimenMuokkausModel', function($q, $filter, $log, $location, Alert, NimiHistoriaModel, Nimet) {
//    emptyNimi = {
//        "nimi" : {
//            "fi" : "",
//            "sv" : "",
//            "en" : ""
//        },
//        "alkuPvm" : ""
//    };

    var model = {
        oid : "",
        minAlkuPvm : "",
        nimi : {},
        mode : "update",
        historiaModel : NimiHistoriaModel,

        // Tyhjenneteään mallin tiedot
        clear: function() {
            this.oid = "";
            this.minAlkuPvm = "";
            this.nimi = {};
            this.mode = "update";
            this.historiaModel.clear();
        },

        // Haetaan uuden nimen minimialkupäivämäärä
        // Viimeisimmän voimassaolevan nimen alkupäivämäärä tai organisaation alkupäiviämäärä.
        getMinAlkuPvm: function(organisaatioAlkuPvm) {
            var voimassaolevaNimi = model.historiaModel.getCurrentNimi();
            var minAlkuPvm = "";

            // Uuden organisaation tapaus
            if (voimassaolevaNimi === null) {
                return minAlkuPvm;
            }

            if('alkuPvm' in voimassaolevaNimi && moment(voimassaolevaNimi.alkuPvm).isValid()) {
                minAlkuPvm = voimassaolevaNimi.alkuPvm;
            }
            else {
                minAlkuPvm = organisaatioAlkuPvm;
            }
            $log.debug('Minimi alkupvm: ' + minAlkuPvm);

            return minAlkuPvm;
        },

        // Laitetaan uusin nimi näkyville / editoitavaksi
        setUusinNimiVisible: function() {
            this.nimi = this.historiaModel.uusinNimi;
        },

        // Tyhjennetään editoitava nimi
        clearVisibleNimi: function() {
            this.nimi = {};
        },

        // Tarkastetaan onko annettu nimi ajastettu nimenmuutos
        isAjastettuMuutos: function(nimi) {
            return this.historiaModel.isAjastettuMuutos(nimi);
        },

        // Uuden nimen tallennus
        saveNewNimi: function(deferred) {
            Nimet.put({oid: this.oid, alkuPvm: ""}, this.nimi, function(result) {
                $log.log(result);
                deferred.resolve();
            },
            // Error case
            function(response) {
                $log.error("Nimet put response: " + response.status);
                Alert.add("error", $filter('i18n')("Nimenmuokkaus.uusinimi.virhe", ""), true);
                deferred.reject();
            });
        },

        // Nimen päivitys
        saveUpdatedNimi: function(deferred) {
            Nimet.post({oid: this.oid, alkuPvm: this.nimi.alkuPvm}, this.nimi, function(result) {
                $log.log(result);
                deferred.resolve();
            },
            // Error case
            function(response) {
                $log.error("Nimet post response: " + response.status);
                Alert.add("error", $filter('i18n')("Nimenmuokkaus.updatenimi.virhe", ""), true);
                deferred.reject();
            });
        },

        // Ajastetun nimenmuutoksen poisto / peruminen
        deletePresetNimi: function(deferred) {
            Nimet.delete({oid: this.oid, alkuPvm: this.historiaModel.uusinNimi.alkuPvm}, function(result) {
                $log.log(result);
                deferred.resolve();
            },
            // Error case
            function(response) {
                $log.error("Nimet delete response: " + response.status);
                Alert.add("error", $filter('i18n')("Nimenmuokkaus.deletenimi.virhe", ""), true);
                deferred.reject();
            });
        },

        // Tallennus, tilasta riippuen luodaan uusi nimi, päivitetään nimi tai perutaan ajastus
        save: function() {
            var deferred = $q.defer();

            // Uuden organisaation tapauksessa luotetaan siihen, että
            // organisaation tallennus tallentaa myös ensimmäisen nimihistorian
            if (this.uusiOrganisaatio) {
                deferred.resolve();
                return deferred.promise;;
            }

            if (this.mode === "update") {
                this.saveUpdatedNimi(deferred);
            }
            else if (this.mode === "new") {
                this.saveNewNimi(deferred);
            }
            else if (this.mode === "delete") {
                this.deletePresetNimi(deferred);
            }
            else {
                $log.error("Unknown mode: " + this.mode);
            }
            return deferred.promise;
        },

        // Ennekuin NimenMuokkausModel:a voidaan käyttää pitää se alustaa
        refresh: function(oid, nimihistoria, organisaatioAlkuPvm,
                          koulutustoimija, oppilaitos, parentNimi,
                          nameFormat, parentPattern) {
            $log.log('refresh()');

            // Alustetaan historiamalli
            this.historiaModel.init(nimihistoria);

            this.oid = oid;
            this.koulutustoimija = koulutustoimija;
            this.oppilaitos = oppilaitos;
            this.parentNimi = parentNimi;
            this.nameFormat = nameFormat;
            this.parentPattern = parentPattern;

            if (/new$/.test($location.path())) {
                this.uusiOrganisaatio = true;
                this.mode = "new";
            }
            else {
                this.uusiOrganisaatio = false;
                this.mode = "update";
            }

            this.ajastettuMuutos = this.historiaModel.ajastettuMuutos;
            this.minAlkuPvm = this.getMinAlkuPvm(organisaatioAlkuPvm);

            this.setUusinNimiVisible();
        }
    };

    return model;
});



