/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.vm.sade.organisaatio.ui.widgets.simple;

import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Set;
/*
 *
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

public class SimpleOrganisaatioWidgetFactory implements ApplicationContextAware {

    private static ApplicationContext staticApplicationContext;

    private static OrganisaatioProxy organisaatioProxy;

    public SimpleOrganisaatioWidgetFactory() {
        // empty
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        setStaticContext(ac);
    }

    private static void setStaticContext(ApplicationContext context) {
        SimpleOrganisaatioWidgetFactory.staticApplicationContext = context;

        // get services from spring
        OrganisaatioService organisaatioService = staticApplicationContext.getBean(OrganisaatioService.class);

        if (organisaatioService == null) {
            throw new IllegalArgumentException("OrganisaatioService is not defined - can't initialize");
        }

        organisaatioProxy = new OrganisaatioProxyCachingImpl(organisaatioService);
    }

    public static OrganisaatioComponent createComponent(Set<String> organisationsHierarchy) {

        // TODO: tee proxy
        if (organisaatioProxy == null) {
            throw new RuntimeException("WidgetFactory was not initialized properly - service proxies are null.");
        }
        OrganisaatioComponent organisaatioComponent = new OrganisaatioComponent(organisaatioProxy, organisationsHierarchy);
        return organisaatioComponent;
    }

}
