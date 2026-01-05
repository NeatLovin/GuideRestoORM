package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.Grade;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.CompleteEvaluationMapper;
import ch.hearc.ig.guideresto.persistence.GradeMapper;
import ch.hearc.ig.guideresto.persistence.BasicEvaluationMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.Set;

/**
 * Service responsible for managing evaluation transactions.
 * Handles the creation of both BasicEvaluation and CompleteEvaluation with their associated Grades.
 * All operations are transactional and managed through this service layer.
 */
public class EvaluationService {
    private final EntityManager em;
    private final CompleteEvaluationMapper completeEvaluationMapper;
    private final GradeMapper gradeMapper;
    private final BasicEvaluationMapper basicEvaluationMapper;

    public EvaluationService(EntityManager em, CompleteEvaluationMapper completeEvaluationMapper, GradeMapper gradeMapper,
                            BasicEvaluationMapper basicEvaluationMapper) {
        this.em = em;
        this.completeEvaluationMapper = completeEvaluationMapper;
        this.gradeMapper = gradeMapper;
        this.basicEvaluationMapper = basicEvaluationMapper;
    }

    /**
     * Crée une évaluation basique en transaction.
     *
     * Grâce à la cascade CascadeType.ALL sur Restaurant.evaluations,
     * l'évaluation basique sera automatiquement persistée quand le restaurant parent est synchronisé.
     *
     * @param evaluation l'évaluation basique à créer
     * @return l'évaluation créée avec son ID généré
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
     *
     * Transaction complexe:
     * 1. Persiste l'évaluation complète (CompleteEvaluation)
     * 2. Persiste tous les grades associés
     * 3. Lie les grades à l'évaluation
     *
     * Grâce à la cascade CascadeType.ALL sur CompleteEvaluation.grades,
     * tous les grades associés seront automatiquement persistés lors de la sauvegarde de l'évaluation.
     *
     * @param evaluation l'évaluation complète avec ses grades pré-ajoutés
     * @return l'évaluation créée avec tous ses grades persistés
     */
    public CompleteEvaluation createCompleteEvaluation(CompleteEvaluation evaluation) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // Vérifier que le restaurant est attaché
            if (evaluation.getRestaurant() != null && !em.contains(evaluation.getRestaurant())) {
                evaluation.setRestaurant(em.merge(evaluation.getRestaurant()));
            }

            // Persister l'évaluation complète - ses grades seront cascadés automatiquement
            em.persist(evaluation);

            // S'assurer que tous les grades ont l'évaluation correctement définie
            for (Grade grade : evaluation.getGrades()) {
                grade.setEvaluation(evaluation);
                if (grade.getCriteria() != null && !em.contains(grade.getCriteria())) {
                    grade.setCriteria(em.merge(grade.getCriteria()));
                }
                em.persist(grade);
            }

            // Flush pour s'assurer que tout est bien inséré dans la BD
            em.flush();
            tx.commit();
            return evaluation;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    /**
     * Met à jour une évaluation complète et ses grades
     * @param evaluation l'évaluation à mettre à jour
     * @return l'évaluation mise à jour
     */
    public CompleteEvaluation updateCompleteEvaluation(CompleteEvaluation evaluation) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CompleteEvaluation merged = em.merge(evaluation);
            em.flush();
            tx.commit();
            return merged;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    /**
     * Supprime une évaluation complète et tous ses grades (cascade)
     * @param evaluation l'évaluation à supprimer
     * @return true si suppression réussie
     */
    public boolean deleteCompleteEvaluation(CompleteEvaluation evaluation) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CompleteEvaluation managed = em.contains(evaluation) ? evaluation : em.merge(evaluation);
            em.remove(managed);
            em.flush();
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    /**
     * Supprime une évaluation basique
     * @param evaluation l'évaluation à supprimer
     * @return true si suppression réussie
     */
    public boolean deleteBasicEvaluation(BasicEvaluation evaluation) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            BasicEvaluation managed = em.contains(evaluation) ? evaluation : em.merge(evaluation);
            em.remove(managed);
            em.flush();
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    /**
     * Ajoute un grade à une évaluation complète
     * @param evaluation l'évaluation complète
     * @param grade le grade à ajouter
     */
    public void addGradeToEvaluation(CompleteEvaluation evaluation, Grade grade) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CompleteEvaluation managed = em.contains(evaluation) ? evaluation : em.merge(evaluation);
            grade.setEvaluation(managed);
            if (grade.getCriteria() != null && !em.contains(grade.getCriteria())) {
                grade.setCriteria(em.merge(grade.getCriteria()));
            }
            em.persist(grade);
            managed.getGrades().add(grade);
            em.flush();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    /**
     * Supprime un grade d'une évaluation complète
     * @param evaluation l'évaluation
     * @param grade le grade à supprimer
     */
    public void removeGradeFromEvaluation(CompleteEvaluation evaluation, Grade grade) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CompleteEvaluation managed = em.contains(evaluation) ? evaluation : em.merge(evaluation);
            Grade managedGrade = em.contains(grade) ? grade : em.merge(grade);
            managed.getGrades().remove(managedGrade);
            em.remove(managedGrade);
            em.flush();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    /**
     * Trouve toutes les évaluations basiques pour un restaurant
     * @param restaurant le restaurant
     * @return ensemble des évaluations basiques
     */
    public Set<BasicEvaluation> findBasicEvaluationsByRestaurant(Restaurant restaurant) {
        return basicEvaluationMapper.findByRestaurantId(restaurant.getId());
    }

    /**
     * Trouve toutes les évaluations complètes pour un restaurant
     * @param restaurant le restaurant
     * @return ensemble des évaluations complètes
     */
    public Set<CompleteEvaluation> findCompleteEvaluationsByRestaurant(Restaurant restaurant) {
        return completeEvaluationMapper.findByRestaurantId(restaurant.getId());
    }

    /**
     * Trouve toutes les évaluations complètes par utilisateur
     * @param username le nom d'utilisateur
     * @return ensemble des évaluations
     */
    public Set<CompleteEvaluation> findCompleteEvaluationsByUsername(String username) {
        return completeEvaluationMapper.findByUsername(username);
    }

    /**
     * Trouve tous les grades pour une évaluation
     * @param evaluation l'évaluation
     * @return ensemble des grades
     */
    public Set<Grade> findGradesForEvaluation(CompleteEvaluation evaluation) {
        return gradeMapper.findByEvaluation(evaluation);
    }
}
