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


package fi.vm.sade.organisaatio.service.util;

import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author simok
 */
public abstract class OrganisaatioPerustietoUtil {
    /**
     * Luo puumaisen organisaatiohierarkian. 
     * 
     * @param organisaatiot List organisaatioista, joista muodostetaan puu
     * @return Lista juuritason organisaatioista (lapset asetettu niiden alle)
     */
    public static List<OrganisaatioPerustieto> createHierarchy(
            final List<OrganisaatioPerustieto> organisaatiot) {

        Map<String, OrganisaatioPerustieto> oidToOrgMap = new HashMap<String, OrganisaatioPerustieto>();

        //ORganisaatiot joilla eil ole isää:
        List<OrganisaatioPerustieto> rootOrgs = new ArrayList<OrganisaatioPerustieto>();

        for (OrganisaatioPerustieto curOrg : organisaatiot) {
            oidToOrgMap.put(curOrg.getOid(), curOrg);
        }

        for (OrganisaatioPerustieto curOrg : organisaatiot) {
            final String parentOid = curOrg.getParentOid();
            final OrganisaatioPerustieto parentOrg = oidToOrgMap.get(parentOid);
            if (parentOrg != null) {
                parentOrg.getChildren().add(curOrg);
            } else {
                rootOrgs.add(curOrg);
            }
        }

        return rootOrgs;
    }
}
