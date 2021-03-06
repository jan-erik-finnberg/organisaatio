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
package fi.vm.sade.organisaatio.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import fi.vm.sade.rajapinnat.kela.KelaGenerator;

@Path("/kela")
@Component
@Api(value = "/kela", description = "Kelan operaatiot")
public class KelaResource {

    private static final Logger LOG = LoggerFactory.getLogger(KelaResource.class);

    private KelaGenerator kelaGenerator;

    @GET
    @Path("/export")
    @Produces("text/plain")
    @ApiOperation(value = "Vie Kelan tiedot", notes = "Operaatio vie Kelan tiedot.", response = String.class)
    public String exportKela(@QueryParam("transfer") @ApiParam(value = "Generoidaanko siirtotiedostot", required = true) final boolean transfer) {
        final ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/context/bundle-context.xml");
        kelaGenerator = context.getBean(KelaGenerator.class);
        try {
            kelaGenerator.generateKelaFiles();
            if (transfer) {
                kelaGenerator.transferFiles();
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        }
        return String.format("%s", kelaGenerator.getHost() != null);
    }
}
