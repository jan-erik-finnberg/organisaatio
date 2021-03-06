/*
 Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus
 
 This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 soon as they will be approved by the European Commission - subsequent versions
 of the EUPL (the "Licence");
 
 You may not use this work except in compliance with the Licence.
 You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 European Union Public Licence for more details.
 */

app.factory('RyhmaKoodisto', function($q, $log, $filter, KoodistoArrayByUri, KoodistoKoodi, Alert) {

    var showAndLogError = function(msg, response) {
        model.alert = Alert.add("error", $filter('i18n')(response.data.errorKey || msg), false);
        $log.error(msg + " (status: " + response.status + ")");
    };

    /*
     * Palauttaa koodiston arrayna, jonka itemit muotoa { uri: <koodiuri>, nimi: <lokalisoitu nimi> }
     * Parametrit:
     *   uri: koodistoUri
     *   resultArray: Array johon koodi-itemit tallennetaan
     *   defaultArray: jos tyyppiä Array, palautetaan virhetilanteessa eikä näytetä virheilmoitusta. Jos muu kuin Array, näytetään virheilmoitus.
     */
    var getKoodistoArray = function(uri, resultArray, defaultArray) {
        KoodistoArrayByUri.get({uri: uri}, function(result) {
            resultArray.length = 0;
            result.forEach(function(rTyyppiKoodi) {
                resultArray.push({uri: rTyyppiKoodi.koodiUri + "#" + rTyyppiKoodi.versio, nimi: KoodistoKoodi.getLocalizedName(rTyyppiKoodi)});
            });
        }, function(response) {
            // koodeja ei löytynyt
            if (defaultArray instanceof Array) {
                defaultArray.forEach(function(rTyyppiKoodi) {
                    resultArray.push(rTyyppiKoodi);
                });
            } else {
                showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
            }
        });
    };

    var model = new function() {
        this.ryhmatyypit = [];
        this.kayttoryhmat = [];
        
        var koodistoArrays = [
            {
                uri: 'ryhmatyyppi',
                resultArray: this.ryhmatyypit,
                defaultArray: [
                    // Default-arvoja käytetään kunnes koodistoon lisätään 'ryhmatyyppi'-koodisto
                    // Default-arvojen lähde: Confluence / Ryhmien määrittely
                    {uri: 'organisaatio', nimi: 'Organisaatio'},
                    {uri: 'hakukohde', nimi: 'Hakukohde'},
                    {uri: 'perustetyoryhma', nimi: 'Perustetyöryhmä'}
                ]
            },
            {
                uri: 'kayttoryhma',
                resultArray: this.kayttoryhmat,
                // Default-arvoja käytetään kunnes koodistoon lisätään 'kayttoryhma'-koodisto
                // Default-arvojen lähde: Confluence / Ryhmien määrittely
                defaultArray: [
                    {uri: 'yleinen', nimi: 'Yleinen'},
                    {uri: 'hakukohde_rajaava', nimi: 'Rajaava'},
                    {uri: 'hakukohde_priorisoiva', nimi: 'Priorisoiva'},
                    {uri: 'hakukohde_liiteosoite', nimi: 'Liiteosoite'},
                    {uri: 'perusteiden_laadinta', nimi: 'Perusteiden laadinta'}
                ]
            }
        ];

        koodistoArrays.forEach(function(koodistoItem) {
            getKoodistoArray(koodistoItem.uri, koodistoItem.resultArray, koodistoItem.defaultArray);
        });

    };
    return model;
});
