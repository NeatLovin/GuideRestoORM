package ch.hearc.ig.guideresto.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

public class EvaluationCriteriaMapper extends AbstractMapper<ch.hearc.ig.guideresto.business.EvaluationCriteria> {
    private final EntityManager em;

    public EvaluationCriteriaMapper(EntityManager em) {
        this.em = em;
    }

    @Override
    public ch.hearc.ig.guideresto.business.EvaluationCriteria findById(int id) {
        ch.hearc.ig.guideresto.business.EvaluationCriteria cached = findInCache(id);
        if (cached != null)
            return cached;
        ch.hearc.ig.guideresto.business.EvaluationCriteria ec = em
                .find(ch.hearc.ig.guideresto.business.EvaluationCriteria.class, id);
        if (ec != null) {
            addToCache(ec);
        }
        return ec;
    }

    @Override
    public Set<ch.hearc.ig.guideresto.business.EvaluationCriteria> findAll() {
        TypedQuery<ch.hearc.ig.guideresto.business.EvaluationCriteria> query = em.createNamedQuery(
                "EvaluationCriteria.findAll", ch.hearc.ig.guideresto.business.EvaluationCriteria.class);
        List<ch.hearc.ig.guideresto.business.EvaluationCriteria> resultList = query.getResultList();
        Set<ch.hearc.ig.guideresto.business.EvaluationCriteria> result = new LinkedHashSet<>(resultList);
        for (ch.hearc.ig.guideresto.business.EvaluationCriteria ec : result) {
            addToCache(ec);
        }
        return result;
    }

    @Override
    public ch.hearc.ig.guideresto.business.EvaluationCriteria create(
            ch.hearc.ig.guideresto.business.EvaluationCriteria object) {
        if (object == null)
            return null;
        em.persist(object);
        addToCache(object);
        return object;
    }

    @Override
    public boolean update(ch.hearc.ig.guideresto.business.EvaluationCriteria object) {
        if (object == null || object.getId() == null)
            return false;
        em.merge(object);
        addToCache(object);
        return true;
    }

    @Override
    public boolean delete(ch.hearc.ig.guideresto.business.EvaluationCriteria object) {
        if (object == null || object.getId() == null)
            return false;
        ch.hearc.ig.guideresto.business.EvaluationCriteria managed = em.contains(object) ? object : em.merge(object);
        em.remove(managed);
        removeFromCache(object.getId());
        return true;
    }

    @Override
    public boolean deleteById(int id) {
        ch.hearc.ig.guideresto.business.EvaluationCriteria ec = findById(id);
        if (ec != null) {
            return delete(ec);
        }
        return false;
    }

    public Set<ch.hearc.ig.guideresto.business.EvaluationCriteria> findByName(String namePart) {
        if (namePart == null)
            return new LinkedHashSet<>();
        TypedQuery<ch.hearc.ig.guideresto.business.EvaluationCriteria> query = em.createNamedQuery(
                "EvaluationCriteria.findByName", ch.hearc.ig.guideresto.business.EvaluationCriteria.class);
        query.setParameter("name", "%" + namePart.toUpperCase() + "%");
        List<ch.hearc.ig.guideresto.business.EvaluationCriteria> resultList = query.getResultList();
        Set<ch.hearc.ig.guideresto.business.EvaluationCriteria> result = new LinkedHashSet<>(resultList);
        for (ch.hearc.ig.guideresto.business.EvaluationCriteria ec : result) {
            addToCache(ec);
        }
        return result;
    }
}
