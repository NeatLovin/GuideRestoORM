package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Localisation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.CityMapper;
import ch.hearc.ig.guideresto.persistence.RestaurantMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class RestaurantService {
    private final EntityManager em;
    private final CityMapper cityMapper;
    private final RestaurantMapper restaurantMapper;

    public RestaurantService(EntityManager em, CityMapper cityMapper, RestaurantMapper restaurantMapper) {
        this.em = em;
        this.cityMapper = cityMapper;
        this.restaurantMapper = restaurantMapper;
    }

    /**
     * Crée un restaurant avec sa localisation et sa ville (si la ville n'existe pas déjà).
     * Toute l'opération est transactionnelle.
     */
    public Restaurant createRestaurant(Restaurant restaurant, Localisation localisation, City city) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Vérifier si la ville existe déjà (par NPA et nom)
            City existingCity = cityMapper.findAll().stream()
                .filter(c -> c.getZipCode().equals(city.getZipCode()) && c.getCityName().equalsIgnoreCase(city.getCityName()))
                .findFirst().orElse(null);
            if (existingCity == null) {
                cityMapper.create(city);
                existingCity = city;
            }
            localisation.setCity(existingCity);
            restaurant.setAddress(localisation);
            restaurantMapper.create(restaurant);
            tx.commit();
            return restaurant;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public java.util.Set<ch.hearc.ig.guideresto.business.Restaurant> findAllRestaurants() {
        return restaurantMapper.findAll();
    }

    public java.util.Set<ch.hearc.ig.guideresto.business.Restaurant> findRestaurantsByName(String name) {
        return restaurantMapper.findByName(name);
    }

    public java.util.Set<ch.hearc.ig.guideresto.business.Restaurant> findRestaurantsByCityName(String cityName) {
        return restaurantMapper.findByCityName(cityName);
    }

    public java.util.Set<ch.hearc.ig.guideresto.business.Restaurant> findRestaurantsByType(int typeId) {
        return restaurantMapper.findByType(typeId);
    }
}
