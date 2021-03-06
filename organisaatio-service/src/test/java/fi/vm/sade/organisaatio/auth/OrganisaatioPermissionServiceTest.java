package fi.vm.sade.organisaatio.auth;

import java.util.List;

import com.google.common.collect.Lists;

import fi.vm.sade.security.OidProvider;
import fi.vm.sade.security.OrganisationHierarchyAuthorizer;
import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;

//TODO combine permission service tests and make this a proper unit test
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
public class OrganisaatioPermissionServiceTest {

    public String rootOrgOid ="root";

    public static final String userOid = "nimi";
    public static final String userOrgOid = "1.2.2004.2";
    public static final String otherOrgOid = "1.2.2005.2";


    private OrganisaatioPermissionServiceImpl permissionService = new OrganisaatioPermissionServiceImpl(rootOrgOid);

    @Test
    public void testBasic() {

        OidProvider oidProvider = Mockito.mock(OidProvider.class);
        Mockito.stub(oidProvider.getSelfAndParentOids(otherOrgOid)).toReturn(
                Lists.newArrayList(rootOrgOid, otherOrgOid));
        Mockito.stub(oidProvider.getSelfAndParentOids(userOrgOid)).toReturn(
                Lists.newArrayList(rootOrgOid, userOrgOid));
        Mockito.stub(oidProvider.getSelfAndParentOids(rootOrgOid)).toReturn(
                Lists.newArrayList(rootOrgOid));
        OrganisationHierarchyAuthorizer authorizer = new OrganisationHierarchyAuthorizer(oidProvider);
        permissionService.setAuthorizer(authorizer);


        //non oph user, outside own hierarchy
        setCurrentUser(userOid, Lists.newArrayList(getAuthority(
                permissionService.ROLE_CRUD, userOrgOid)));
        OrganisaatioDTO org = getOrganisaatio(userOid, otherOrgOid, OrganisaatioTyyppi.KOULUTUSTOIMIJA);
        Assert.assertFalse(permissionService.userCanCreateRootOrganisation());
        Assert.assertFalse(permissionService.userCanUpdateYTJ());
        Assert.assertFalse(permissionService.userCanCreateOrganisation(OrganisaatioContext.get(org)));
        Assert.assertFalse(permissionService.userCanDeleteOrganisation(OrganisaatioContext.get(org)));
        Assert.assertFalse(permissionService.userCanUpdateOrganisation(OrganisaatioContext.get(org)));
        Assert.assertFalse(permissionService.userCanMoveOrganisation(OrganisaatioContext.get(org)));

        //yhteystietojentyyppi
        Assert.assertFalse(permissionService.userCanEditYhteystietojenTyypit());
        Assert.assertFalse(permissionService.userCanDeleteYhteystietojenTyyppi());

        //alku, loppupäivä
        Assert.assertFalse(permissionService.userCanEditDates(OrganisaatioContext.get(org)));

        //non oph user inside own hierarchy
        setCurrentUser(userOid, Lists.newArrayList(getAuthority(
                permissionService.ROLE_CRUD, userOrgOid)));
        org = getOrganisaatio(userOid, userOrgOid, OrganisaatioTyyppi.OPPILAITOS);
        Assert.assertTrue(permissionService.userCanCreateOrganisation(OrganisaatioContext.get(org)));
        Assert.assertFalse(permissionService.userCanDeleteOrganisation(OrganisaatioContext.get(org)));
        Assert.assertTrue(permissionService.userCanUpdateOrganisation(OrganisaatioContext.get(org)));
        Assert.assertTrue(permissionService.userCanMoveOrganisation(OrganisaatioContext.get(org)));

        assertEditOrganisation(OrganisaatioTyyppi.MUU_ORGANISAATIO, false);
        assertEditOrganisation(OrganisaatioTyyppi.KOULUTUSTOIMIJA, true);
        assertEditOrganisation(OrganisaatioTyyppi.TOIMIPISTE, true);
        assertEditOrganisation(OrganisaatioTyyppi.OPPILAITOS, true);

        //sallitut tyypit
        Assert.assertFalse(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.KOULUTUSTOIMIJA));
        Assert.assertFalse(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.OPPILAITOS));
        Assert.assertFalse(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.MUU_ORGANISAATIO));
        Assert.assertTrue(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.TOIMIPISTE));

        //nimen muokkaus
        Assert.assertFalse(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.KOULUTUSTOIMIJA))));
        Assert.assertFalse(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.OPPILAITOS))));
        Assert.assertFalse(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.MUU_ORGANISAATIO))));
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.TOIMIPISTE))));

        //alku, loppupäivä
        Assert.assertFalse(permissionService.userCanEditDates(OrganisaatioContext.get(org)));

        //non oph user, opetuspiste
        setCurrentUser(userOid, Lists.newArrayList(getAuthority(permissionService.ROLE_CRUD,userOrgOid)));
        org = getOrganisaatio(userOid, userOrgOid, OrganisaatioTyyppi.TOIMIPISTE);
        //alku, loppupäivä
        Assert.assertTrue(permissionService.userCanEditDates(OrganisaatioContext.get(org)));

        //oph CRUD user
        setCurrentUser(userOid, Lists.newArrayList(getAuthority(permissionService.ROLE_CRUD,  rootOrgOid)));

        //can edit all types
        assertEditOrganisation(OrganisaatioTyyppi.MUU_ORGANISAATIO, true);
        assertEditOrganisation(OrganisaatioTyyppi.KOULUTUSTOIMIJA, true);
        assertEditOrganisation(OrganisaatioTyyppi.TOIMIPISTE, true);
        assertEditOrganisation(OrganisaatioTyyppi.OPPILAITOS, true);


        //yhteystietojentyyppi
        Assert.assertTrue(permissionService.userCanEditYhteystietojenTyypit());
        Assert.assertTrue(permissionService.userCanDeleteYhteystietojenTyyppi());

        //alku, loppupäivä
        Assert.assertTrue(permissionService.userCanEditDates(OrganisaatioContext.get(org)));

        //sallitut tyyppit
        Assert.assertTrue(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.KOULUTUSTOIMIJA));
        Assert.assertTrue(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.OPPILAITOS));
        Assert.assertTrue(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.MUU_ORGANISAATIO));
        Assert.assertTrue(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.TOIMIPISTE));

        //nimen muokkaus
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.KOULUTUSTOIMIJA))));
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.OPPILAITOS))));
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.MUU_ORGANISAATIO))));
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.TOIMIPISTE))));

        //oph RU user
        setCurrentUser(userOid, Lists.newArrayList(getAuthority(permissionService.ROLE_RU,  rootOrgOid)));

        //yhteystietojentyyppi
        Assert.assertTrue(permissionService.userCanEditYhteystietojenTyypit());
        Assert.assertFalse(permissionService.userCanDeleteYhteystietojenTyyppi());

        //alku, loppupäivä
        Assert.assertTrue(permissionService.userCanEditDates(OrganisaatioContext.get(org)));

        //sallitut tyyppit
        Assert.assertFalse(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.KOULUTUSTOIMIJA));
        Assert.assertFalse(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.OPPILAITOS));
        Assert.assertFalse(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.MUU_ORGANISAATIO));
        Assert.assertFalse(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.TOIMIPISTE));

        //nimen muokkaus
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.KOULUTUSTOIMIJA))));
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.OPPILAITOS))));
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.MUU_ORGANISAATIO))));
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.TOIMIPISTE))));

        assertEditOrganisation(OrganisaatioTyyppi.MUU_ORGANISAATIO, false);
        assertEditOrganisation(OrganisaatioTyyppi.KOULUTUSTOIMIJA, true);
        assertEditOrganisation(OrganisaatioTyyppi.TOIMIPISTE, true);
        assertEditOrganisation(OrganisaatioTyyppi.OPPILAITOS, true);

    }

    private void assertEditOrganisation(OrganisaatioTyyppi tyyppi, boolean expectedResult) {
        OrganisaatioDTO org = getOrganisaatio(userOid, userOrgOid, tyyppi);
        Assert.assertEquals(expectedResult, permissionService.userCanUpdateOrganisation(OrganisaatioContext.get(org)));
    }

    private OrganisaatioDTO getOrganisaatio(String nimi, String oid, OrganisaatioTyyppi tyyppi) {
        OrganisaatioDTO org = new OrganisaatioDTO();
        org.setNimi(fi.vm.sade.organisaatio.helper.Util.getMonikielinenTekstiTyyppi("fi",nimi));
        org.getTyypit().add(tyyppi);
        org.setOid(oid);
        return org;
    }

    List<GrantedAuthority> getAuthority(String appPermission, String oid) {
        GrantedAuthority orgAuthority = new SimpleGrantedAuthority(String.format("%s", appPermission));
        GrantedAuthority roleAuthority = new SimpleGrantedAuthority(String.format("%s_%s", appPermission, oid));
        return Lists.newArrayList(orgAuthority, roleAuthority);
    }

    static void setCurrentUser(final String oid, final List<GrantedAuthority> grantedAuthorities) {

        Authentication auth = new TestingAuthenticationToken(oid, null, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }



}
