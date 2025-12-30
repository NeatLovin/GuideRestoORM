package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.Grade;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

public class GradeMapper extends AbstractMapper<Grade> {
    private final EntityManager em;

    public GradeMapper(EntityManager em) {
        this.em = em;
    }




    // Les méthodes getSequenceQuery, getExistsQuery, getCountQuery ne sont plus nécessaires avec JPA
    @Override
    protected String getSequenceQuery() { return null; }
    @Override
    protected String getExistsQuery() { return null; }
    @Override
    protected String getCountQuery() { return null; }

    // CRUD de base
    @Override
    public Grade findById(int id) {
        Grade cached = findInCache(id);
        if (cached != null) return cached;
        Grade grade = em.find(Grade.class, id);
        if (grade != null) {
            addToCache(grade);
        }
        return grade;
    }

    @Override
    public Set<Grade> findAll() {
        if (!identityMap().isEmpty()) {
            return new LinkedHashSet<>(identityMap().values());
        }
        if (!cache.isEmpty()) {
            return new LinkedHashSet<>(cache.values());
        }
        TypedQuery<Grade> query = em.createQuery("SELECT g FROM Grade g ORDER BY g.id", Grade.class);
        List<Grade> resultList = query.getResultList();
        Set<Grade> result = new LinkedHashSet<>(resultList);
        for (Grade grade : result) {
            addToCache(grade);
        }
        return result;
    }

    @Override
    public Grade create(Grade object) {
        if (object == null) return null;
        em.persist(object);
        addToCache(object);
        return object;
    }

    @Override
    public boolean update(Grade object) {
        if (object == null || object.getId() == null) return false;
        em.merge(object);
        addToCache(object);
        return true;
    }

    @Override
    public boolean delete(Grade object) {
        if (object == null || object.getId() == null) return false;
        Grade managed = em.contains(object) ? object : em.merge(object);
        em.remove(managed);
        removeFromCache(object.getId());
        return true;
    }

    @Override
    public boolean deleteById(int id) {
        Grade grade = findById(id);
        if (grade != null) {
            return delete(grade);
        }
        return false;
    }

    // Méthodes de recherche utiles
    public Set<Grade> findByEvaluationId(int evaluationId) {
        TypedQuery<Grade> query = em.createQuery("SELECT g FROM Grade g WHERE g.evaluation.id = :evalId ORDER BY g.id", Grade.class);
        query.setParameter("evalId", evaluationId);
        List<Grade> resultList = query.getResultList();
        Set<Grade> result = new LinkedHashSet<>(resultList);
        for (Grade grade : result) {
            addToCache(grade);
        }
        return result;
    }

    public Set<Grade> findByEvaluation(CompleteEvaluation evaluation) {
        if (evaluation == null || evaluation.getId() == null) {
            return new LinkedHashSet<>();
        }
        return findByEvaluationId(evaluation.getId());
    }

    public Set<Grade> findByCriteriaId(int criteriaId) {
        TypedQuery<Grade> query = em.createQuery("SELECT g FROM Grade g WHERE g.criteria.id = :critId ORDER BY g.id", Grade.class);
        query.setParameter("critId", criteriaId);
        List<Grade> resultList = query.getResultList();
        Set<Grade> result = new LinkedHashSet<>(resultList);
        for (Grade grade : result) {
            addToCache(grade);
        }
        return result;
    }

    public Set<Grade> findByCriteria(EvaluationCriteria criteria) {
        if (criteria == null || criteria.getId() == null) {
            return new LinkedHashSet<>();
        }
        return findByCriteriaId(criteria.getId());
    }

    public Grade findOneByEvaluationAndCriteria(int evaluationId, int criteriaId) {
        TypedQuery<Grade> query = em.createQuery("SELECT g FROM Grade g WHERE g.evaluation.id = :evalId AND g.criteria.id = :critId", Grade.class);
        query.setParameter("evalId", evaluationId);
        query.setParameter("critId", criteriaId);
        List<Grade> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            Grade grade = resultList.get(0);
            addToCache(grade);
            return grade;
        }
        return null;
    }
}

