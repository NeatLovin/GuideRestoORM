package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.RestaurantTypeMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.Set;

/**
 * Service applicatif pour gérer les {@link RestaurantType}.
 * Encapsule les écritures dans une transaction JPA (begin/commit/rollback).
 * Les lectures délèguent au {@link RestaurantTypeMapper}.
 */
public class RestaurantTypeService {
    private final EntityManager em;
    private final RestaurantTypeMapper typeMapper;

    public RestaurantTypeService(EntityManager em, RestaurantTypeMapper typeMapper) {
        this.em = em;
        this.typeMapper = typeMapper;
    }

    public RestaurantType createType(RestaurantType type) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            typeMapper.create(type);
            tx.commit();
            return type;
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        }
    }

    public RestaurantType updateType(RestaurantType type) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            typeMapper.update(type);
            tx.commit();
            return type;
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        }
    }

    public boolean deleteType(RestaurantType type) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            boolean result = typeMapper.delete(type);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        }
    }

    public Set<RestaurantType> findAllTypes() {
        return typeMapper.findAll();
    }

    public RestaurantType findTypeById(int id) {
        return typeMapper.findById(id);
    }
}
