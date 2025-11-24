package ch.hearc.ig.guideresto.business;

import org.apache.commons.collections4.CollectionUtils;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author cedric.baudet
 */
@Entity
@Table(name = "RESTAURANTS")
@NamedQueries({
    @NamedQuery(name = "Restaurant.findAll", query = "select distinct r from Restaurant r"),
    @NamedQuery(name = "Restaurant.findByName", query = "select r from Restaurant r where upper(r.name) like upper(:name)"),
    @NamedQuery(name = "Restaurant.findByCity", query = "select r from Restaurant r join r.address.city c where upper(c.cityName) like upper(:cityName)")
})
public class Restaurant implements IBusinessObject {

    @Id
    @Column(name = "NUMERO")
    @SequenceGenerator(name = "resto_seq", sequenceName = "SEQ_RESTAURANTS", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resto_seq")
    private Integer id;

    @Column(name = "NOM", nullable = false)
    private String name;

    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "SITE_WEB")
    private String website;

    @Embedded
    private Localisation address;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_TYPE")
    private RestaurantType type;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Evaluation> evaluations = new HashSet<>();

    public Restaurant() {
        this(null, null, null, null, null, null);
    }

    public Restaurant(Integer id, String name, String description, String website, String street, City city, RestaurantType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.website = website;
        this.evaluations = new HashSet<>();
        this.address = new Localisation(street, city);
        this.type = type;
    }

    public Restaurant(Integer id, String name, String description, String website, Localisation address, RestaurantType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.website = website;
        this.evaluations = new HashSet<>();
        this.address = address;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Set<Evaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(Set<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }

    public Localisation getAddress() {
        return address;
    }

    public void setAddress(Localisation address) {
        this.address = address;
    }

    public RestaurantType getType() {
        return type;
    }

    public void setType(RestaurantType type) {
        this.type = type;
    }

    public boolean hasEvaluations() {
        return CollectionUtils.isNotEmpty(evaluations);
    }

    @Transient
    public String getStreet() {
        return this.address != null ? this.address.getStreet() : null;
    }

    public void setStreet(String street) {
        if (this.address == null) {
            this.address = new Localisation();
        }
        this.address.setStreet(street);
    }

}