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

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Function;

import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;

public class EntityToMonikielinenTekstiTyyppiFunction implements Function<MonikielinenTeksti, MonikielinenTekstiTyyppi> {

    @Override
    public MonikielinenTekstiTyyppi apply(@Nullable MonikielinenTeksti source) {
        if (source == null) {
            return null;
        }
        MonikielinenTekstiTyyppi dto = new MonikielinenTekstiTyyppi();
        for (Map.Entry<String, String> e : source.getValues().entrySet()) {
            Teksti curTeksti = new Teksti();
            curTeksti.setKieliKoodi(e.getKey());
            curTeksti.setValue(e.getValue());
            dto.getTeksti().add(curTeksti);
        }
        return dto;
    }
}
