package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Data Mapper JPA pour {@link City}.
 * Maintient une Identity Map (thread-local) afin de garantir une instance par
 * id.
 * Expose des recherches par zip/nom basées sur des NamedQueries.
 */
public class CityMapper extends AbstractMapper<City> {
    private final EntityManager em;

    public CityMapper(EntityManager em) {
        this.em = em;
    }

    @Override
    public City findById(int id) {
        City cached = findInCache(id);
        if (cached != null)
            return cached;
        City city = em.find(City.class, id);
        if (city != null) {
            addToCache(city);
        }
        return city;
    }

    @Override
    public Set<City> findAll() {
        TypedQuery<City> query = em.createNamedQuery("City.findAll", City.class);
        List<City> resultList = query.getResultList();
        Set<City> result = new LinkedHashSet<>(resultList);
        for (City city : result) {
            addToCache(city);
        }
        return result;
    }

    @Override
    public City create(City object) {
        if (object == null)
            return null;
        em.persist(object);
        addToCache(object);
        return object;
    }

    @Override
    public boolean update(City object) {
        if (object == null || object.getId() == null)
            return false;
        City managed = em.merge(object);
        addToCache(managed);
        return true;
    }

    @Override
    public boolean delete(City object) {
        if (object == null || object.getId() == null)
            return false;
        City managed = em.contains(object) ? object : em.merge(object);
        em.remove(managed);
        removeFromCache(object.getId());
        return true;
    }

    @Override
    public boolean deleteById(int id) {
        City city = findById(id);
        if (city != null) {
            return delete(city);
        }
        return false;
    }

    /**
     * Recherche des villes par NPA via une NamedQuery.
     */
    public Set<City> findByZipCode(String zipCode) {
        if (zipCode == null)
            return new LinkedHashSet<>();
        TypedQuery<City> query = em.createNamedQuery("City.findByZipCode", City.class);
        query.setParameter("zip", zipCode);
        List<City> resultList = query.getResultList();
        Set<City> result = new LinkedHashSet<>(resultList);
        for (City city : result) {
            addToCache(city);
        }
        return result;
    }

    /**
     * Recherche des villes dont le nom contient la chaîne donnée (case-insensitive
     * côté requête).
     */
    public Set<City> findByName(String namePart) {
        if (namePart == null)
            return new LinkedHashSet<>();
        TypedQuery<City> query = em.createNamedQuery("City.findByName", City.class);
        query.setParameter("name", "%" + namePart.toUpperCase() + "%");
        List<City> resultList = query.getResultList();
        Set<City> result = new LinkedHashSet<>(resultList);
        for (City city : result) {
            addToCache(city);
        }
        return result;
    }
}
