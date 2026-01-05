package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author cedric.baudet
 */
@Entity
@Table(name = "TYPES_GASTRONOMIQUES")
@NamedQueries({
    @NamedQuery(name = "RestaurantType.findAll", query = "SELECT t FROM RestaurantType t ORDER BY t.label"),
    @NamedQuery(name = "RestaurantType.findByName", query = "SELECT t FROM RestaurantType t WHERE UPPER(t.label) LIKE :name ORDER BY t.label")
})
public class RestaurantType implements IBusinessObject {

    @Id
    @Column(name = "NUMERO")
    @SequenceGenerator(name = "type_seq", sequenceName = "SEQ_TYPES_GASTRONOMIQUES", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "type_seq")
    private Integer id;

    @Column(name = "LIBELLE", nullable = false, unique = true)
    private String label;

    @Lob
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @OneToMany(mappedBy = "type", cascade = CascadeType.PERSIST)
    private Set<Restaurant> restaurants = new HashSet<>();

    public RestaurantType() {
        this(null, null);
    }

    public RestaurantType(String label, String description) {
        this(null, label, description);
    }

    public RestaurantType(Integer id, String label, String description) {
        this.id = id;
        this.label = label;
        this.description = description;
    }

    @Override
    public String toString() {
        return label;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

}