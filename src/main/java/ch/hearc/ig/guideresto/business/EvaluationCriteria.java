package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author cedric.baudet
 */
@Entity
@Table(name = "CRITERES_EVALUATION")
@NamedQueries({
    @NamedQuery(name = "EvaluationCriteria.findAll", query = "SELECT c FROM EvaluationCriteria c ORDER BY c.name"),
    @NamedQuery(name = "EvaluationCriteria.findByName", query = "SELECT c FROM EvaluationCriteria c WHERE UPPER(c.name) LIKE :name ORDER BY c.name")
})
public class EvaluationCriteria implements IBusinessObject {

    @Id
    @Column(name = "NUMERO")
    @SequenceGenerator(name = "crit_seq", sequenceName = "SEQ_CRITERES_EVALUATION", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crit_seq")
    private Integer id;

    @Column(name = "NOM", nullable = false, unique = true)
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @OneToMany(mappedBy = "criteria", cascade = CascadeType.ALL, orphanRemoval = false)
    private Set<Grade> grades = new HashSet<>();

    public EvaluationCriteria() {
        this(null, null);
    }

    public EvaluationCriteria(String name, String description) {
        this(null, name, description);
    }

    public EvaluationCriteria(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    public Set<Grade> getGrades() {
        return grades;
    }

    public void setGrades(Set<Grade> grades) {
        this.grades = grades;
    }
}