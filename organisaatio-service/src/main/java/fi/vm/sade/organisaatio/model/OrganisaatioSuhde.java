package fi.vm.sade.organisaatio.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import fi.vm.sade.generic.model.BaseEntity;

/**
 * This entity is used to manage relationships between Organisaatio's.
 * It is used to keep track of historical "as it was" situations.
 *
 * @author mlyly
 */
@Entity
@Table(name = "organisaatiosuhde")
// @Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"oid"})})
@org.hibernate.annotations.Table(appliesTo = "organisaatiosuhde", comment = "Sisältää organisaatioiden väliset suhteet. Suhteen tyyppejä ovat LIITOS ja HISTORIA.")
public class OrganisaatioSuhde extends BaseEntity {

	private static final long serialVersionUID = 1L;

    /**
     * Relation types.
     * Possible extension point for different types of relations (belongs, relates, ...)
     */
    public enum OrganisaatioSuhdeTyyppi {
        /**
         * This is used when Organisation is "moved" so that the old relation is stored for later history browsing.
         */
         HISTORIA,
        /**
         * When old Organisation ceases to exist, old ones should be attached to it with this type of information.
         */
        LIITOS
    };

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganisaatioSuhdeTyyppi suhdeTyyppi = OrganisaatioSuhdeTyyppi.HISTORIA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Organisaatio parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Organisaatio child;

    @Temporal(TemporalType.TIMESTAMP)
    private Date alkuPvm;

    @Temporal(TemporalType.TIMESTAMP)
    private Date loppuPvm;
    
    private String opetuspisteenJarjNro;
    

    public String getOpetuspisteenJarjNro() {
        return opetuspisteenJarjNro;
    }

    public void setOpetuspisteenJarjNro(String opetuspisteenJarjNro) {
        this.opetuspisteenJarjNro = opetuspisteenJarjNro;
    }

    public Organisaatio getParent() {
        return parent;
    }

    public void setParent(Organisaatio parent) {
        this.parent = parent;
    }

    public Organisaatio getChild() {
        return child;
    }

    public void setChild(Organisaatio child) {
        this.child = child;
    }

    public Date getAlkuPvm() {
        return alkuPvm;
    }

    public void setAlkuPvm(Date alkuPvm) {
        this.alkuPvm = alkuPvm;
    }

    public Date getLoppuPvm() {
        return loppuPvm;
    }

    public void setLoppuPvm(Date loppuPvm) {
        this.loppuPvm = loppuPvm;
    }

    public OrganisaatioSuhdeTyyppi getSuhdeTyyppi() {
        return suhdeTyyppi;
    }

    public void setSuhdeTyyppi(OrganisaatioSuhdeTyyppi suhdeTyyppi) {
        this.suhdeTyyppi = suhdeTyyppi;
    }

}
