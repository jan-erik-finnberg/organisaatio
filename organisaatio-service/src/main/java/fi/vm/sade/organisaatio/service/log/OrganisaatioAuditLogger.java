package fi.vm.sade.organisaatio.service.log;

import fi.vm.sade.log.model.Tapahtuma;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import fi.vm.sade.log.client.LoggerHelper;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import fi.vm.sade.log.client.LoggerMock;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioMetaDataRDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Jouni Peltonen
 */

public class OrganisaatioAuditLogger {

    protected static final org.slf4j.Logger LOG = LoggerFactory.getLogger(OrganisaatioAuditLogger.class);
    
    protected static final LoggerMock logger = new LoggerMock();
    
    public static final String SYSTEM = "organisaatio-service";
    public static final String TARGET_TYPE = "Organisaatio";
    public static final int OPERATION_TYPE_INSERT = 1;
    public static final int OPERATION_TYPE_UPDATE = 2;
    public static final int OPERATION_TYPE_DELETE = 3;
    
    private transient Map<String, String> _onLoadValues;
       
    public void logSave(OrganisaatioRDTO model, boolean updating, boolean skipParentDateValidation){        
        
        try {
            if(updating){
                logAuditTapahtuma(createUpdateTapahtuma(model.getOid(), TARGET_TYPE, "saveUpdate", mapOrganisaatioRDTO(model)));
            } else {
                Map<String, Serializable> map = new HashMap<String, Serializable>();
                map.put("organisaatio", model);
                logAuditTapahtuma(createCreateTapahtuma(model.getOid(), TARGET_TYPE, "saveInsert", map));
            }
        } catch (Exception e) {
            LOG.error(e.toString());
            LOG.error(e.getMessage());
        }
    }
    
    private void logAuditTapahtuma(Tapahtuma tapahtuma) {
        LoggerHelper.init(logger);
        if(tapahtuma == null){
            LOG.error("tapahtuma null");
        }
        logger.log(tapahtuma);
        
    }
    
    private Tapahtuma createUpdateTapahtuma(String target, String targetType, String type, Map<String, String> values) {        
        Tapahtuma t = Tapahtuma.createUPDATEMaps(SYSTEM, getAuthor(), targetType, target, _onLoadValues, values);
        // Update current state (for possible future loggings)
        _onLoadValues = values;

        return t;
    }
    
    private Tapahtuma createCreateTapahtuma(String target, String targetType, String type, Map<String, Serializable> values) {
        Tapahtuma t = Tapahtuma.createCREATE(SYSTEM,getAuthor(), targetType, target);
        t.setValues(values);
        return t;
    }
    
    private Map<String, String> mapOrganisaatioRDTO(OrganisaatioRDTO model) {
        
        Map<String, String> map = new HashMap<String, String>();
        map.put("oid", model.getOid());
        map.put("version", Integer.toString(model.getVersion()));
        
        Date alkuPvm = model.getAlkuPvm();
        if(alkuPvm != null) {
            map.put("alkuPvm", alkuPvm.toString());
        } else {
            map.put("alkuPvm", "-");
        }
        
        Date lakkautusPvm = model.getLakkautusPvm();
        if(lakkautusPvm != null) {
            map.put("lakkautusPvm", lakkautusPvm.toString());
        } else {
            map.put("lakkautusPvm", "-");
        }
        
        Date ytjPaivitysPvm = model.getYTJPaivitysPvm();
        if(ytjPaivitysPvm != null) {
            map.put("ytjPaivitysPvm", ytjPaivitysPvm.toString());
        } else {
            map.put("ytjPaivitysPvm", "-");
        }
        
        int kieliSeq = 1;
        for(String str : model.getKieletUris()){
            map.put("kieli" + kieliSeq, str);
            ++kieliSeq;
        }
        
        map.put("maaUri", model.getMaaUri());
        map.put("domainNimi", model.getDomainNimi());
        map.put("kotipaikkaUri", model.getKotipaikkaUri());
        
        int nimiSeq = 1;
        for(Entry<String,String> entry: model.getNimi().entrySet()){
            map.put("nimi" + nimiSeq, entry.getKey() + " " + entry.getValue());
            ++nimiSeq;
        }
        
        map.put("oppilaitosKoodi", model.getOppilaitosKoodi());
        map.put("oppilaitosTyyppiUri", model.getOppilaitosTyyppiUri());
        map.put("ytunnus", model.getYTunnus());
        
        int tyyppiSeq = 1;
        for(String str : model.getTyypit()){
            map.put("tyyppi" + tyyppiSeq, str);
            ++tyyppiSeq;
        }
        
        map.put("toimipisteenKoodi", model.getToimipistekoodi());
        map.put("yritysMuoto", model.getYritysmuoto());
        
        int vuosiluokkaSeq = 1;
        for(String str : model.getVuosiluokat()){
            map.put("vuosiluokka" + vuosiluokkaSeq, str);
            ++vuosiluokkaSeq;
        }
        
        int kayntiosoiteSeq = 1;
        for(Entry<String,String> entry: model.getKayntiosoite().entrySet()){
            map.put("kayntiosoite" + kayntiosoiteSeq, entry.getKey() + " " + entry.getValue());
            ++kayntiosoiteSeq;
        }
        
        int postiosoiteSeq = 1;
        for(Entry<String,String> entry: model.getPostiosoite().entrySet()){
            map.put("postiosoite" + postiosoiteSeq, entry.getKey() + " " + entry.getValue());
            ++postiosoiteSeq;
        }
        
        map.put("kuvaus", model.getKuvaus());
        
        int kuvaus2Seq = 1;
        for(Entry<String,String> entry: model.getKuvaus2().entrySet()){
            map.put("kuvaus2_" + kuvaus2Seq, entry.getKey() + " " + entry.getValue());
            ++kuvaus2Seq;
        }
        
        map.put("parentOid", model.getParentOid());
        map.put("parentPath", model.getParentOidPath());
        
        // Metadata
        
        OrganisaatioMetaDataRDTO metadata = model.getMetadata();
        
        if(metadata == null){
            map.put("metadata", null);            
        } else {        
            int outerDataSeq = 1;
            for(Entry<String,Map<String, String>> entry: metadata.getData().entrySet()){
                int innerDataSeq = 1;
                for(Entry<String,String> innerEntry: entry.getValue().entrySet()){
                    map.put("metadata data" + outerDataSeq + " " + entry.getKey() + innerDataSeq, innerEntry.getKey() + " " + innerEntry.getValue());
                    ++innerDataSeq;
                }
                ++outerDataSeq;
            }

            map.put("metadata hakutoimistoEctsEmail", metadata.getHakutoimistoEctsEmail());
            map.put("metadata hakutoimistoEctsNimi", metadata.getHakutoimistoEctsNimi());
            map.put("metadata hakutoimistoEctsPuhelin", metadata.getHakutoimistoEctsPuhelin());
            map.put("metadata hakutoimistoEctsTehtavaNimike", metadata.getHakutoimistoEctsTehtavanimike());

            int hakutoimistonNimiSeq = 1;
            for(Entry<String,String> entry: metadata.getHakutoimistonNimi().entrySet()){
                map.put("metadata hakutoimistonnimi" + hakutoimistonNimiSeq, entry.getKey() + " " + entry.getValue());
                ++hakutoimistonNimiSeq;
            }

            map.put("metadata koodi", metadata.getKoodi());
            map.put("metadata kuvaencoded", metadata.getKuvaEncoded());

            Date metaluontiPvm = metadata.getLuontiPvm();
            if(metaluontiPvm != null) {
                map.put("metadata luontiPvm", metaluontiPvm.toString());
            } else {
                map.put("metadata luontiPvm", "-");
            }

            Date metamuokkausPvm = metadata.getMuokkausPvm();
            if(metamuokkausPvm != null) {
                map.put("metadata muokkausPvm", metamuokkausPvm.toString());
            } else {
                map.put("metadata muokkausPvm", "-");
            }

            int metadataNimiSeq = 1;
            for(Entry<String,String> entry: metadata.getNimi().entrySet()){
                map.put("metadata nimi" + metadataNimiSeq, entry.getKey() + " " + entry.getValue());
                ++metadataNimiSeq;
            }

            int outerMetaYhteystiedot = 1;
            for(Map<String,String> metamap: metadata.getYhteystiedot()){
                int innerMetaYhteystiedot = 1;
                for(Entry<String,String> entry: metamap.entrySet()){
                    map.put("metadata yhteystiedot" + outerMetaYhteystiedot + "_" + innerMetaYhteystiedot, entry.getKey() + " " + entry.getValue());
                    ++innerMetaYhteystiedot;
                }
                ++outerMetaYhteystiedot;
            }
        }
        
        map.put("emailOsoite", model.getEmailOsoite());
        map.put("faksiNumero", model.getFaksinumero());
        map.put("puhelinNumero", model.getPuhelinnumero());
        map.put("wwwOsoite", model.getWwwOsoite());
        map.put("yhteishaunKoulukoodi", model.getYhteishaunKoulukoodi());
        
        int outerYhteystiedotseq = 1;
        for(Map<String,String> yhtMap: model.getYhteystietoArvos()){
           int innerYhteystiedotseq = 1;
           for(Entry<String,String> entry: yhtMap.entrySet()){
               map.put("YhteystiedoArvo" + outerYhteystiedotseq + "_" + innerYhteystiedotseq, entry.getKey() + " " + entry.getValue());
               ++innerYhteystiedotseq;
           }
           ++outerYhteystiedotseq;
        }
        
        map.put("virastoTunnus", model.getVirastoTunnus());
        map.put("opetuspisteenJarjNro", model.getOpetuspisteenJarjNro());
        
        int outerYhteystiedoseq = 1;
        for(Map<String,String> yhtMap: model.getYhteystiedot()){
           int innerYhteystiedoseq = 1;
           for(Entry<String,String> entry: yhtMap.entrySet()){
               map.put("Yhteystieto" + outerYhteystiedoseq + "_" + innerYhteystiedoseq, entry.getKey() + " " + entry.getValue());
               ++innerYhteystiedoseq;
           }
           ++outerYhteystiedoseq;
        }
        return map;
        
    }
    
    private String getAuthor() {
        String author = null;
        if(SecurityContextHolder.getContext() != null
           && SecurityContextHolder.getContext().getAuthentication() != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if(StringUtils.isNotBlank(authentication.getName())) {
                author = authentication.getName();
            } else if (authentication.getPrincipal() != null 
                       && StringUtils.isNotBlank(authentication.getPrincipal().toString())) {
                author = authentication.getPrincipal().toString();
            }
        }
        return author;
    }
}
