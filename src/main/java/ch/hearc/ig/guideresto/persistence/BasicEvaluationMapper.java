package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.BasicEvaluation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BasicEvaluationMapper extends AbstractMapper<BasicEvaluation> {
    private final EntityManager em;

    public BasicEvaluationMapper(EntityManager em) {
        this.em = em;
    }



    @Override
    protected String getSequenceQuery() { return null; }
    @Override
    protected String getExistsQuery() { return null; }
    @Override
    protected String getCountQuery() { return null; }

    @Override
    public BasicEvaluation findById(int id) {
        BasicEvaluation cached = findInCache(id);
        if (cached != null) return cached;
        BasicEvaluation evaluation = em.find(BasicEvaluation.class, id);
        if (evaluation != null) {
            addToCache(evaluation);
        }
        return evaluation;
    }

    @Override
    public Set<BasicEvaluation> findAll() {
        if (!identityMap().isEmpty()) {
            return new LinkedHashSet<>(identityMap().values());
        }
        if (!cache.isEmpty()) {
            return new LinkedHashSet<>(cache.values());
        }
        TypedQuery<BasicEvaluation> query = em.createNamedQuery("BasicEvaluation.findAll", BasicEvaluation.class);
        List<BasicEvaluation> resultList = query.getResultList();
        Set<BasicEvaluation> res = new LinkedHashSet<>(resultList);
        for (BasicEvaluation be : res) {
            addToCache(be);
        }
        return res;
    }

    @Override
    public BasicEvaluation create(BasicEvaluation evaluation) {
        if (evaluation == null) return null;
        em.persist(evaluation);
        addToCache(evaluation);
        return evaluation;
    }

    @Override
    public boolean update(BasicEvaluation evaluation) {
        if (evaluation == null || evaluation.getId() == null) return false;
        em.merge(evaluation);
        addToCache(evaluation);
        return true;
    }

    @Override
    public boolean delete(BasicEvaluation evaluation) {
        if (evaluation == null || evaluation.getId() == null) return false;
        BasicEvaluation managed = em.contains(evaluation) ? evaluation : em.merge(evaluation);
        em.remove(managed);
        removeFromCache(evaluation.getId());
        return true;
    }

    @Override
    public boolean deleteById(int id) {
        BasicEvaluation evaluation = findById(id);
        if (evaluation != null) {
            return delete(evaluation);
        }
        return false;
    }

    public Set<BasicEvaluation> findByRestaurantId(int restaurantId) {
        TypedQuery<BasicEvaluation> query = em.createNamedQuery("BasicEvaluation.findByRestaurant", BasicEvaluation.class);
        query.setParameter("restaurantId", restaurantId);
        List<BasicEvaluation> resultList = query.getResultList();
        Set<BasicEvaluation> res = new LinkedHashSet<>(resultList);
        for (BasicEvaluation be : res) {
            addToCache(be);
        }
        return res;
    }

    // Méthode mapRow supprimée (JPA gère le mapping)
}
