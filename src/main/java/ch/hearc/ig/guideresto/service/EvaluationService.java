package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.BasicEvaluation;
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
     * Crée une evaluation basique en transaction.
     *
     * Grâce à la cascade CascadeType.ALL sur Restaurant.evaluations,
     * l'évaluation basique sera automatiquement persistée quand le restaurant parent est synchronisé.
     */
    public BasicEvaluation createBasicEvaluation(BasicEvaluation evaluation) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Persist the basic evaluation
            em.persist(evaluation);
            em.flush();
            tx.commit();
            return evaluation;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    /**
     * Crée une évaluation complète et ses notes (grades) dans une transaction.
     * Simplifié : persister l'évaluation et laisser JPA cascader les grades.
     *
     * Grâce à la cascade CascadeType.ALL sur CompleteEvaluation.grades,
     * tous les grades associés seront automatiquement persistés lors de la sauvegarde de l'évaluation.
     */
    public CompleteEvaluation createCompleteEvaluation(CompleteEvaluation evaluation) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Persister l'évaluation complète - ses grades seront cascadés automatiquement
            em.persist(evaluation);
            // Flush pour s'assurer que tout est bien inséré dans la BD
            em.flush();
            tx.commit();
            return evaluation;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
}
