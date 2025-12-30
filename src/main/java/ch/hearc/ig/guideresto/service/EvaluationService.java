package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Grade;
import ch.hearc.ig.guideresto.persistence.CompleteEvaluationMapper;
import ch.hearc.ig.guideresto.persistence.GradeMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.Set;

public class EvaluationService {
    private final EntityManager em;
    private final CompleteEvaluationMapper completeEvaluationMapper;
    private final GradeMapper gradeMapper;

    public EvaluationService(EntityManager em, CompleteEvaluationMapper completeEvaluationMapper, GradeMapper gradeMapper) {
        this.em = em;
        this.completeEvaluationMapper = completeEvaluationMapper;
        this.gradeMapper = gradeMapper;
    }

    /**
     * Crée une évaluation complète et ses notes (grades) dans une transaction.
     */
    public CompleteEvaluation createCompleteEvaluation(CompleteEvaluation evaluation, Set<Grade> grades) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            completeEvaluationMapper.create(evaluation);
            if (grades != null) {
                for (Grade grade : grades) {
                    grade.setEvaluation(evaluation);
                    gradeMapper.create(grade);
                }
            }
            tx.commit();
            return evaluation;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
}
