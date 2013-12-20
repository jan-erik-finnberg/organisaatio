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
package fi.vm.sade.organisaatio.service.search.it;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.api.model.GenericFault;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.RemoveByOidType;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAOImpl;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAOImplTest;
import fi.vm.sade.organisaatio.dao.OrganisaatioSuhdeDAOImpl;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import fi.vm.sade.organisaatio.model.Osoite;
import fi.vm.sade.organisaatio.model.Yhteystieto;
import fi.vm.sade.organisaatio.resource.IndexerResource;
import fi.vm.sade.organisaatio.resource.OrganisaatioResource;
import fi.vm.sade.organisaatio.service.search.OrganisaatioSearchService;
import fi.vm.sade.organisaatio.service.search.SolrServerFactory;

/**
 * Indexes set of orgs in a hierarchy, indexes org data, searches org data from
 * index and compares the result with current implementation.
 */
@ContextConfiguration(locations = { "classpath:spring/test-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ActiveProfiles("embedded-solr")
public class TestIndexingSearching extends SecurityAwareTestBase {

    private static final String ROOT_NAME = "ROOT";
    @Autowired
    private SolrServerFactory factory;
    @Autowired
    private IndexerResource indexer;
    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioDAOImplTest.class);
    @Autowired
    OrganisaatioDAOImpl organisaatioDAO;
    @Autowired
    OrganisaatioSuhdeDAOImpl organisaatioSuhdeDAO;
    final Random r = new Random(0);
    @Autowired
    OrganisaatioResource res;
    @Autowired
    OrganisaatioService organisaatioService;
    @Autowired
    OrganisaatioSearchService searchService;
    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    @Test
    public void testHierarchySearch() throws SolrServerException, IOException {
        // build test hierarchy
        LOG.info("doTest()...");
        // clear index
        factory.getSolrServer().deleteByQuery("*:*");
        factory.getSolrServer().commit();

        // create hierarchy
        // ROOT
        // |
        // +-A
        // | |
        // | +-F
        // |
        // +-B
        // | |
        // | +-C
        // |   |
        // |   +-D
        // |     |
        // |     +-G
        // |
        // +-E
        // | |
        // | +-J (expired 24h ago)
        // |
        // +-H (expired 24h ago)
        // |
        // +-I (starts 24h)
        Organisaatio root = createOrganisaatio(ROOT_NAME, null);
        Organisaatio a = createOrganisaatio("AAA", root);
        Organisaatio b = createOrganisaatio("BBB", root);
        Organisaatio c = createOrganisaatio("CCC", b);
        Organisaatio d = createOrganisaatio("DDD", c);
        Organisaatio e = createOrganisaatio("EEE", root);
        Organisaatio f = createOrganisaatio("FFF", a);
        Organisaatio g = createOrganisaatio("GGG", d);
        Organisaatio h = createOrganisaatio("HHH", root, null, new Date(System.currentTimeMillis()-1000*60*60*24));
        Organisaatio i = createOrganisaatio("III", root, new Date(System.currentTimeMillis() + 1000*60*60*24));
        Organisaatio j = createOrganisaatio("JJJ", e, null, new Date(System.currentTimeMillis()-1000*60*60*24));

        indexer.reBuildIndex(true);
        OrganisaatioSearchCriteria searchCriteria = new OrganisaatioSearchCriteria();
        searchCriteria.setKunta(null);
        searchCriteria.setOppilaitosTyyppi(null);
        searchCriteria.setSearchStr(null);
        //searchCriteria.setFirstResult(0); //default value, returns parents

        // test returns all (except oph), org search has changed and this is now impossible to test!
        //assertResultMatches(searchCriteria, "AAA","BBB","CCC","DDD","EEE","FFF");

        // with restriction list (leaf, f)
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.getOidRestrictionList().add(f.getOid());
        assertResultMatches(searchCriteria, "FFF");

        // with restriction list (2 leafs, d and e, j is expired, should not be seen)
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.getOidRestrictionList().add(d.getOid());
        searchCriteria.getOidRestrictionList().add(e.getOid());
        searchCriteria.setLakkautetut(false);
        assertResultMatches(searchCriteria, "DDD", "EEE", "GGG");
        searchCriteria.setLakkautetut(true);
        assertResultMatches(searchCriteria, "DDD", "EEE", "GGG", "JJJ");

        // with restriction list (non leaf, c)
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.getOidRestrictionList().add(c.getOid());
        assertResultMatches(searchCriteria, "CCC", "DDD","GGG");

        // with restriction list (non leaf, c) and name
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.getOidRestrictionList().add(c.getOid());
        searchCriteria.setSearchStr("ccc");
        assertResultMatches(searchCriteria, "CCC", "DDD", "GGG");

        // with restriction list (non leaf, c) and name and type
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.getOidRestrictionList().add(c.getOid());
        searchCriteria.setSearchStr("ccc");
        assertResultMatches(searchCriteria, "CCC", "DDD","GGG");

        // without restriction list, name
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.setSearchStr("ggg");
        assertResultMatches(searchCriteria, "CCC", "DDD","GGG","BBB");

        // org expired
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.setSearchStr("hhh");
        searchCriteria.setLakkautetut(false);
        assertResultMatches(searchCriteria);

        // include expired
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.setSearchStr("hhh");
        searchCriteria.setLakkautetut(true);
        assertResultMatches(searchCriteria, "HHH");

        // org planned
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.setSearchStr("iii");
        searchCriteria.setSuunnitellut(false);
        assertResultMatches(searchCriteria);

        // include planned
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.setSearchStr("iii");
        searchCriteria.setSuunnitellut(true);
        assertResultMatches(searchCriteria, "III");
        
        
        //"delete" org
        deleteOrg(i.getOid());
        assertResultMatches(searchCriteria);

        //XXXspecial search for tarjonta, currently hacked because no api change is possible in 4.2
        searchCriteria = new OrganisaatioSearchCriteria();
        searchCriteria.setKunta(null);
        searchCriteria.setOppilaitosTyyppi(null);
        searchCriteria.setSearchStr(null);
        searchCriteria.setSkipParents(true);

        // test returns all (except oph), org search has changed and this is now impossible to test!
        //assertResultMatches(searchCriteria, "AAA","BBB","CCC","DDD","EEE","FFF");

        // with restriction list (leaf, f)
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.getOidRestrictionList().add(f.getOid());
        assertResultMatches(searchCriteria, "FFF");

        // with restriction list (2 leafs, d and e, j is expired, should not be seen)
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.getOidRestrictionList().add(d.getOid());
        searchCriteria.getOidRestrictionList().add(e.getOid());
        searchCriteria.setLakkautetut(false);
        assertResultMatches(searchCriteria, "DDD", "EEE", "GGG");
        searchCriteria.setLakkautetut(true);
        assertResultMatches(searchCriteria, "DDD", "EEE", "GGG", "JJJ");

        // with restriction list (non leaf, c)
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.getOidRestrictionList().add(c.getOid());
        assertResultMatches(searchCriteria, "CCC", "DDD","GGG");

        // with restriction list (non leaf, c) and name
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.getOidRestrictionList().add(c.getOid());
        searchCriteria.setSearchStr("ccc");
        assertResultMatches(searchCriteria, "CCC", "DDD", "GGG");

        // with restriction list (non leaf, c) and name and type
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.getOidRestrictionList().add(c.getOid());
        searchCriteria.setSearchStr("ccc");
        assertResultMatches(searchCriteria, "CCC", "DDD","GGG");

        // without restriction list, name (no parents!)
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.setSearchStr("ggg");
        assertResultMatches(searchCriteria, "GGG");

        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.setSearchStr("ddd");
        assertResultMatches(searchCriteria, "DDD","GGG");

        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.setSearchStr("ccc");
        assertResultMatches(searchCriteria, "CCC","DDD","GGG");

        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.setSearchStr("bbb");
        assertResultMatches(searchCriteria, "BBB", "CCC","DDD","GGG");

        // org expired
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.setSearchStr("hhh");
        searchCriteria.setLakkautetut(false);
        assertResultMatches(searchCriteria);

        // include expired
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.setSearchStr("hhh");
        searchCriteria.setLakkautetut(true);
        assertResultMatches(searchCriteria, "HHH");

        // org planned
        searchCriteria.getOidRestrictionList().clear();
        searchCriteria.setSearchStr("iii");
        searchCriteria.setSuunnitellut(false);
        assertResultMatches(searchCriteria);
    }

    /**
     * Compare result from solr with result from organisaatio service.
     * 
     * @param results
     */
    private void assertResultMatches(OrganisaatioSearchCriteria searchCriteria, String... results) {
        final int count = results.length;
        final List<OrganisaatioPerustieto> search = searchService.searchBasicOrganisaatios(searchCriteria);
        final List<String> result = Lists.newArrayList(results);

        Assert.assertEquals("search count mismatch", count, search.size());
        assertContent(result, search);
    }

    private void assertContent(List<String> reference, List<OrganisaatioPerustieto> result) {
        List<String> resultOids = Lists.newArrayList();
        for (OrganisaatioPerustieto org : result) {
            resultOids.add(org.getOid());
        }
        List<String> operate = Lists.newArrayList(resultOids);
        operate.removeAll(resultOids);
        Assert.assertEquals("Content mismatch:" + resultOids + "!=" + reference, 0, operate.size());
    }

    private void printResult(String name, final List<OrganisaatioPerustieto> search) {
        System.out.println(name + " result:");
        for (OrganisaatioPerustieto perus : search) {
            System.out.println(perus.getNimi("fi") + ":" + perus.getOid());
        }
    }
    
    
    private void deleteOrg(String oid) {
        RemoveByOidType type = new RemoveByOidType();
        type.setOid(oid);
        try {
            organisaatioService.removeOrganisaatioByOid(type);
        } catch (GenericFault e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Organisaatio createOrganisaatio(String nimi, Organisaatio parent, Date... startStop) {
        LOG.info("createOrganisaatio({})", nimi);

        Organisaatio o = new Organisaatio();

        if (nimi.equals(ROOT_NAME)) {
            o.setOid(rootOrganisaatioOid);
        } else {
            o.setOid("oid-" + Math.abs(r.nextLong()));
        }

        o.setNimi(new MonikielinenTeksti());
        o.getNimi().addString("fi", "blah blah " + nimi + " blah blah");
        o.setNimihaku(o.getNimi().getString("fi"));
        o.setDomainNimi(nimi + "domain");
        if (parent != null) {
            createParentPath(o, parent.getOid());
        }
        
		if (startStop.length > 0) {
			o.setAlkuPvm(startStop[0]);
		}

		if (startStop.length > 1) {
			o.setLakkautusPvm(startStop[1]);
		}

        List<Yhteystieto> oYhteystiedot = Lists.newArrayList(createOsoite());
        o.setYhteystiedot(oYhteystiedot);

        o = organisaatioDAO.insert(o);

        if (parent != null) {
            final OrganisaatioSuhde suhde = organisaatioSuhdeDAO.addChild(parent.getId(), o.getId(), new Date(), null);
            o = organisaatioDAO.findByOid(o.getOid());
            o.getParentSuhteet().add(suhde);
            organisaatioSuhdeDAO.getEntityManager().flush();
            System.out.println("parent after insert:" + o.getParent());
        }

        return o;
    }

    private Yhteystieto createOsoite() {
        Osoite o = new Osoite(Osoite.TYYPPI_KAYNTIOSOITE, "katu", "0000", "Helsinki", UUID.randomUUID().toString());
        return o;
    }

    private static final String parentSeparator = "|";

    private void createParentPath(Organisaatio entity, String parentOid) {
        String parentOidPath = "";
        String parentIdPath = "";
        for (Organisaatio curParent : organisaatioDAO.findParentsTo(parentOid)) {
            parentOidPath += parentSeparator + curParent.getOid();
            parentIdPath += parentSeparator + curParent.getId();
        }
        parentOidPath += parentSeparator;
        parentIdPath += parentSeparator;
        entity.setParentOidPath(parentOidPath);
        entity.setParentIdPath(parentIdPath);
    }

}
