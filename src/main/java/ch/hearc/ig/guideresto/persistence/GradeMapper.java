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

    // CRUD de base
    @Override
    public Grade findById(int id) {
        Grade cached = findInCache(id);
        if (cached != null)
            return cached;
        Grade grade = em.find(Grade.class, id);
        if (grade != null) {
            addToCache(grade);
        }
        return grade;
    }

    @Override
    public Set<Grade> findAll() {
        TypedQuery<Grade> query = em.createNamedQuery("Grade.findAll", Grade.class);
        List<Grade> resultList = query.getResultList();
        Set<Grade> result = new LinkedHashSet<>(resultList);
        for (Grade grade : result) {
            addToCache(grade);
        }
        return result;
    }

    @Override
    public Grade create(Grade object) {
        if (object == null)
            return null;
        em.persist(object);
        addToCache(object);
        return object;
    }

    @Override
    public boolean update(Grade object) {
        if (object == null || object.getId() == null)
            return false;
        Grade managed = em.merge(object);
        addToCache(managed);
        return true;
    }

    @Override
    public boolean delete(Grade object) {
        if (object == null || object.getId() == null)
            return false;
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
        TypedQuery<Grade> query = em.createNamedQuery("Grade.findByEvaluation", Grade.class);
        query.setParameter("evaluationId", evaluationId);
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
        TypedQuery<Grade> query = em.createNamedQuery("Grade.findByCriteria", Grade.class);
        query.setParameter("criteriaId", criteriaId);
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
        TypedQuery<Grade> query = em.createQuery(
                "SELECT g FROM Grade g WHERE g.evaluation.id = :evalId AND g.criteria.id = :critId", Grade.class);
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
