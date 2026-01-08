package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RestaurantMapper extends AbstractMapper<Restaurant> {
    private final EntityManager em;

    public RestaurantMapper(EntityManager em) {
        this.em = em;
    }

    @Override
    public Restaurant findById(int id) {
        Restaurant inCache = findInCache(id);
        if (inCache != null)
            return inCache;
        Restaurant restaurant = em.find(Restaurant.class, id);
        if (restaurant != null) {
            addToCache(restaurant);
        }
        return restaurant;
    }

    @Override
    public Set<Restaurant> findAll() {
        TypedQuery<Restaurant> query = em.createNamedQuery("Restaurant.findAll", Restaurant.class);
        List<Restaurant> resultList = query.getResultList();
        Set<Restaurant> restaurants = new LinkedHashSet<>(resultList);
        for (Restaurant restaurant : restaurants) {
            addToCache(restaurant);
        }
        return restaurants;
    }

    @Override
    public Restaurant create(Restaurant restaurant) {
        if (restaurant == null)
            return null;
        em.persist(restaurant);
        addToCache(restaurant);
        return restaurant;
    }

    @Override
    public boolean update(Restaurant restaurant) {
        if (restaurant == null || restaurant.getId() == null)
            return false;
        em.merge(restaurant);
        addToCache(restaurant);
        return true;
    }

    @Override
    public boolean delete(Restaurant restaurant) {
        if (restaurant == null || restaurant.getId() == null)
            return false;
        Restaurant managed = em.contains(restaurant) ? restaurant : em.merge(restaurant);
        em.remove(managed);
        removeFromCache(restaurant.getId());
        return true;
    }

    @Override
    public boolean deleteById(int id) {
        Restaurant restaurant = findById(id);
        if (restaurant != null) {
            return delete(restaurant);
        }
        return false;
    }

    public Set<Restaurant> findByName(String name) {
        if (name == null)
            return new LinkedHashSet<>();
        TypedQuery<Restaurant> query = em.createNamedQuery("Restaurant.findByName", Restaurant.class);
        query.setParameter("name", "%" + name + "%");
        List<Restaurant> resultList = query.getResultList();
        Set<Restaurant> restaurants = new LinkedHashSet<>(resultList);
        for (Restaurant restaurant : restaurants) {
            addToCache(restaurant);
        }
        return restaurants;
    }

    public Set<Restaurant> findByCityName(String cityName) {
        if (cityName == null)
            return new LinkedHashSet<>();
        TypedQuery<Restaurant> query = em.createNamedQuery("Restaurant.findByCity", Restaurant.class);
        query.setParameter("cityName", "%" + cityName + "%");
        List<Restaurant> resultList = query.getResultList();
        Set<Restaurant> restaurants = new LinkedHashSet<>(resultList);
        for (Restaurant restaurant : restaurants) {
            addToCache(restaurant);
        }
        return restaurants;
    }

    public Set<Restaurant> findByType(int typeId) {
        TypedQuery<Restaurant> query = em
                .createQuery("SELECT r FROM Restaurant r WHERE r.type.id = :typeId ORDER BY r.name", Restaurant.class);
        query.setParameter("typeId", typeId);
        List<Restaurant> resultList = query.getResultList();
        Set<Restaurant> restaurants = new LinkedHashSet<>(resultList);
        for (Restaurant restaurant : restaurants) {
            addToCache(restaurant);
        }
        return restaurants;
    }

}
