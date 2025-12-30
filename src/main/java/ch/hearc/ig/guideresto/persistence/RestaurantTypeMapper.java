package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.RestaurantType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

public class RestaurantTypeMapper extends AbstractMapper<RestaurantType> {
    private final EntityManager em;

    public RestaurantTypeMapper(EntityManager em) {
        this.em = em;
    }



    @Override
    public RestaurantType findById(int id) {
        RestaurantType cached = findInCache(id);
        if (cached != null) return cached;
        RestaurantType type = em.find(RestaurantType.class, id);
        if (type != null) {
            addToCache(type);
        }
        return type;
    }


    @Override
    public Set<RestaurantType> findAll() {
        if (!identityMap().isEmpty()) {
            return new LinkedHashSet<>(identityMap().values());
        }
        if (!cache.isEmpty()) {
            return new LinkedHashSet<>(cache.values());
        }
        TypedQuery<RestaurantType> query = em.createNamedQuery("RestaurantType.findAll", RestaurantType.class);
        List<RestaurantType> resultList = query.getResultList();
        Set<RestaurantType> result = new LinkedHashSet<>(resultList);
        for (RestaurantType type : result) {
            addToCache(type);
        }
        return result;
    }
    public Set<RestaurantType> findByName(String namePart) {
        if (namePart == null) return new LinkedHashSet<>();
        TypedQuery<RestaurantType> query = em.createNamedQuery("RestaurantType.findByName", RestaurantType.class);
        query.setParameter("name", "%" + namePart.toUpperCase() + "%");
        List<RestaurantType> resultList = query.getResultList();
        Set<RestaurantType> result = new LinkedHashSet<>(resultList);
        for (RestaurantType type : result) {
            addToCache(type);
        }
        return result;
    }



    @Override
    public RestaurantType create(RestaurantType object) {
        if (object == null) return null;
        em.persist(object);
        addToCache(object);
        return object;
    }

    @Override
    public boolean update(RestaurantType object) {
        if (object == null || object.getId() == null) return false;
        em.merge(object);
        addToCache(object);
        return true;
    }

    @Override
    public boolean delete(RestaurantType object) {
        if (object == null || object.getId() == null) return false;
        RestaurantType managed = em.contains(object) ? object : em.merge(object);
        em.remove(managed);
        removeFromCache(object.getId());
        return true;
    }

    @Override
    public boolean deleteById(int id) {
        RestaurantType type = findById(id);
        if (type != null) {
            return delete(type);
        }
        return false;
    }


    // Les méthodes getSequenceQuery, getExistsQuery, getCountQuery ne sont plus nécessaires avec JPA
    @Override
    protected String getSequenceQuery() { return null; }
    @Override
    protected String getExistsQuery() { return null; }
    @Override
    protected String getCountQuery() { return null; }
}
