package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import java.util.Date;
import ch.hearc.ig.guideresto.persistence.jpa.BooleanConverter;

/**
 * @author cedric.baudet
 */
@Entity
@Table(name = "LIKES")
@NamedQueries({
    @NamedQuery(name = "BasicEvaluation.findAll", query = "select b from BasicEvaluation b order by b.id"),
    @NamedQuery(name = "BasicEvaluation.findByRestaurant", query = "select b from BasicEvaluation b where b.restaurant.id = :restaurantId order by b.id")
})
public class BasicEvaluation extends Evaluation {

    @Convert(converter = BooleanConverter.class)
    @Column(name = "APPRECIATION", nullable = false, length = 1)
    private Boolean likeRestaurant;

    @Column(name = "ADRESSE_IP", nullable = false)
    private String ipAddress;

    public BasicEvaluation() {
        this(null, null, null, null);
    }

    public BasicEvaluation(Date visitDate, Restaurant restaurant, Boolean likeRestaurant, String ipAddress) {
        this(null, visitDate, restaurant, likeRestaurant, ipAddress);
    }

    public BasicEvaluation(Integer id, Date visitDate, Restaurant restaurant, Boolean likeRestaurant, String ipAddress) {
        super(id, visitDate, restaurant);
        this.likeRestaurant = likeRestaurant;
        this.ipAddress = ipAddress;
    }

    public Boolean getLikeRestaurant() {
        return likeRestaurant;
    }

    public void setLikeRestaurant(Boolean likeRestaurant) {
        this.likeRestaurant = likeRestaurant;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

}