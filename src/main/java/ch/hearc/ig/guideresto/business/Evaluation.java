package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import java.util.Date;

/**
 * @author cedric.baudet
 */
@MappedSuperclass
public abstract class Evaluation implements IBusinessObject {

    @Id
    @Column(name = "NUMERO")
    @SequenceGenerator(name = "eval_seq", sequenceName = "SEQ_EVAL", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "eval_seq")
    private Integer id;

    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_EVAL")
    private Date visitDate;

    @Transient
    private Restaurant restaurant;

    public Evaluation() {
        this(null, null, null);
    }

    public Evaluation(Integer id, Date visitDate, Restaurant restaurant) {
        this.id = id;
        this.visitDate = visitDate;
        this.restaurant = restaurant;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

}