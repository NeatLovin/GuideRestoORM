package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author cedric.baudet
 */
@Entity
@Table(name = "VILLES")
public class City implements IBusinessObject {

    @Id
    @Column(name = "NUMERO")
    @SequenceGenerator(name = "city_seq", sequenceName = "SEQ_VILLES", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "city_seq")
    private Integer id;

    @Column(name = "CODE_POSTAL", nullable = false)
    private String zipCode;

    @Column(name = "NOM_VILLE", nullable = false)
    private String cityName;

    @OneToMany(mappedBy = "address.city", cascade = CascadeType.ALL, orphanRemoval = false)
    private Set<Restaurant> restaurants = new HashSet<>();

    public City() {
        this(null, null);
    }

    public City(String zipCode, String cityName) {
        this(null, zipCode, cityName);
    }

    public City(Integer id, String zipCode, String cityName) {
        this.id = id;
        this.zipCode = zipCode;
        this.cityName = cityName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String city) {
        this.cityName = city;
    }

    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

}