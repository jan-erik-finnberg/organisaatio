/*
* Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
*
* This program is free software:  Licensed under the EUPL, Version 1.1 or - as
* soon as they will be approved by the European Commission - subsequent versions
* of the EUPL (the "Licence");
*
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*/

package fi.vm.sade.organisaatio.dao;

import fi.vm.sade.generic.dao.JpaDAO;
import fi.vm.sade.organisaatio.model.Organisaatio;
import java.util.List;

/**
 *
 * @author simok
 */
public interface OrganisaatioDAO extends JpaDAO<Organisaatio, Long> {
    
    /**
     *
     * @param kieliList kielirajaus kielivalikoima-koodiston koodiUreja: ["kielivalikoima_en", "kielivalikoima_sv"]
     * @param kuntaList kuntarajaus kunta-koodiston koodiUreja: ["kunta_905", "kunta_401"]
     * @param oppilaitostyyppiList oppilaitostyyppirajaus oppilaitostyyppi-koodiston koodiUreja: ["oppilaitostyyppi_19", "oppilaitostyyppi_91"]
     * @param vuosiluokkaList vuosiluokkarajaus vuosiluokat-koodiston koodiUreja: ["vuosiluokat_1","vuosiluokat_2"]
     * @param ytunnusList y-tunnusrajaus: ["0147510-4", "0203797-4"]
     * @param limit hakutuloksen määrän rajoite
     * @return
     */
    public List<Organisaatio> findBySearchCriteria(
            List<String> kieliList,
            List<String> kuntaList,
            List<String> oppilaitostyyppiList,
            List<String> vuosiluokkaList,
            List<String> ytunnusList,
            int limit);
    
}
