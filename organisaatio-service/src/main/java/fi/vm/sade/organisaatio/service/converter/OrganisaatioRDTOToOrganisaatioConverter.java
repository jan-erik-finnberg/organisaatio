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

import fi.vm.sade.generic.service.conversion.AbstractToDomainConverter;
import fi.vm.sade.organisaatio.model.Email;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.Osoite;
import fi.vm.sade.organisaatio.model.Puhelinnumero;
import fi.vm.sade.organisaatio.model.Www;
import fi.vm.sade.organisaatio.model.Yhteystieto;
import fi.vm.sade.organisaatio.model.lop.BinaryData;
import fi.vm.sade.organisaatio.model.lop.NamedMonikielinenTeksti;
import fi.vm.sade.organisaatio.model.lop.OrganisaatioMetaData;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioMetaDataRDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.solr.common.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rsal
 */
public class OrganisaatioRDTOToOrganisaatioConverter extends AbstractToDomainConverter<OrganisaatioRDTO, Organisaatio> {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioRDTOToOrganisaatioConverter.class);

    @Override
    public Organisaatio convert(OrganisaatioRDTO t) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        List<Yhteystieto> yhteystietos = new ArrayList<Yhteystieto>();
        Organisaatio s = new Organisaatio();

        s.setOid(t.getOid());
        s.setVersion((long)t.getVersion());

        s.setAlkuPvm(t.getAlkuPvm());
        // t.setChildCount(s.getChildCount());
        s.setDomainNimi(t.getDomainNimi());

        s.setKielet(convertListToList(t.getKieletUris()));
        s.setKotipaikka(t.getKotipaikkaUri());
        s.setKuvaus(s.getKuvaus());
        s.setKuvaus2(convertMapToMonikielinenTeksti(t.getKuvaus2()));
        s.setLakkautusPvm(t.getLakkautusPvm());
        s.setMaa(t.getMaaUri());
        s.setMetadata(convertMetadata(t.getMetadata()));
        s.setNimi(convertMapToMonikielinenTeksti(t.getNimi()));
        // t.set(s.getNimiLyhenne());
        t.setOpetuspisteenJarjNro(s.getOpetuspisteenJarjNro());
        s.setOppilaitosKoodi(s.getOppilaitosKoodi());
        s.setOppilaitosTyyppi(t.getOppilaitosTyyppiUri());
        // t.set(s.getOrganisaatiotyypitStr());
        // s.setParentOid(s.getParent() != null ? s.getParent().getOid() : null);
        // t.set(s.getParentIdPath());
        // t.setParentMetadata(s.getParentMetadata());
        s.setParentOidPath(s.getParentOidPath());
        // t.set(s.getParentSuhteet());

        // t.set(s.getPuhelin());
        // t.set(s.getSopimusKunnat()); -- non existing old ui functionality has left it's marks...
        s.setToimipisteKoodi(t.getToimipistekoodi());
        s.setTyypit(convertListToList(t.getTyypit()));
        // t.set(s.getTyypitAsString());
        s.setVuosiluokat(convertListToList(t.getVuosiluokat()));
        s.setYhteishaunKoulukoodi(t.getYhteishaunKoulukoodi());
        // t.set(s.getYhteystiedot());
        // t.set(s.getYhteystietoArvos());
        s.setYritysmuoto(t.getYritysmuoto());
        s.setYtjPaivitysPvm(t.getYTJPaivitysPvm());
        s.setYtunnus(t.getYTunnus());
        s.setVirastoTunnus(t.getVirastoTunnus());

        // Get dynamic Yhteysieto / Yhteystietotyppie / Elementti data
        //List<Map<String, String>> yhteystietoArvos = new ArrayList<Map<String, String>>();
        //t.setYhteystietoArvos(yhteystietoArvos);

        for (Map<String, String> m : t.getYhteystiedot()) {
            Yhteystieto y = convertYhteystietoGeneric(m);
            if (y != null) {
                yhteystietos.add(y);
            }
        }
        s.setYhteystiedot(yhteystietos);

        return s;
    }

    private Osoite convertMapToOsoite(Map<String, String> s, String tyyppi) {
        if (s == null) {
            return null;
        }

        Osoite t = new Osoite();
        t.setOsoiteTyyppi(tyyppi);
        if (s.containsKey("coordinateType")) {
            t.setCoordinateType(s.get("coordinateType"));
        }
        if (s.containsKey("extraRivi")) {
            t.setExtraRivi(s.get("extraRivi"));
        }
        if (s.containsKey("maaUri")) {
            t.setMaa(s.get("maaUri"));
        }
        if (s.containsKey("osavaltio")) {
            t.setOsavaltio(s.get("osavaltio"));
        }
        if (s.containsKey("osoite")) {
            t.setOsoite(s.get("osoite"));
        }
        if (s.containsKey("osoiteTyyppi")) {
            t.setOsoiteTyyppi(s.get("osoiteTyyppi"));
        }
        if (s.containsKey("postinumeroUri")) {
            t.setPostinumero(s.get("postinumeroUri"));
        }
        if (s.containsKey("postitoimipaikka")) {
            t.setPostitoimipaikka(s.get("postitoimipaikka"));
        }
        if (s.containsKey("yhteystietoOid")) {
            t.setYhteystietoOid(s.get("yhteystietoOid"));
        }
        if (s.get("lap") != null) {
            try {
                t.setLat(Double.parseDouble(s.get("lap")));
            }
            catch (NumberFormatException nfe) {
                // just don't set it then
            }
        }
        if (s.get("lng") != null) {
            try {
                t.setLng(Double.parseDouble(s.get("lng")));
            }
            catch (NumberFormatException nfe) {
                // just don't set it then
            }
        }
        if (s.get("ytjPaivitysPvm") != null) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            try {
                t.setYtjPaivitysPvm(df.parse(s.get("ytjPaivitysPvm")));
            }
            catch (ParseException pe) {
                // just don't set it then
            }
        }

        return t;
    }

    private MonikielinenTeksti convertMapToMonikielinenTeksti(Map<String, String> m) {
        MonikielinenTeksti mt = new MonikielinenTeksti();
        for (Map.Entry<String, String> e : m.entrySet()) {
            mt.addString(e.getKey(), e.getValue());
        }
        return mt;
    }

    private Puhelinnumero convertPuhelinnumero(String numero, String tyyppi) {
        Puhelinnumero p = new Puhelinnumero();
        p.setPuhelinnumero(numero);
        p.setTyyppi(tyyppi);
        return p;
    }

    private Www convertWww(String wwwOsoite) {
        Www www = new Www();
        www.setWwwOsoite(wwwOsoite);
        return www;
    }

    private Email convertEmail(String emailOsoite) {
        Email email = new Email();
        email.setEmail(emailOsoite);
        return email;
    }

    private List<String> convertListToList(List<String> a) {
        return new ArrayList<String>(a);
    }

    private BinaryData decodeFromUUENCODED(String kuva) {
        if (kuva == null || kuva.isEmpty()) {
            return null;
        }
        BinaryData bd = new BinaryData();
        bd.setData(Base64.base64ToByteArray(kuva));
        return bd;
    }

    private OrganisaatioMetaData convertMetadata(OrganisaatioMetaDataRDTO t) {
        if (t == null) {
            return null;
        }

        OrganisaatioMetaData s = new OrganisaatioMetaData();

        s.setHakutoimistoEctsEmail(t.getHakutoimistoEctsEmail());
        s.setHakutoimistoEctsNimi(t.getHakutoimistoEctsNimi());
        s.setHakutoimistoEctsPuhelin(t.getHakutoimistoEctsPuhelin());
        s.setHakutoimistoEctsTehtavanimike(t.getHakutoimistoEctsTehtavanimike());
        s.setHakutoimistoNimi(convertMapToMonikielinenTeksti(t.getHakutoimistonNimi()));
        s.setKoodi(t.getKoodi());
        s.setKuva(decodeFromUUENCODED(t.getKuvaEncoded()));
        s.setLuontiPvm(t.getLuontiPvm());
        s.setMuokkausPvm(t.getMuokkausPvm());
        s.setNimi(convertMapToMonikielinenTeksti(t.getNimi()));

        for (Map<String, String> yhteystieto : t.getYhteystiedot()) {
            s.getYhteystiedot().add(convertYhteystietoGeneric(yhteystieto));
        }

        Set<NamedMonikielinenTeksti> nmtSet = new HashSet<NamedMonikielinenTeksti>();
        for (Map.Entry<String, Map<String, String>> e : t.getData().entrySet()) {
            NamedMonikielinenTeksti nmt = new NamedMonikielinenTeksti();
            nmt.setKey(e.getKey());
            nmt.setValue(convertMapToMonikielinenTeksti(e.getValue()));
            nmtSet.add(nmt);
        }
        s.setValues(nmtSet);

        return s;
    }

    private Yhteystieto convertYhteystietoGeneric(Map<String, String> s) {
        if (s != null) {
            try {
                if (s.containsKey("email")) {
                    Email v = convertEmail(s.get("email"));
                    if (s.containsKey("yhteystietoOid")) {
                        v.setId(Long.parseLong(s.get("id")));
                        v.setYhteystietoOid(s.get("yhteystietoOid"));
                    }
                    return v;
                } else if (s.containsKey("www")) {
                    Www v = convertWww(s.get("www"));
                    if (s.containsKey("yhteystietoOid")) {
                        v.setId(Long.parseLong(s.get("id")));
                        v.setYhteystietoOid(s.get("yhteystietoOid"));
                    }
                    return v;
                } else if (s.containsKey("numero")) {
                    Puhelinnumero v = convertPuhelinnumero(s.get("numero"), s.get("tyyppi"));
                    if (s.containsKey("yhteystietoOid")) {
                        v.setId(Long.parseLong(s.get("id")));
                        v.setYhteystietoOid(s.get("yhteystietoOid"));
                    }
                    return v;
                } else if (s.containsKey("osoite")) {
                    Osoite v = convertMapToOsoite(s, null);
                    if (s.containsKey("yhteystietoOid")) {
                        Long id = Long.parseLong(s.remove("id"));
                        v.setId(id);
                    }
                    return v;
                }
            }
            catch (NumberFormatException nfe) {
                LOG.error("failed parsing number", nfe);
            }
        }
        return null;
    }
}
