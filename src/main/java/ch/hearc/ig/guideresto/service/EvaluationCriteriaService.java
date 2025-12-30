package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.persistence.EvaluationCriteriaMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.Set;

public class EvaluationCriteriaService {
    private final EntityManager em;
    private final EvaluationCriteriaMapper criteriaMapper;

    public EvaluationCriteriaService(EntityManager em, EvaluationCriteriaMapper criteriaMapper) {
        this.em = em;
        this.criteriaMapper = criteriaMapper;
    }

    public EvaluationCriteria createCriteria(EvaluationCriteria criteria) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            criteriaMapper.create(criteria);
            tx.commit();
            return criteria;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public EvaluationCriteria updateCriteria(EvaluationCriteria criteria) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            criteriaMapper.update(criteria);
            tx.commit();
            return criteria;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public boolean deleteCriteria(EvaluationCriteria criteria) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            boolean result = criteriaMapper.delete(criteria);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public Set<EvaluationCriteria> findAllCriteria() {
        return criteriaMapper.findAll();
    }

    public EvaluationCriteria findCriteriaById(int id) {
        return criteriaMapper.findById(id);
    }
}
