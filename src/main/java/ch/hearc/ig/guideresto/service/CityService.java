package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.persistence.CityMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.Set;

/**
 * Service applicatif pour gérer les {@link City}.
 * Encapsule les écritures dans une transaction JPA (begin/commit/rollback).
 * Les lectures délèguent au {@link CityMapper}.
 */
public class CityService {
    private final EntityManager em;
    private final CityMapper cityMapper;

    public CityService(EntityManager em, CityMapper cityMapper) {
        this.em = em;
        this.cityMapper = cityMapper;
    }

    public City createCity(City city) {
        EntityTransaction tx = em.getTransaction();
        boolean startedTx = false;
        try {
            if (!tx.isActive()) {
                tx.begin();
                startedTx = true;
            }
            cityMapper.create(city);
            if (startedTx) {
                tx.commit();
            }
            return city;
        } catch (Exception e) {
            if (startedTx && tx.isActive())
                tx.rollback();
            throw e;
        }
    }

    public City updateCity(City city) {
        EntityTransaction tx = em.getTransaction();
        boolean startedTx = false;
        try {
            if (!tx.isActive()) {
                tx.begin();
                startedTx = true;
            }
            cityMapper.update(city);
            if (startedTx) {
                tx.commit();
            }
            return city;
        } catch (Exception e) {
            if (startedTx && tx.isActive())
                tx.rollback();
            throw e;
        }
    }

    public boolean deleteCity(City city) {
        EntityTransaction tx = em.getTransaction();
        boolean startedTx = false;
        try {
            if (!tx.isActive()) {
                tx.begin();
                startedTx = true;
            }
            boolean result = cityMapper.delete(city);
            if (startedTx) {
                tx.commit();
            }
            return result;
        } catch (Exception e) {
            if (startedTx && tx.isActive())
                tx.rollback();
            throw e;
        }
    }

    public Set<City> findAllCities() {
        return cityMapper.findAll();
    }

    public City findCityById(int id) {
        return cityMapper.findById(id);
    }
}
