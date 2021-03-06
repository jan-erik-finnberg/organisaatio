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
package fi.vm.sade.organisaatio.service.converter;

import fi.vm.sade.generic.service.conversion.AbstractFromDomainConverter;
import fi.vm.sade.organisaatio.model.Email;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.Osoite;
import fi.vm.sade.organisaatio.model.Puhelinnumero;
import fi.vm.sade.organisaatio.model.Www;
import fi.vm.sade.organisaatio.model.Yhteystieto;
import fi.vm.sade.organisaatio.model.YhteystietoArvo;
import fi.vm.sade.organisaatio.model.YhteystietoElementti;
import fi.vm.sade.organisaatio.model.YhteystietojenTyyppi;
import fi.vm.sade.organisaatio.model.lop.BinaryData;
import fi.vm.sade.organisaatio.model.lop.NamedMonikielinenTeksti;
import fi.vm.sade.organisaatio.model.lop.OrganisaatioMetaData;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioMetaDataRDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.solr.common.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mlyly
 */
public class OrganisaatioToOrganisaatioRDTOConverter extends AbstractFromDomainConverter<Organisaatio, OrganisaatioRDTO> {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioToOrganisaatioRDTOConverter.class);

    @Override
    public OrganisaatioRDTO convert(Organisaatio s) {
        OrganisaatioRDTO t = new OrganisaatioRDTO();

        t.setOid(s.getOid());
        t.setVersion(s.getVersion() != null ? s.getVersion().intValue() : 0);

        t.setAlkuPvm(s.getAlkuPvm());
        // t.setChildCount(s.getChildCount());
        t.setDomainNimi(s.getDomainNimi());

        t.setKayntiosoite(convertOsoiteToMap(s.getKayntiosoite()));

        t.setKieletUris(convertListToList(s.getKielet()));
        t.setKotipaikkaUri(s.getKotipaikka());
        t.setKuvaus2(convertMKTToMap(s.getKuvaus2()));
        t.setLakkautusPvm(s.getLakkautusPvm());
        t.setMaaUri(s.getMaa());
        t.setMetadata(convertMetadata(s.getMetadata()));
        t.setNimi(convertMKTToMap(s.getNimi()));
        // t.set(s.getNimiLyhenne());
        // t.set(s.getOpetuspisteenJarjNro());
        t.setOppilaitosKoodi(s.getOppilaitosKoodi());
        t.setOppilaitosTyyppiUri(s.getOppilaitosTyyppi());
        // t.set(s.getOrganisaatiotyypitStr());
        t.setParentOid(s.getParent() != null ? s.getParent().getOid() : null);
        // t.set(s.getParentIdPath());
        // t.setParentMetadata(s.getParentMetadata());
        t.setParentOidPath(s.getParentOidPath());
        // t.set(s.getParentSuhteet());


        t.setPostiosoite(convertOsoiteToMap(s.getPostiosoite()));

        // t.set(s.getPuhelin());
        t.setToimipistekoodi(s.getToimipisteKoodi());
        t.setTyypit(convertListToList(s.getTyypit()));
        // t.set(s.getTyypitAsString());
        t.setVuosiluokat(convertListToList(s.getVuosiluokat()));
        t.setRyhmatyypit(convertListToList(s.getRyhmatyypit()));
        t.setKayttoryhmat(convertListToList(s.getKayttoryhmat()));
        t.setYhteishaunKoulukoodi(s.getYhteishaunKoulukoodi());
        // t.set(s.getYhteystiedot());
        // t.set(s.getYhteystietoArvos());
        t.setYritysmuoto(s.getYritysmuoto());
        t.setYTJPaivitysPvm(s.getYtjPaivitysPvm());
        t.setYTunnus(s.getYtunnus());
        t.setVirastoTunnus(s.getVirastoTunnus());

        //t.setPuhelinnumero(convertYhteystietoToPuhelinnumero(s.getYhteystiedot()));
        //t.setFaksinumero(convertYhteystietoToFaksinumero(s.getYhteystiedot()));
        //t.setEmailOsoite(convertYhteystietoToEmailOsoite(s.getYhteystiedot()));
        //t.setWwwOsoite(convertYhteystietoToWwwOsoite(s.getYhteystiedot()));

        // Get dynamic Yhteysieto / Yhteystietotyppie / Elementti data
        List<Map<String, String>> yhteystietoArvos = new ArrayList<Map<String, String>>();
        t.setYhteystietoArvos(yhteystietoArvos);

        for (Yhteystieto y : s.getYhteystiedot()) {
            t.addYhteystieto(convertYhteystietoGeneric(y));
        }

        for (YhteystietoArvo yhteystietoArvo : s.getYhteystietoArvos()) {
            YhteystietoElementti yElementti = null;
            YhteystietojenTyyppi yTyyppi = null;

            Map<String, String> val = new HashMap<String, String>();
            yhteystietoArvos.add(val);

            val.put("YhteystietoArvo.arvoText", yhteystietoArvo.getArvoText());
            val.put("YhteystietoArvo.kieli", yhteystietoArvo.getKieli());

            yElementti = yhteystietoArvo.getKentta();

            if (yElementti != null) {
                val.put("YhteystietoElementti.nimi", yElementti.getNimi());
                val.put("YhteystietoElementti.nimiSv", yElementti.getNimiSv());
                val.put("YhteystietoElementti.oid", yElementti.getOid());
                val.put("YhteystietoElementti.tyyppi", yElementti.getTyyppi());
                val.put("YhteystietoElementti.kaytossa", "" + yElementti.isKaytossa());
                val.put("YhteystietoElementti.pakollinen", "" + yElementti.isPakollinen());

                yTyyppi = yElementti.getYhteystietojenTyyppi();

                if (yTyyppi != null) {
                    Map<String, String> nimiMap = convertMKTToMap(yTyyppi.getNimi());
                    for (String kieli : nimiMap.keySet()) {
                        val.put("YhteystietojenTyyppi.nimi." + kieli, nimiMap.get(kieli));
                    }

                    val.put("YhteystietojenTyyppi.oid", yTyyppi.getOid());

                    // yTyyppi.getSovellettavatOppilaitostyyppis();
                    // yTyyppi.getSovellettavatOrganisaatioTyyppis();
                }
            }
        }

        LOG.debug("convert: {} --> {}", s, t);

        return t;
    }

    private Map<String, String> convertMKTToMap(MonikielinenTeksti nimi) {
        Map<String, String> result = new HashMap<String, String>();

        if (nimi != null) {
            result.putAll(nimi.getValues());
        }

        return result;
    }

    private List<String> convertListToList(List<String> s) {
        List<String> result = new ArrayList<String>();
        for (String v : s) {
            result.add(v);
        }
        return result;
    }

    private String formatDate(Date dt) {
        if (dt != null) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            return df.format(dt);
        } else {
            return null;
        }
    }

    private Map<String, String> convertOsoiteToMap(Osoite s) {
        Map<String, String> t = new HashMap<String, String>();

        if (s == null) {
            return null;
        }

        addToMapIfNotNULL(t, "coordinateType", s.getCoordinateType());
        addToMapIfNotNULL(t, "extraRivi", s.getExtraRivi());
        addToMapIfNotNULL(t, "maaUri", s.getMaa());
        addToMapIfNotNULL(t, "osavaltio", s.getOsavaltio());
        addToMapIfNotNULL(t, "osoite", s.getOsoite().replace("<br />", "\n"));
        addToMapIfNotNULL(t, "osoiteTyyppi", s.getOsoiteTyyppi());
        addToMapIfNotNULL(t, "postinumeroUri", s.getPostinumero());
        addToMapIfNotNULL(t, "postitoimipaikka", s.getPostitoimipaikka());
        addToMapIfNotNULL(t, "yhteystietoOid", s.getYhteystietoOid());
        addToMapIfNotNULL(t, "lap", s.getLat());
        addToMapIfNotNULL(t, "lng", s.getLng());

        addToMapIfNotNULL(t, "ytjPaivitysPvm", formatDate(s.getYtjPaivitysPvm()));

        return t;
    }

    private void addToMapIfNotNULL(Map map, String key, Object value) {
//        if (value != null) {
        map.put(key, value);
//        }
    }

    private OrganisaatioMetaDataRDTO convertMetadata(OrganisaatioMetaData s) {
        if (s == null) {
            return null;
        }

        OrganisaatioMetaDataRDTO t = new OrganisaatioMetaDataRDTO();

        t.setHakutoimistoEctsEmail(s.getHakutoimistoEctsEmail());
        t.setHakutoimistoEctsNimi(s.getHakutoimistoEctsNimi());
        t.setHakutoimistoEctsPuhelin(s.getHakutoimistoEctsPuhelin());
        t.setHakutoimistoEctsTehtavanimike(s.getHakutoimistoEctsTehtavanimike());
        t.setHakutoimistonNimi(convertMKTToMap(s.getHakutoimistoNimi()));
        t.setKoodi(s.getKoodi());
        t.setKuvaEncoded(encodeToUUENCODED(s.getKuva()));
        t.setLuontiPvm(s.getLuontiPvm());
        t.setMuokkausPvm(s.getMuokkausPvm());
        t.setNimi(convertMKTToMap(s.getNimi()));

        // TODO t.set(s.getYhteystiedot());
        for (Yhteystieto yhteystieto : s.getYhteystiedot()) {
            t.getYhteystiedot().add(convertYhteystietoGeneric(yhteystieto));
        }


        for (NamedMonikielinenTeksti namedMonikielinenTeksti : s.getValues()) {
            // TODO how about namedMonikielinenTeksti.getNimi ???
            t.addByKey(namedMonikielinenTeksti.getKey(), convertMKTToMap(namedMonikielinenTeksti.getValue()));
        }

        return t;
    }


    /**
     * Converts any Yhteystieto to a MAP.
     *
     * @param s
     * @return
     */
    private Map<String, String> convertYhteystietoGeneric(Yhteystieto s) {
        Map<String, String> result = new HashMap<String, String>();

        if (s != null) {
            result.put("id", "" + s.getId());
            result.put("yhteystietoOid", s.getYhteystietoOid());
            result.put("kieli", s.getKieli());

            if (s instanceof Email) {
                Email v = (Email) s;
                result.put("email", v.getEmail());
            } else if (s instanceof Www) {
                Www v = (Www) s;
                result.put("www", v.getWwwOsoite());
            } else if (s instanceof Puhelinnumero) {
                Puhelinnumero v = (Puhelinnumero) s;
                result.put("numero", v.getPuhelinnumero());
                result.put("tyyppi", v.getTyyppi());
            } else if (s instanceof Osoite) {
                Osoite v = (Osoite) s;
                result.putAll(convertOsoiteToMap(v));
            } else {
                result.put("ERROR", "UNKNOWN TYPE OF: " + s.getClass().getSimpleName());
            }
        }

        return result;
    }

    private String encodeToUUENCODED(BinaryData kuva) {
        if (kuva == null || kuva.getData() == null) {
            return null;
        }

        return Base64.byteArrayToBase64(kuva.getData(), 0, kuva.getData().length);
    }

    public String convertYhteystietoToPuhelinnumero(List<Yhteystieto> yhteystietos) {

        for (Yhteystieto yhteystieto : yhteystietos) {
            if (yhteystieto instanceof Puhelinnumero) {
                Puhelinnumero p = (Puhelinnumero) yhteystieto;
                if (Puhelinnumero.TYYPPI_PUHELIN.equals(p.getTyyppi())) {
                    return p.getPuhelinnumero();
                }
            }
        }
        return null;
    }

    public String convertYhteystietoToFaksinumero(List<Yhteystieto> yhteystietos) {

        for (Yhteystieto yhteystieto : yhteystietos) {
            if (yhteystieto instanceof Puhelinnumero) {
                Puhelinnumero p = (Puhelinnumero) yhteystieto;
                if (Puhelinnumero.TYYPPI_FAKSI.equals(p.getTyyppi())) {
                    return p.getPuhelinnumero();
                }
            }
        }
        return null;
    }

    public String convertYhteystietoToWwwOsoite(List<Yhteystieto> yhteystietos) {

        for (Yhteystieto yhteystieto : yhteystietos) {
            if (yhteystieto instanceof Www) {
                return ((Www) yhteystieto).getWwwOsoite();
            }
        }
        return null;
    }

    public String convertYhteystietoToEmailOsoite(List<Yhteystieto> yhteystietos) {

        for (Yhteystieto yhteystieto : yhteystietos) {
            if (yhteystieto instanceof Email) {
                return ((Email) yhteystieto).getEmail();
            }
        }
        return null;
    }

    private boolean isEmpty(String s) {
        return (s == null || s.trim().isEmpty());
    }

}
