/*
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
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
 * European Union Public Licence for more details.
 */
package fi.vm.sade.organisaatio.service.converter;

import javax.annotation.Nullable;

import com.google.common.base.Function;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioKuvaTyyppi;
import fi.vm.sade.organisaatio.model.lop.BinaryData;

public class EntityToOrganisaatioKuvaTyyppiFunction implements Function<BinaryData, OrganisaatioKuvaTyyppi> {

    @Override
    public OrganisaatioKuvaTyyppi apply(@Nullable BinaryData entity) {
        if (entity == null) {
            return null;
        }
        final OrganisaatioKuvaTyyppi kuva = new OrganisaatioKuvaTyyppi();
        kuva.setFileName(entity.getFilename());
        kuva.setMimeType(entity.getMimeType());
        kuva.setKuva(entity.getData());
        return kuva;
    }
}
