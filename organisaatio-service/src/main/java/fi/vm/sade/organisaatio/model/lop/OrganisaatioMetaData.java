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
package fi.vm.sade.organisaatio.model.lop;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import fi.vm.sade.generic.model.BaseEntity;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.Yhteystieto;
import org.hibernate.annotations.Table;

/**
 * LOP related metadata. Always accosiated with a Organisation.
 * 
 * @author mlyly
 */
@Entity
@Table(appliesTo = "OrganisaatioMetaData", comment = "Sisältää organisaation metatiedot, kuten nimi ja kuva.")
public class OrganisaatioMetaData extends BaseEntity {

    /**
     * Owner organisation
     */
    @OneToOne(mappedBy = "metadata")
    private Organisaatio organisation;

    /**
     * Translatable name for this LOP.
     */
    @ManyToOne(cascade = CascadeType.ALL)
    private MonikielinenTeksti nimi;

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    private BinaryData kuva;

    // Hakutoimisto
    @ManyToOne(cascade = CascadeType.ALL)
    private MonikielinenTeksti hakutoimistoNimi;

    @OneToMany(orphanRemoval=true, cascade = CascadeType.ALL)
    @OrderBy("id")
    private List<Yhteystieto> yhteystiedot = new ArrayList<Yhteystieto>();
    
    public List<Yhteystieto> getYhteystiedot() {
        return yhteystiedot;
    }

    public MonikielinenTeksti getHakutoimistoNimi() {
        return hakutoimistoNimi;
    }

    public void setHakutoimistoNimi(MonikielinenTeksti hakutoimistoNimi) {
        this.hakutoimistoNimi = hakutoimistoNimi;
    }

    public String getHakutoimistoEctsNimi() {
        return hakutoimistoEctsNimi;
    }

    public void setHakutoimistoEctsNimi(String hakutoimistoEctsNimi) {
        this.hakutoimistoEctsNimi = hakutoimistoEctsNimi;
    }

    public String getHakutoimistoEctsTehtavanimike() {
        return hakutoimistoEctsTehtavanimike;
    }

    public void setHakutoimistoEctsTehtavanimike(String hakutoimistoEctsTehtavanimike) {
        this.hakutoimistoEctsTehtavanimike = hakutoimistoEctsTehtavanimike;
    }

    public String getHakutoimistoEctsEmail() {
        return hakutoimistoEctsEmail;
    }

    public void setHakutoimistoEctsEmail(String hakutoimistoEctsSahkoposti) {
        this.hakutoimistoEctsEmail = hakutoimistoEctsSahkoposti;
    }

    public String getHakutoimistoEctsPuhelin() {
        return hakutoimistoEctsPuhelin;
    }

    public void setHakutoimistoEctsPuhelin(String hakutoimistoEctsPuhelin) {
        this.hakutoimistoEctsPuhelin = hakutoimistoEctsPuhelin;
    }

    // ects koordinaattori
    private String hakutoimistoEctsNimi;
    private String hakutoimistoEctsTehtavanimike;
    private String hakutoimistoEctsEmail;
    private String hakutoimistoEctsPuhelin;

    public BinaryData getKuva() {
        return kuva;
    }

    public void setKuva(BinaryData kuva) {
        this.kuva = kuva;
    }

    /**
     * Code(?) for this LOP.
     */
    private String koodi;

    /**
     * Translated values for this entry by key.
     */
    @OneToMany(cascade = CascadeType.ALL)
    private Set<NamedMonikielinenTeksti> values = new HashSet<NamedMonikielinenTeksti>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false, nullable = false)
    private Date luontiPvm = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date muokkausPvm = new Date();

    //
    // Getters setters with "logic"
    //

    /**
     * Creates a keyed <code>dataKey</code> MonikielinenTeksti (translations)
     * entry.
     * 
     * @param dataKey
     *            Used key, for example "genericInformationAboutCostOfLiving".
     * @param languageCode
     *            Koodisto language code (uri: EN 123) or just some language key
     * @param value
     *            actual textual value, null value clears data
     */
    public void setNamedValue(String dataKey, String languageCode, String value) {
        // Find correct entry
        NamedMonikielinenTeksti nmkt = findByKey(dataKey);

        // Create new if not exists
        if (nmkt == null) {
            if (value == null) {
                return;
            }
            nmkt = new NamedMonikielinenTeksti();
            nmkt.setKey(dataKey);
        }

        // Trigger update
        getValues().remove(nmkt);
        getValues().add(nmkt);

        // Add/set translatable text
        if (nmkt.getValue() == null) {
            MonikielinenTeksti mt = new MonikielinenTeksti();
            nmkt.setValue(mt);
        }

        // Create/update the actual value with selected language
        if (value != null) {
            nmkt.getValue().addString(languageCode, value);
        } else {
            nmkt.getValue().getValues().remove(languageCode);
        }
    }

    /**
     * Get correct translated text with all languages.
     * 
     * @param dataKey
     * @return
     */
    public MonikielinenTeksti getNamedValue(String dataKey) {
        NamedMonikielinenTeksti t = findByKey(dataKey);
        return t == null ? null : t.getValue();
    }

    /**
     * Get named value for given language.
     * 
     * @param dataKey
     * @param languageCode
     * @return
     */
    public String getNamedValue(String dataKey, String languageCode) {
        MonikielinenTeksti t = getNamedValue(dataKey);
        return t == null ? null : t.getString(languageCode);
    }

    /*
     * Find named entry.
     */
    private NamedMonikielinenTeksti findByKey(String key) {
        for (NamedMonikielinenTeksti namedMonikielinenTeksti : values) {
            if (namedMonikielinenTeksti.getKey().equals(key)) {
                return namedMonikielinenTeksti;
            }
        }
        return null;
    }

    public void setNimi(String languageCode, String value) {
        if (getNimi() == null) {
            setNimi(new MonikielinenTeksti());
        }
        getNimi().addString(languageCode, value);
    }

    public String getNimi(String languageCode) {
        return getNimi() == null ? null : getNimi().getString(languageCode);
    }

    //
    // Getters setters
    //

    public Organisaatio getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisaatio organisation) {
        this.organisation = organisation;
    }

    public MonikielinenTeksti getNimi() {
        return nimi;
    }

    public void setNimi(MonikielinenTeksti nimi) {
        this.nimi = nimi;
    }

    public String getKoodi() {
        return koodi;
    }

    public void setKoodi(String koodi) {
        this.koodi = koodi;
    }

    public Set<NamedMonikielinenTeksti> getValues() {
        return values;
    }

    public void setValues(Set<NamedMonikielinenTeksti> values) {
        this.values = values;
    }

    public Date getLuontiPvm() {
        return luontiPvm;
    }

    public void setLuontiPvm(Date luontiPvm) {
        this.luontiPvm = luontiPvm;
    }

    public Date getMuokkausPvm() {
        return muokkausPvm;
    }

    public void setMuokkausPvm(Date muokkausPvm) {
        this.muokkausPvm = muokkausPvm;
    }
}
