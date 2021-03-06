/*
* Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
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

import fi.vm.sade.organisaatio.model.OrganisaatioNimi;

import fi.vm.sade.generic.dao.JpaDAO;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;

import java.util.Date;
import java.util.List;

/**
 *
 * @author simok
 */
public interface OrganisaatioNimiDAO extends JpaDAO<OrganisaatioNimi, Long>  {

    /**
     * Luodaan uusi nimi organisaatiolle.
     *
     * @param organisaatioId
     * @param nimi
     * @param alkuPvm
     * @param paivittaja
     * @return Luotu OrganisaatioNimi
     */
    OrganisaatioNimi addNimi(Long organisaatioId, MonikielinenTeksti nimi, Date alkuPvm, String paivittaja);

    /**
     * Haetaan annetun organisaation nimet
     *
     * @param organisaatioId
     * @return Annetun organisaation nimihistoria listana
     */
    List<OrganisaatioNimi> findNimet(Long organisaatioId);

    /**
     * Haetaan annetun organisaation nimet
     *
     * @param organisaatioOid
     * @return Annetun organisaation nimihistoria listana
     */
    List<OrganisaatioNimi> findNimet(String organisaatioOid);

    public OrganisaatioNimi findNimi(Long organisaatioId, Date alkuPvm);
    public OrganisaatioNimi findNimi(String organisaatioOid, Date alkuPvm);

    public OrganisaatioNimi findCurrentNimi(Long organisaatioId);
    public OrganisaatioNimi findCurrentNimi(String organisaatioOid);

}
