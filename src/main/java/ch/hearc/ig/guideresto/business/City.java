package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;


/**
 * @author cedric.baudet
 */
@Entity
@Table(name = "VILLES")
@NamedQueries({
    @NamedQuery(name = "City.findAll", query = "SELECT c FROM City c ORDER BY c.cityName"),
    @NamedQuery(name = "City.findByZipCode", query = "SELECT c FROM City c WHERE c.zipCode = :zip ORDER BY c.cityName"),
    @NamedQuery(name = "City.findByName", query = "SELECT c FROM City c WHERE UPPER(c.cityName) LIKE :name ORDER BY c.cityName")
})
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


}