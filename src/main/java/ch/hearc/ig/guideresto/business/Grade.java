package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

/**
 * @author cedric.baudet
 */
@Entity
@Table(name = "NOTES")
@NamedQueries({
    @NamedQuery(name = "Grade.findAll", query = "select g from Grade g order by g.id"),
    @NamedQuery(name = "Grade.findByEvaluation", query = "select g from Grade g where g.evaluation.id = :evaluationId order by g.id"),
    @NamedQuery(name = "Grade.findByCriteria", query = "select g from Grade g where g.criteria.id = :criteriaId order by g.id")
})
public class Grade implements IBusinessObject {

    @Id
    @Column(name = "NUMERO")
    @SequenceGenerator(name = "note_seq", sequenceName = "SEQ_NOTES", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "note_seq")
    private Integer id;

    @Column(name = "NOTE", nullable = false)
    private Integer grade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_COMM")
    private CompleteEvaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_CRIT")
    private EvaluationCriteria criteria;

    public Grade() {
        this(null, null, null);
    }

    public Grade(Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
        this(null, grade, evaluation, criteria);
    }

    public Grade(Integer id, Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
        this.id = id;
        this.grade = grade;
        this.evaluation = evaluation;
        this.criteria = criteria;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public CompleteEvaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(CompleteEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    public EvaluationCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(EvaluationCriteria criteria) {
        this.criteria = criteria;
    }


}