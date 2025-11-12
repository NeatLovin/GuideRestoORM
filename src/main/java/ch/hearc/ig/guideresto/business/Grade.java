package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

/**
 * @author cedric.baudet
 */
@Entity
@Table(name = "NOTES")
public class Grade implements IBusinessObject {

    @Id
    @Column(name = "NUMERO")
    @SequenceGenerator(name = "note_seq", sequenceName = "SEQ_NOTES", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "note_seq")
    private Integer id;

    @Column(name = "NOTE", nullable = false)
    private Integer grade;

    @Transient
    private CompleteEvaluation evaluation;

    @Transient
    private EvaluationCriteria criteria;

    // Colonnes simples pour FKs
    @Column(name = "FK_COMM", nullable = false)
    private Integer evaluationId;

    @Column(name = "FK_CRIT", nullable = false)
    private Integer criteriaId;

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
        this.evaluationId = evaluation != null ? evaluation.getId() : null;
        this.criteriaId = criteria != null ? criteria.getId() : null;
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
        this.evaluationId = evaluation != null ? evaluation.getId() : null;
    }

    public EvaluationCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(EvaluationCriteria criteria) {
        this.criteria = criteria;
        this.criteriaId = criteria != null ? criteria.getId() : null;
    }


}