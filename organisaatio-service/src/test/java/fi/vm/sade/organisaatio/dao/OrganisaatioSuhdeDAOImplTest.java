package fi.vm.sade.organisaatio.dao;

import fi.vm.sade.organisaatio.dao.impl.OrganisaatioSuhdeDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.OrganisaatioDAOImpl;
import fi.vm.sade.log.client.LoggerHelper;
import fi.vm.sade.log.client.LoggerMock;
import fi.vm.sade.log.model.Tapahtuma;
import fi.vm.sade.organisaatio.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;

/**
 * @author mlyly
 */
@ContextConfiguration(locations = {
        "classpath:spring/test-context.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class OrganisaatioSuhdeDAOImplTest {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioSuhdeDAOImplTest.class);

    @Autowired
    OrganisaatioSuhdeDAOImpl organisaatioSuhdeDAO;

    @Autowired
    OrganisaatioDAOImpl organisaatioDAO;

    @Before
    public void onBefore() {
        LoggerHelper.init(new LoggerMock());
    }

    @After
    public void onAfter() {
        Tapahtuma t = LoggerHelper.getAuditRootTapahtuma();
        t.setHost("NA");
        t.setSystem("NA");
        t.setTarget("NA");
        t.setTargetType("NA");
        t.setType("NA");
        t.setUser("NA");
        t.setUserActsForUser("NA");

        LoggerHelper.log();
    }

    @Test
    public void doTest() {
        LOG.info("doTest()...");

        //Organisaatio that resembles the hidden root organisation (i.e. the parent of roots)
        Organisaatio OPH = createOrganisaatio("OPH");

        Date t = createDate("1.1.2000");

        Organisaatio a = createOrganisaatio("A");
        organisaatioSuhdeDAO.addChild(OPH.getId(), a.getId(), t, null);
        Organisaatio b = createOrganisaatio("B");
        organisaatioSuhdeDAO.addChild(OPH.getId(), b.getId(), t, null);
        Organisaatio c = createOrganisaatio("C");
        organisaatioSuhdeDAO.addChild(OPH.getId(), c.getId(), t, null);
        Organisaatio d = createOrganisaatio("D");
        organisaatioSuhdeDAO.addChild(OPH.getId(), d.getId(), t, null);
        Organisaatio e = createOrganisaatio("E");
        organisaatioSuhdeDAO.addChild(OPH.getId(), e.getId(), t, null);

        printOrganisaatioSuhdeTable();


        t = createDate("1.1.2001");
        organisaatioSuhdeDAO.addChild(a.getId(), b.getId(), t, null);
        printOrganisaatioSuhdeTable();
        Assert.assertTrue(verifyChildren(t, a.getId(), b.getId()));

        t = createDate("2.1.2001");
        organisaatioSuhdeDAO.addChild(a.getId(), c.getId(), t, null);
        printOrganisaatioSuhdeTable();
        Assert.assertTrue(verifyChildren(t, a.getId(), b.getId(), c.getId()));

        t = createDate("3.1.2001");
        organisaatioSuhdeDAO.addChild(d.getId(), e.getId(), t, null);
        printOrganisaatioSuhdeTable();
        Assert.assertTrue(verifyChildren(t, d.getId(), e.getId()));

        t = createDate("4.1.2001");
        organisaatioSuhdeDAO.addChild(OPH.getId(), e.getId(), t, null);
        printOrganisaatioSuhdeTable();
        Assert.assertTrue(verifyChildren(t, d.getId()));

        t = createDate("5.1.2001");
        organisaatioSuhdeDAO.addChild(c.getId(), b.getId(), t, null);
        printOrganisaatioSuhdeTable();
        Assert.assertTrue(verifyChildren(t, c.getId(), b.getId()));

        t = createDate("6.1.2001");
        organisaatioSuhdeDAO.addChild(c.getId(), d.getId(), t, null);
        printOrganisaatioSuhdeTable();
        Assert.assertTrue(verifyChildren(t, c.getId(), b.getId(), d.getId()));

        //
        // Test "history"
        //

        // "before everything"
        t = createDate("1.1.2000");
        Assert.assertTrue(verifyChildren(t, a.getId()));
        Assert.assertTrue(verifyChildren(t, b.getId()));
        Assert.assertTrue(verifyChildren(t, c.getId()));
        Assert.assertTrue(verifyChildren(t, d.getId()));
        Assert.assertTrue(verifyChildren(t, e.getId()));

        t = createDate("1.1.2001");
        // after a -> b
        Assert.assertTrue(verifyChildren(t, a.getId(), b.getId()));
        Assert.assertTrue(verifyChildren(t, b.getId()));
        Assert.assertTrue(verifyChildren(t, c.getId()));
        Assert.assertTrue(verifyChildren(t, d.getId()));
        Assert.assertTrue(verifyChildren(t, e.getId()));

        t = createDate("2.1.2001");
        // after a -> b
        // after a -> c
        Assert.assertTrue(verifyChildren(t, a.getId(), b.getId(), c.getId()));
        Assert.assertTrue(verifyChildren(t, b.getId()));
        Assert.assertTrue(verifyChildren(t, c.getId()));
        Assert.assertTrue(verifyChildren(t, d.getId()));
        Assert.assertTrue(verifyChildren(t, e.getId()));

        t = createDate("3.1.2001");
        // after a -> b
        // after a -> c
        // after d -> e
        Assert.assertTrue(verifyChildren(t, a.getId(), b.getId(), c.getId()));
        Assert.assertTrue(verifyChildren(t, b.getId()));
        Assert.assertTrue(verifyChildren(t, c.getId()));
        Assert.assertTrue(verifyChildren(t, d.getId(), e.getId()));
        Assert.assertTrue(verifyChildren(t, e.getId()));

        t = createDate("4.1.2001");
        // after a -> b
        // after a -> c
        // after d -> e  this removed
        Assert.assertTrue(verifyChildren(t, a.getId(), b.getId(), c.getId()));
        Assert.assertTrue(verifyChildren(t, b.getId()));
        Assert.assertTrue(verifyChildren(t, c.getId()));
        Assert.assertTrue(verifyChildren(t, d.getId()));
        Assert.assertTrue(verifyChildren(t, e.getId()));

        t = createDate("5.1.2001");
        // after a -> b  this removed
        // after a -> c
        // after d -> e  this removed
        // after c -> b
        Assert.assertTrue(verifyChildren(t, a.getId(), c.getId()));
        Assert.assertTrue(verifyChildren(t, b.getId()));
        Assert.assertTrue(verifyChildren(t, c.getId(), b.getId()));
        Assert.assertTrue(verifyChildren(t, d.getId()));
        Assert.assertTrue(verifyChildren(t, e.getId()));

        t = createDate("6.1.2001");
        // after a -> b  this removed
        // after a -> c
        // after d -> e  this removed
        // after c -> b
        // after c -> d
        Assert.assertTrue(verifyChildren(t, a.getId(), c.getId()));
        Assert.assertTrue(verifyChildren(t, b.getId()));
        Assert.assertTrue(verifyChildren(t, c.getId(), b.getId(), d.getId()));
        Assert.assertTrue(verifyChildren(t, d.getId()));
        Assert.assertTrue(verifyChildren(t, e.getId()));

        printOrganisaatioSuhdeTable();

        t = new Date();
        this.organisaatioSuhdeDAO.remove(this.organisaatioSuhdeDAO.findParentTo(e.getId(), new Date()));
        Assert.assertTrue(verifyChildren(t, d.getId(), e.getId()));


        //
        // Test organisation name history
        //

        // Save test data
        organisaatioDAO.addHistoriaMetadata(a.getId(), "nimi", "FI", createDate("1.1.2011"), "a 1.1.2011");
        organisaatioDAO.addHistoriaMetadata(a.getId(), "nimi", "FI", createDate("1.3.2011"), "aa 1.3.2011");
        organisaatioDAO.addHistoriaMetadata(a.getId(), "nimi", "FI", createDate("1.6.2011"), "aaa 1.6.2011");
        organisaatioDAO.addHistoriaMetadata(a.getId(), "nimi", "FI", createDate("1.9.2011"), "A 1.9.2011");
        organisaatioDAO.addHistoriaMetadata(a.getId(), "nimi", "FI", createDate("1.12.2011"), "AA 1.12.2011");
        organisaatioDAO.addHistoriaMetadata(a.getId(), "nimi", "FI", createDate("1.3.2012"), "AAA 1.3.2012");

        HistoryMetadata hmd = null;

        // Future == AAA 1.3.2012
        hmd = organisaatioDAO.findPreviousValidHistoryMetadata(a.getId(), "nimi", "FI", createDate("1.3.2013"));
        Assert.assertEquals("AAA 1.3.2012", hmd.getArvo());

        // 2.1.2011 == a 1.1.2011
        hmd = organisaatioDAO.findPreviousValidHistoryMetadata(a.getId(), "nimi", "FI", createDate("2.3.2011"));
        Assert.assertEquals("aa 1.3.2011", hmd.getArvo());

        // 2.6.2011 == aaa 1.6.2011
        hmd = organisaatioDAO.findPreviousValidHistoryMetadata(a.getId(), "nimi", "FI", createDate("2.6.2011"));
        Assert.assertEquals("aaa 1.6.2011", hmd.getArvo());

        // 2.12.2011 == AA 1.12.2011
        hmd = organisaatioDAO.findPreviousValidHistoryMetadata(a.getId(), "nimi", "FI", createDate("2.12.2011"));
        Assert.assertEquals("AA 1.12.2011", hmd.getArvo());

        LOG.info("doTest()... done.");
    }

    private void printOrganisaatioSuhdeTable() {
        List<OrganisaatioSuhde> oss = organisaatioSuhdeDAO.findAll();

        LOG.info("ORGANISAATIOSUHDE TABLE:");
        for (OrganisaatioSuhde os : oss) {
            LOG.info("  OS: pId={}, cId={}, a={}, l={}",
                    new Object[]{os.getParent().getId(), os.getChild().getId(), os.getAlkuPvm(), os.getLoppuPvm()});
        }
    }

    private boolean verifyChildren(Date d, Long parentId, Long... children) {
        List<OrganisaatioSuhde> oss = organisaatioSuhdeDAO.findChildrenTo(parentId, d);

        if (oss.size() != children.length) {
            LOG.info("verifyChildren : expected {} != actual {} ???", children.length, oss.size());
            return false;
        }

        for (Long childId : children) {
            OrganisaatioSuhde os = organisaatioSuhdeDAO.findParentTo(childId, d);
            if (os == null || os.getParent().getId() != parentId) {
                LOG.info("Invalid parent! expected: {}, was {}", parentId, (os != null) ? os.getParent().getId() : "NULL");
                return false;
            }
        }

        return true;
    }

    private Date createDate(String s) {
        LOG.info("-------------------------------- createDate({})", s);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            return sdf.parse(s);
        } catch (ParseException e) {
            Assert.fail("Invalid date in test: " + s);
            return null;
        }
    }

    private Organisaatio createOrganisaatio(String nimi) {
        LOG.info("createOrganisaatio({})", nimi);

        Organisaatio o = new Organisaatio();

        o.setNimi(new MonikielinenTeksti());
        o.getNimi().addString("FI", nimi);

        List<Yhteystieto> oYhteystiedot = new ArrayList<Yhteystieto>();
        oYhteystiedot.add(createOsoite());
        o.setYhteystiedot(oYhteystiedot);

        organisaatioDAO.insert(o);

        return o;
    }

    private Osoite createOsoite() {
        Osoite o = new Osoite(Osoite.TYYPPI_KAYNTIOSOITE, "katu", "0000", "Helsinki", UUID.randomUUID().toString());
        return o;
    }

}
