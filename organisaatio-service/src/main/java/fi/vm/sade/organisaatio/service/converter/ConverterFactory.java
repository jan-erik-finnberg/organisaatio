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

package fi.vm.sade.organisaatio.service.converter;

//import fi.vm.sade.generic.common.BaseDTO;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.vm.sade.organisaatio.api.model.types.EmailDTO;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.api.model.types.OsoiteTyyppi;
import fi.vm.sade.organisaatio.api.model.types.PuhelinNumeroTyyppi;
import fi.vm.sade.organisaatio.api.model.types.PuhelinnumeroDTO;
import fi.vm.sade.organisaatio.api.model.types.WwwDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoArvoDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.YhteystietojenTyyppiDTO;
import fi.vm.sade.organisaatio.dao.impl.OrganisaatioDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietoArvoDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietoDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietoElementtiDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietojenTyyppiDAOImpl;
import fi.vm.sade.organisaatio.model.Email;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioBaseEntity;
import fi.vm.sade.organisaatio.model.Osoite;
import fi.vm.sade.organisaatio.model.Puhelinnumero;
import fi.vm.sade.organisaatio.model.Www;
import fi.vm.sade.organisaatio.model.Yhteystieto;
import fi.vm.sade.organisaatio.model.YhteystietoArvo;
import fi.vm.sade.organisaatio.model.YhteystietoElementti;
import fi.vm.sade.organisaatio.model.YhteystietojenTyyppi;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import javax.ws.rs.core.Response;

/**
 * @author Antti Salonen
 */
public class ConverterFactory {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private DozerBeanMapper mapper;
    @PersistenceContext
    private EntityManager entityManager;
    private List<Converter> converters = new ArrayList<Converter>();

    @Autowired
    private OrganisaatioDAOImpl organisaatioDAO;

    @Autowired
    private YhteystietoArvoDAOImpl yhteystietoArvoDAO;

    @Autowired
    private YhteystietoDAOImpl yhteystietoDAO;

    @Autowired
    private YhteystietoElementtiDAOImpl yhteystietoElementtiDAO;

    @Autowired
    private YhteystietojenTyyppiDAOImpl yhteystietojenTyyppiDAO;


    @PostConstruct
    public void initConverters() {
        registerConverter(new OrganisaatioConverter(this, entityManager));
        registerConverter(new OrganisaatioFatConverter(this, entityManager));
        //registerConverter(new OrganisaatiotyypinYhteystiedotConverter(this, entityManager));
        registerConverter(new YhteystietoArvoConverter(this, entityManager));
        registerConverter(new YhteystietojenTyyppiConverter(this, entityManager));
    }

    public void registerConverter(Converter converter) {
        converters.add(converter);
    }

    public <DTO> List<DTO> convertToDTO(List<? extends OrganisaatioBaseEntity> entities, Class<? extends DTO> resultClass) {
        List<DTO> dtos = new ArrayList<DTO>();
        for (OrganisaatioBaseEntity entity : entities) {

            dtos.add(convertToDTO(entity, resultClass));

        }
        return dtos;
    }

    public <DTO> DTO convertToDTO(OrganisaatioBaseEntity entity) {
        return (DTO) convertToDTO(entity, entity.getDTOClass());
    }

    public <DTO> DTO convertToDTO(OrganisaatioBaseEntity entity, Class<? extends DTO> resultClass) {
        DTO dto = null;

        // if resultClass is abstractclass, get resultclass from entity, but ensure it is resultclass' subclass
        if (Modifier.isAbstract(resultClass.getModifiers())) {
            Class temp = entity.getDTOClass();
            if (!resultClass.isAssignableFrom(temp)) {
                throw new IllegalArgumentException("cannot convert, resultClass is abstract and not not assignable from entity's dtoclass, resultClass: " +
                        resultClass + ", entity.dtoclass: " + entity.getDTOClass());
            }
            resultClass = temp;
        }

        // create object and convert basic fields with dozer
        if (entity != null) {
            dto = mapper.map(entity, resultClass);
        }

        // convert other fields with custom converter
        Converter converter = getConverterForDto(resultClass);
        if (converter != null) {
            converter.setValuesToDTO(entity, dto);
        }

        if (entity instanceof Puhelinnumero) {
            ((PuhelinnumeroDTO)dto).setTyyppi(PuhelinNumeroTyyppi.fromValue(((Puhelinnumero) entity).getTyyppi()));
        } else if (entity instanceof Osoite) {
            ((OsoiteDTO)dto).setOsoiteTyyppi(OsoiteTyyppi.fromValue(((Osoite) entity).getOsoiteTyyppi()));
        } else if (entity instanceof YhteystietoElementti) {
            ((YhteystietoElementtiDTO)dto).setTyyppi(YhteystietoElementtiTyyppi.fromValue(((YhteystietoElementti) entity).getTyyppi()));
        }

        //DEBUGSAWAY:log.debug("convertToDTO: " + entity + " -> " + dto);
        return dto;
    }



    private Converter getConverterForDto(Class dtoClass) {
        for (Converter converter : converters){
            if (converter.supportsDtoClass(dtoClass)) {
                return converter;
            }
        }
        return null;
    }

    private Converter getConverterForEntity(OrganisaatioBaseEntity entity) {
        for (Converter converter : converters){
            if (converter.supportsEntityClass(entity.getClass())) {
                return converter;
            }
        }
        return null;
    }

    private Class getJPAClass(Object dto) {
        Converter converter = getConverterForDto(dto.getClass());
        if (converter != null) {
            return converter.getJpaClass();
        }

        // TODO: omat convertterit näillekin?
        Class<? extends OrganisaatioBaseEntity> jpaClass;
        if (OsoiteDTO.class.isAssignableFrom(dto.getClass())) {
            jpaClass = Osoite.class;
        } else if (PuhelinnumeroDTO.class.isAssignableFrom(dto.getClass())) {
            jpaClass = Puhelinnumero.class;
        } else if (WwwDTO.class.isAssignableFrom(dto.getClass())) {
            jpaClass = Www.class;
        } else if (EmailDTO.class.isAssignableFrom(dto.getClass())) {
            jpaClass = Email.class;
        } //else if (OrganisaatioTyyppiDTO.class.isAssignableFrom(dto.getClass())) {
            //jpaClass = OrganisaatioTyyppi.class;}
        else if (YhteystietoElementtiDTO.class.isAssignableFrom(dto.getClass())) {
            jpaClass = YhteystietoElementti.class;
        } else {
            throw new IllegalArgumentException("no converter found for dto: " + dto);
        }

        return jpaClass;
    }

    /**
     * converts dto to jpa entity
     * @param merge Applicable only for objects that already exist in db (has id). If true, merge changes, otherhwise will reload from db instead converting.
     */
//    public <JPACLASS extends OrganisaatioBaseEntity> JPACLASS convertToJPA(Object dto, Class <? extends JPACLASS> resultClass, boolean merge) {
//        JPACLASS entity = null;
//        if (dto != null) {
//
//            Class jpaClass = getJPAClass(dto);
//            // reload if !merge and entity exists in db already
//            if (dto.getId() != null && !merge) {
//                entity = (JPACLASS) entityManager.find(jpaClass, dto.getId());
//DEBUGSAWAY://                log.debug("convertToJPA reloaded object: "+entity);
//            } else if (dto.getId() != null && merge) {
//                // hibernate merge tms jos on kannassa jo ja merge=true, muuten syntyy duplikaatti objekti
//                /*
//                entity = (JPACLASS) mapper.map(dto, jpaClass);
//                entity = entityManager.merge(entity);
//                */
//                entity = (JPACLASS) entityManager.find(jpaClass, dto.getId());
//                mapper.map(dto, entity);
//            } else {
//                // or convert fields from dto
//                entity = (JPACLASS) mapper.map(dto, jpaClass);
//            }
//            // organisaatio parent
//
//            Converter converter = getConverterForDto(dto.getClass());
//            if (converter != null) {
//                converter.setValuesToJPA(dto, entity, merge);
//            }
//
//        }
//DEBUGSAWAY://        log.debug("convertToJPA: " + dto + " -> " + entity);
//        return entity;
//    }

    /*

    TKATVA, testing more simpler merging. Just retrieve map dto to entity and set the id of the entity

     */

    public Organisaatio convertOrganisaatioToEntity(OrganisaatioDTO dto, boolean merge) {
        Organisaatio entity = null;
        try {
           entity = new Organisaatio();
           if (merge) {
               Organisaatio orgEntity = this.organisaatioDAO.findByOid(dto.getOid());
               entity.setId(orgEntity.getId());
           }

           mapper.map(dto,entity);


           entity.setTyypit(getTyypitStr(dto.getTyypit()));

           entity.setVuosiluokat(dto.getVuosiluokat());
           entity.setRyhmatyypit(dto.getRyhmatyypit());
           entity.setKayttoryhmat(dto.getKayttoryhmat());
           entity.setKielet(dto.getKielet());
           convertNimiToEntity(dto, entity);

        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }

        return entity;
    }


    private void convertNimiToEntity(OrganisaatioDTO dto, Organisaatio entity) {
        if (dto.getNimi() == null) {
            return;
        }
        MonikielinenTeksti nimiE = new MonikielinenTeksti();
        String nimihaku = "";
        for (MonikielinenTekstiTyyppi.Teksti curTeksti : dto.getNimi().getTeksti()) {
            nimiE.addString(curTeksti.getKieliKoodi(), curTeksti.getValue());
            nimihaku += "," + curTeksti.getValue();
        }
        entity.setNimihaku(nimihaku);
        entity.setNimi(nimiE);
    }

    /**
     * converts dto to jpa entity
     * @param merge Applicable only for objects that already exist in db (has id). If true, merge changes, otherhwise will reload from db instead converting.
     */
    public Organisaatio convertOrganisaatioToJPA(OrganisaatioDTO dto, boolean merge) {
        Organisaatio entity = null;
        if (dto != null) {

            Class jpaClass = Organisaatio.class;
            // reload if !merge and entity exists in db already
            if (dto.getOid() != null && this.organisaatioDAO.findByOid(dto.getOid()) != null && !merge) {
                entity = this.organisaatioDAO.findByOid(dto.getOid());//entityManager.find(jpaClass, dto.getOid());
                //DEBUGSAWAY:log.debug("convertToJPA reloaded object: "+entity);
            } else if (dto.getOid() != null && this.organisaatioDAO.findByOid(dto.getOid()) != null && merge) {
                // hibernate merge tms jos on kannassa jo ja merge=true, muuten syntyy duplikaatti objekti
                /*
                entity = (JPACLASS) mapper.map(dto, jpaClass);
                entity = entityManager.merge(entity);
                */
                entity = this.organisaatioDAO.findByOid(dto.getOid());
                mapper.map(dto, entity);
                entity.setTyypit(getTyypitStr(dto.getTyypit()));
            } else {
                // or convert fields from dto
                entity = (Organisaatio) mapper.map(dto, jpaClass);
                entity.setTyypit(getTyypitStr(dto.getTyypit()));
            }
            // organisaatio parent

            Converter converter = getConverterForDto(dto.getClass());
            if (converter != null) {
                converter.setValuesToJPA(dto, entity, merge, this.organisaatioDAO);
            }

        }
        //DEBUGSAWAY:log.debug("convertToJPA: " + dto + " -> " + entity);
        return entity;
    }

    private List<String> getTyypitStr(List<OrganisaatioTyyppi> tyypit) {
        List<String> tyypitStr = new ArrayList<String>();
        for (OrganisaatioTyyppi curT : tyypit) {
            tyypitStr.add(curT.value());
        }
        return tyypitStr;
    }

    /**
     * converts dto to jpa entity
     * @param merge Applicable only for objects that already exist in db (has id). If true, merge changes, otherhwise will reload from db instead converting.
     */
    public YhteystietoArvo convertYhteystietoArvoToJPA(YhteystietoArvoDTO dto, boolean merge) {
        YhteystietoArvo entity = null;
        if (dto != null) {

            Class jpaClass = YhteystietoArvo.class;
            // reload if !merge and entity exists in db already
            if (dto.getYhteystietoArvoOid() != null && this.yhteystietoArvoDAO.findBy("yhteystietoArvoOid", dto.getYhteystietoArvoOid()).size() > 0 && !merge) {
                entity = this.yhteystietoArvoDAO.findBy("yhteystietoArvoOid", dto.getYhteystietoArvoOid()).get(0);//(YhteystietoArvo) entityManager.find(jpaClass, dto.getYhteystietoArvoOid());
                //DEBUGSAWAY:log.debug("convertToJPA reloaded object: "+entity);
            } else if (dto.getYhteystietoArvoOid() != null && this.yhteystietoArvoDAO.findBy("yhteystietoArvoOid", dto.getYhteystietoArvoOid()).size() > 0 && merge) {
                // hibernate merge tms jos on kannassa jo ja merge=true, muuten syntyy duplikaatti objekti
                /*
                entity = (JPACLASS) mapper.map(dto, jpaClass);
                entity = entityManager.merge(entity);
                */
                entity = this.yhteystietoArvoDAO.findBy("yhteystietoArvoOid", dto.getYhteystietoArvoOid()).get(0);
                mapper.map(dto, entity);
            } else {
                // or convert fields from dto
                entity = (YhteystietoArvo) mapper.map(dto, jpaClass);
            }
            // organisaatio parent

            Converter converter = getConverterForDto(dto.getClass());
            if (converter != null) {
                converter.setValuesToJPA(dto, entity, merge, this.organisaatioDAO, this.yhteystietoElementtiDAO);
            }

        }
        //DEBUGSAWAY:log.debug("convertToJPA: " + dto + " -> " + entity);
        return entity;
    }

    /**
     * converts dto to jpa entity
     * @param merge Applicable only for objects that already exist in db (has id). If true, merge changes, otherhwise will reload from db instead converting.
     */
    public YhteystietoElementti convertYhteystietoElementtiToJPA(YhteystietoElementtiDTO dto, boolean merge) {
        YhteystietoElementti entity = null;
        if (dto != null) {

            Class jpaClass = YhteystietoElementti.class;
            // reload if !merge and entity exists in db already
            if (dto.getOid() != null && this.yhteystietoElementtiDAO.findBy("oid", dto.getOid()).size() > 0 && !merge) {
                entity = this.yhteystietoElementtiDAO.findBy("oid", dto.getOid()).get(0);//(YhteystietoElementti) entityManager.find(jpaClass, dto.getNimi());
                //DEBUGSAWAY:log.debug("convertToJPA reloaded object: "+entity);
            } else if (dto.getOid() != null && this.yhteystietoElementtiDAO.findBy("oid", dto.getOid()).size() > 0 && merge) {
                // hibernate merge tms jos on kannassa jo ja merge=true, muuten syntyy duplikaatti objekti
                /*
                entity = (JPACLASS) mapper.map(dto, jpaClass);
                entity = entityManager.merge(entity);
                */
                entity = this.yhteystietoElementtiDAO.findBy("oid", dto.getOid()).get(0);//(YhteystietoElementti) entityManager.find(jpaClass, dto.getNimi());
                mapper.map(dto, entity);
                entity.setTyyppi(dto.getTyyppi().value());

                entity.setPakollinen(dto.isPakollinen());

                entity.setKaytossa(dto.isKaytossa());
            } else {
                // or convert fields from dto

                entity = (YhteystietoElementti) mapper.map(dto, jpaClass);
                entity.setTyyppi(dto.getTyyppi().value());

                entity.setPakollinen(dto.isPakollinen());

                entity.setKaytossa(dto.isKaytossa());
            }
            // organisaatio parent

            Converter converter = getConverterForDto(dto.getClass());
            if (converter != null) {
                converter.setValuesToJPA(dto, entity, merge);
            }

        }
        //DEBUGSAWAY:log.debug("convertToJPA: " + dto + " -> " + entity);
        return entity;
    }

    /**
     * converts dto to jpa entity
     * @param merge Applicable only for objects that already exist in db (has id). If true, merge changes, otherhwise will reload from db instead converting.
     */

    public <JPACLASS extends Yhteystieto> JPACLASS convertYhteystietoToJPA(YhteystietoDTO dto, boolean merge) {
        JPACLASS entity = null;
        Class jpaClass = getJPAClass(dto);
        if (dto != null) {
            // reload if !merge and entity exists in db already
           if (dto.getYhteystietoOid() != null && yhteystietoDAO.findBy("yhteystietoOid", dto.getYhteystietoOid()).size() > 0 && !merge) {
                entity = (JPACLASS)(yhteystietoDAO.findBy("yhteystietoOid", dto.getYhteystietoOid()).get(0));
                //DEBUGSAWAY://log.debug("convertToJPA reloaded object: "+entity);
            } else if (dto.getYhteystietoOid() != null && yhteystietoDAO.findBy("yhteystietoOid", dto.getYhteystietoOid()).size() > 0 && merge) {
                // hibernate merge tms jos on kannassa jo ja merge=true, muuten syntyy duplikaatti objekti
                /*
                entity = (JPACLASS) mapper.map(dto, jpaClass);
                entity = entityManager.merge(entity);
                */
                entity = (JPACLASS)(yhteystietoDAO.findBy("yhteystietoOid", dto.getYhteystietoOid()).get(0));
                mapper.map(dto, entity);
            } else {
                // or convert fields from dto
                entity = (JPACLASS) mapper.map(dto, jpaClass);
            }
            // organisaatio parent

            if (entity instanceof Puhelinnumero) {
                ((Puhelinnumero) entity).setTyyppi(((PuhelinnumeroDTO)dto).getTyyppi().value());
            } else if (entity instanceof Osoite) {
                ((Osoite) entity).setOsoiteTyyppi(((OsoiteDTO)dto).getOsoiteTyyppi().value());
            }


        }
        //DEBUGSAWAY:log.debug("convertToJPA: " + dto + " -> " + entity);
        return entity;
    }

    public YhteystietojenTyyppi convertYhteystietojenTyyppiToJPA(YhteystietojenTyyppiDTO dto, boolean merge) {
        YhteystietojenTyyppi entity = null;
        Class jpaClass = YhteystietojenTyyppi.class;
         if (dto != null) {
             // reload if !merge and entity exists in db already
            if (dto.getOid() != null && this.yhteystietojenTyyppiDAO.findBy("oid", dto.getOid()).size() > 0 && !merge) {
                 entity = this.yhteystietojenTyyppiDAO.findBy("oid", dto.getOid()).get(0);//(YhteystietojenTyyppi) entityManager.find(jpaClass, dto.getOid());
                 //DEBUGSAWAY://log.debug("convertToJPA reloaded object: "+entity);
             } else if (dto.getOid() != null && this.yhteystietojenTyyppiDAO.findBy("oid", dto.getOid()).size() > 0 && merge) {
                 // hibernate merge tms jos on kannassa jo ja merge=true, muuten syntyy duplikaatti objekti
                 /*
                 entity = (JPACLASS) mapper.map(dto, jpaClass);
                 entity = entityManager.merge(entity);
                 */
                 entity = this.yhteystietojenTyyppiDAO.findBy("oid", dto.getOid()).get(0);
                 if (entity.getVersion() != dto.getVersion()) {
                     throw new OrganisaatioResourceException(Response.Status.CONFLICT, "Data version changed.", "yhteystietojentyyppi.exception.modified");
                 }
                 mapper.map(dto, entity);
             } else {
                 // or convert fields from dto

                 entity = (YhteystietojenTyyppi) mapper.map(dto, jpaClass);

             }
             // organisaatio parent

             Converter converter = getConverterForDto(dto.getClass());
             if (converter != null) {

                 converter.setValuesToJPA(dto, entity, merge);
             }
         }
         //DEBUGSAWAY:log.debug("convertToJPA: " + dto + " -> " + entity);
         return entity;
     }

    //



    public List<String> convertOrganisaatiotyypinYhteystiedotToJPA(List<fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi> dtos, boolean merge ) {
        List<String> orgTypes = new ArrayList<String>();
        //YhteystietoConverter ytConv = new YhteystietoConverter(this, entityManager);//OrganisaatiotyypinYhteystiedot
        if (dtos != null) {
            for (fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi dto : dtos) {
                if (!orgTypes.contains(dto.value())) {
                    orgTypes.add(dto.value());
                }
            }
        }
        return orgTypes;
    }

    public <JPACLASS extends OrganisaatioBaseEntity> List<JPACLASS> convertYhteystiedotToJPA(List<? extends YhteystietoDTO> dtos, Class<? extends JPACLASS> resultClass, boolean merge ) {
        List jpas = new ArrayList();

        if (dtos != null) {
            for (YhteystietoDTO dto : dtos) {
                jpas.add(convertYhteystietoToJPA(dto, merge));
            }
        }
        return jpas;
    }

    public <JPACLASS extends OrganisaatioBaseEntity> List<JPACLASS> convertYhteystietoArvosToJPA(List<YhteystietoArvoDTO> dtos, Class<? extends JPACLASS> resultClass, boolean merge ) {
        List jpas = new ArrayList();
        //YhteystietoConverter ytConv = new YhteystietoConverter(this, entityManager);
        if (dtos != null) {
            for (YhteystietoArvoDTO dto : dtos) {
                if (dto.getArvo() != null
                        && (((dto.getArvo() instanceof String)
                                && ((String)dto.getArvo()).length() > 0
                            || isValidYhteystieto(dto.getArvo())))) {
                    YhteystietoArvo jpa = convertYhteystietoArvoToJPA(dto, merge);
                    if (jpa.getYhteystietoArvoOid() == null) {
                    	jpa.setYhteystietoArvoOid(dto.getYhteystietoArvoOid());
                    }
                    jpas.add(jpa);
                }
            }
        }
        return jpas;
    }

    private boolean isValidYhteystieto(Object yhteystieto) {
        boolean isValid= false;
        if (yhteystieto instanceof OsoiteDTO) {
            OsoiteDTO yhteystietoO = (OsoiteDTO)yhteystieto;
            return yhteystietoO.getPostinumero() != null
                    && yhteystietoO.getOsoite() != null
                    && yhteystietoO.getOsoite().length() > 0
                    && yhteystietoO.getPostitoimipaikka() != null;
        }
        if (yhteystieto instanceof PuhelinnumeroDTO) {
            PuhelinnumeroDTO yhteystietoP = (PuhelinnumeroDTO)yhteystieto;
            return yhteystietoP.getPuhelinnumero() != null && yhteystietoP.getPuhelinnumero().length() > 0;
        }
        if (yhteystieto instanceof EmailDTO) {
            EmailDTO yhteystietoE = (EmailDTO)yhteystieto;
            return yhteystietoE.getEmail() != null && yhteystietoE.getEmail().length() > 0;
        }
        if (yhteystieto instanceof WwwDTO) {
            WwwDTO yhteystietoW = (WwwDTO)yhteystieto;
            return yhteystietoW.getWwwOsoite() != null && yhteystietoW.getWwwOsoite().length() > 0;
        }
        return isValid;
    }

    public <JPACLASS extends OrganisaatioBaseEntity> List<JPACLASS> convertYhteystietoElementtisToJPA(List<YhteystietoElementtiDTO> dtos, Class<? extends JPACLASS> resultClass, boolean merge ) {
        List jpas = new ArrayList();
        //YhteystietoConverter ytConv = new YhteystietoConverter(this, entityManager);
        if (dtos != null) {
            for (YhteystietoElementtiDTO dto : dtos) {
                YhteystietoElementti jpa = convertYhteystietoElementtiToJPA(dto, merge);
                jpas.add(jpa);
            }
        }
        return jpas;
    }

    /*
    public <JPACLASS extends OrganisaatioBaseEntity> List<JPACLASS> convertToJPA(List<? extends  BaseDTO> dtos, Class<? extends JPACLASS> resultClass, boolean merge) {
        List jpas = new ArrayList();
        if (dtos != null) {
            for (BaseDTO dto : dtos) {
                JPACLASS jpa = convertToJPA(dto, resultClass, merge);
                jpas.add(jpa);
            }
        }
        return jpas;
    }*/

    public OrganisaatioDTO convertToFatDTO(Organisaatio entity) {
        OrganisaatioDTO dto = convertToDTO(entity, OrganisaatioDTO.class);
        LOG.info("convertToFatDTO: " + entity + " -> " + dto);
        return dto;
    }

}
