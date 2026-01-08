package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
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

    @Override
    public CompleteEvaluation findById(int id) {
        CompleteEvaluation cached = findInCache(id);
        if (cached != null)
            return cached;
        CompleteEvaluation evaluation = em.find(CompleteEvaluation.class, id);
        if (evaluation != null) {
            evaluation.setGrades(new LinkedHashSet<>(gradeMapper.findByEvaluationId(evaluation.getId())));
            addToCache(evaluation);
        }
        return evaluation;
    }

    @Override
    public Set<CompleteEvaluation> findAll() {
        TypedQuery<CompleteEvaluation> query = em.createNamedQuery("CompleteEvaluation.findAll",
                CompleteEvaluation.class);
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
        if (evaluation == null)
            return null;
        em.persist(evaluation);
        // Les grades sont persistés via cascade définie sur CompleteEvaluation.grades
        addToCache(evaluation);
        return evaluation;
    }

    @Override
    public boolean update(CompleteEvaluation evaluation) {
        if (evaluation == null || evaluation.getId() == null)
            return false;
        em.merge(evaluation);
        addToCache(evaluation);
        return true;
    }

    @Override
    public boolean delete(CompleteEvaluation evaluation) {
        if (evaluation == null || evaluation.getId() == null)
            return false;
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
        TypedQuery<CompleteEvaluation> query = em.createNamedQuery("CompleteEvaluation.findByRestaurant",
                CompleteEvaluation.class);
        query.setParameter("restaurantId", restaurantId);
        List<CompleteEvaluation> resultList = query.getResultList();
        Set<CompleteEvaluation> result = new LinkedHashSet<>(resultList);
        for (CompleteEvaluation evaluation : result) {
            evaluation.setGrades(new LinkedHashSet<>(gradeMapper.findByEvaluationId(evaluation.getId())));
            addToCache(evaluation);
        }
        return result;
    }

    public Set<CompleteEvaluation> findByUsername(String username) {
        if (username == null)
            return new LinkedHashSet<>();
        TypedQuery<CompleteEvaluation> query = em.createNamedQuery("CompleteEvaluation.findByUsername",
                CompleteEvaluation.class);
        query.setParameter("username", username.toUpperCase());
        List<CompleteEvaluation> resultList = query.getResultList();
        Set<CompleteEvaluation> result = new LinkedHashSet<>(resultList);
        for (CompleteEvaluation evaluation : result) {
            evaluation.setGrades(new LinkedHashSet<>(gradeMapper.findByEvaluationId(evaluation.getId())));
            addToCache(evaluation);
        }
        return result;
    }
}
