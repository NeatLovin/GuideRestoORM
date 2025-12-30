package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Grade;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CompleteEvaluationMapper extends AbstractMapper<CompleteEvaluation> {
    private final EntityManager em;
    private final GradeMapper gradeMapper;

    public CompleteEvaluationMapper(EntityManager em, GradeMapper gradeMapper) {
        this.em = em;
        this.gradeMapper = gradeMapper;
    }

    // Requêtes pour COMMENTAIRES

    // Métadonnées
    private static final String EXISTS_QUERY = "SELECT 1 FROM COMMENTAIRES WHERE numero = ?";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM COMMENTAIRES";
    private static final String SEQUENCE_QUERY = "SELECT SEQ_EVAL.CURRVAL FROM DUAL";



    @Override
    protected String getSequenceQuery() { return SEQUENCE_QUERY; }

    @Override
    protected String getExistsQuery() { return EXISTS_QUERY; }

    @Override
    protected String getCountQuery() { return COUNT_QUERY; }

    @Override
    public CompleteEvaluation findById(int id) {
        CompleteEvaluation cached = findInCache(id);
        if (cached != null) return cached;
        CompleteEvaluation evaluation = em.find(CompleteEvaluation.class, id);
        if (evaluation != null) {
            evaluation.setGrades(new LinkedHashSet<>(gradeMapper.findByEvaluationId(evaluation.getId())));
            addToCache(evaluation);
        }
        return evaluation;
    }

    @Override
    public Set<CompleteEvaluation> findAll() {
        if (!identityMap().isEmpty()) {
            return new LinkedHashSet<>(identityMap().values());
        }
        if (!cache.isEmpty()) {
            return new LinkedHashSet<>(cache.values());
        }
        TypedQuery<CompleteEvaluation> query = em.createQuery("SELECT c FROM CompleteEvaluation c ORDER BY c.id", CompleteEvaluation.class);
        List<CompleteEvaluation> resultList = query.getResultList();
        Set<CompleteEvaluation> result = new LinkedHashSet<>(resultList);
        for (CompleteEvaluation evaluation : result) {
            evaluation.setGrades(new LinkedHashSet<>(gradeMapper.findByEvaluationId(evaluation.getId())));
            addToCache(evaluation);
        }
        return result;
    }

    @Override
    public CompleteEvaluation create(CompleteEvaluation evaluation) {
        if (evaluation == null) return null;
        em.persist(evaluation);
        if (evaluation.getGrades() != null) {
            for (Grade grade : evaluation.getGrades()) {
                grade.setEvaluation(evaluation);
                gradeMapper.create(grade);
            }
        }
        addToCache(evaluation);
        return evaluation;
    }

    @Override
    public boolean update(CompleteEvaluation evaluation) {
        if (evaluation == null || evaluation.getId() == null) return false;
        em.merge(evaluation);
        addToCache(evaluation);
        return true;
    }

    @Override
    public boolean delete(CompleteEvaluation evaluation) {
        if (evaluation == null || evaluation.getId() == null) return false;
        CompleteEvaluation managed = em.contains(evaluation) ? evaluation : em.merge(evaluation);
        em.remove(managed);
        removeFromCache(evaluation.getId());
        return true;
    }

    @Override
    public boolean deleteById(int id) {
        CompleteEvaluation evaluation = findById(id);
        if (evaluation != null) {
            return delete(evaluation);
        }
        return false;
    }

    // Finders additionnels
    public Set<CompleteEvaluation> findByRestaurantId(int restaurantId) {
        TypedQuery<CompleteEvaluation> query = em.createQuery("SELECT c FROM CompleteEvaluation c WHERE c.restaurant.id = :restId ORDER BY c.id", CompleteEvaluation.class);
        query.setParameter("restId", restaurantId);
        List<CompleteEvaluation> resultList = query.getResultList();
        Set<CompleteEvaluation> result = new LinkedHashSet<>(resultList);
        for (CompleteEvaluation evaluation : result) {
            evaluation.setGrades(new LinkedHashSet<>(gradeMapper.findByEvaluationId(evaluation.getId())));
            addToCache(evaluation);
        }
        return result;
    }

    public Set<CompleteEvaluation> findByUsername(String username) {
        if (username == null) return new LinkedHashSet<>();
        TypedQuery<CompleteEvaluation> query = em.createQuery("SELECT c FROM CompleteEvaluation c WHERE UPPER(c.username) = :uname ORDER BY c.id", CompleteEvaluation.class);
        query.setParameter("uname", username.toUpperCase());
        List<CompleteEvaluation> resultList = query.getResultList();
        Set<CompleteEvaluation> result = new LinkedHashSet<>(resultList);
        for (CompleteEvaluation evaluation : result) {
            evaluation.setGrades(new LinkedHashSet<>(gradeMapper.findByEvaluationId(evaluation.getId())));
            addToCache(evaluation);
        }
        return result;
    }
}

