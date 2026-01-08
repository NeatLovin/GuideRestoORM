package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.Grade;
import ch.hearc.ig.guideresto.persistence.GradeMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.Set;

/**
 * Service applicatif pour gérer les {@link Grade}.
 * Encapsule les écritures dans une transaction JPA (begin/commit/rollback).
 * Les lectures délèguent au {@link GradeMapper}.
 */
public class GradeService {
    private final EntityManager em;
    private final GradeMapper gradeMapper;

    public GradeService(EntityManager em, GradeMapper gradeMapper) {
        this.em = em;
        this.gradeMapper = gradeMapper;
    }

    public Grade createGrade(Grade grade) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            gradeMapper.create(grade);
            tx.commit();
            return grade;
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        }
    }

    public Grade updateGrade(Grade grade) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            gradeMapper.update(grade);
            tx.commit();
            return grade;
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        }
    }

    public boolean deleteGrade(Grade grade) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            boolean result = gradeMapper.delete(grade);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        }
    }

    public Set<Grade> findAllGrades() {
        return gradeMapper.findAll();
    }

    public Grade findGradeById(int id) {
        return gradeMapper.findById(id);
    }
}
