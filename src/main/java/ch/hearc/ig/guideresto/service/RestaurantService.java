package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Localisation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.CityMapper;
import ch.hearc.ig.guideresto.persistence.RestaurantMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PessimisticLockException;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PersistenceException;
import java.util.HashMap;

/**
 * Service applicatif pour les {@link Restaurant}.
 * Gère la création transactionnelle et protège les modifications/suppressions
 * par verrou pessimiste (fail-fast).
 * Les lectures délèguent au {@link RestaurantMapper}.
 */
public class RestaurantService {
    private final EntityManager em;
    private final CityMapper cityMapper;
    private final RestaurantMapper restaurantMapper;

    /**
     * Petite “session” d'édition qui garde une transaction ouverte et un verrou
     * pessimiste actif
     * pendant que l'utilisateur saisit ses modifications.
     */
    public static final class RestaurantEditSession {
        private final EntityManager em;
        private final EntityTransaction tx;
        private final Restaurant locked;
        private boolean closed;

        private RestaurantEditSession(EntityManager em, EntityTransaction tx, Restaurant locked) {
            this.em = em;
            this.tx = tx;
            this.locked = locked;
        }

        public Restaurant getLockedRestaurant() {
            return locked;
        }

        /**
         * Valide les modifications et libère le verrou; idempotent.
         */
        public void commit() {
            if (closed)
                return;
            try {
                em.flush();
                tx.commit();
            } finally {
                closed = true;
            }
        }

        /**
         * Annule la transaction, nettoie le contexte et libère le verrou; idempotent.
         */
        public void rollback() {
            if (closed)
                return;
            try {
                if (tx.isActive())
                    tx.rollback();
            } finally {
                em.clear();
                closed = true;
            }
        }
    }

    public RestaurantService(EntityManager em, CityMapper cityMapper, RestaurantMapper restaurantMapper) {
        this.em = em;
        this.cityMapper = cityMapper;
        this.restaurantMapper = restaurantMapper;
    }

    /**
     * Crée un restaurant avec sa localisation et sa ville (si la ville n'existe pas
     * déjà).
     * Toute l'opération est transactionnelle.
     *
     * Grâce à la configuration des cascades dans Restaurant (@OneToMany cascade),
     * les évaluations associées seront gérées automatiquement par Hibernate.
     */
    public Restaurant createRestaurant(Restaurant restaurant, Localisation localisation, City city) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Vérifier si la ville existe déjà (par NPA et nom)
            City existingCity = cityMapper.findAll().stream()
                    .filter(c -> c.getZipCode().equals(city.getZipCode())
                            && c.getCityName().equalsIgnoreCase(city.getCityName()))
                    .findFirst().orElse(null);
            if (existingCity == null) {
                // Persister la nouvelle ville via JPA
                em.persist(city);
                em.flush();
                existingCity = city;
            }
            localisation.setCity(existingCity);
            restaurant.setAddress(localisation);
            // Persister le restaurant - les cascades JPA s'occuperont des évaluations
            em.persist(restaurant);
            em.flush();
            tx.commit();
            return restaurant;
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
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

    /**
     * Met à jour un restaurant sous verrou pessimiste (timeout 0) pour éviter les
     * éditions concurrentes.
     * Détache l'instance passée afin de forcer l'acquisition du verrou via
     * {@code em.find(..., PESSIMISTIC_WRITE)}.
     */
    public void updateRestaurant(Restaurant restaurant) {
        EntityTransaction tx = em.getTransaction();
        try {
            // IMPORTANT : Détacher l'entité du contexte de persistance
            // Sinon em.find() retournera l'instance déjà gérée SANS acquérir de verrou !
            if (em.contains(restaurant)) {
                em.detach(restaurant);
            }

            tx.begin();
            // Verrouillage pessimiste avec timeout fail-fast
            var props = new HashMap<String, Object>();
            props.put("jakarta.persistence.lock.timeout", 0);
            Restaurant locked = em.find(Restaurant.class, restaurant.getId(), LockModeType.PESSIMISTIC_WRITE, props);
            if (locked == null)
                throw new RuntimeException("Restaurant non trouvé pour modification.");
            // Appliquer les modifications sur l'entité verrouillée
            locked.setName(restaurant.getName());
            locked.setDescription(restaurant.getDescription());
            locked.setWebsite(restaurant.getWebsite());
            locked.setType(restaurant.getType());
            if (locked.getAddress() != null && restaurant.getAddress() != null) {
                locked.getAddress().setStreet(restaurant.getAddress().getStreet());
                locked.getAddress().setCity(restaurant.getAddress().getCity());
            }
            // em.flush() pour s'assurer que les cascades sont bien traitées
            em.flush();
            tx.commit();
        } catch (PessimisticLockException | LockTimeoutException e) {
            if (tx.isActive())
                tx.rollback();
            em.clear();
            throw new RuntimeException(
                    "Conflit de modification: ce restaurant est en cours de modification par un autre utilisateur. Rechargez la fiche et réessayez.",
                    e);
        } catch (PersistenceException e) {
            if (tx.isActive())
                tx.rollback();
            em.clear();
            throw new RuntimeException("Erreur de persistance lors de la modification du restaurant.", e);
        } catch (RuntimeException e) {
            if (tx.isActive())
                tx.rollback();
            em.clear();
            throw e;
        }
    }

    /**
     * Démarre une transaction et acquiert un verrou pessimiste sur le restaurant.
     * Le verrou reste actif tant que {@link RestaurantEditSession#commit()} ou
     * {@link RestaurantEditSession#rollback()} n'est pas appelé.
     */
    public RestaurantEditSession beginEditRestaurant(int restaurantId) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            var props = new HashMap<String, Object>();
            props.put("jakarta.persistence.lock.timeout", 0);

            Restaurant locked = em.find(Restaurant.class, restaurantId, LockModeType.PESSIMISTIC_WRITE, props);
            if (locked == null) {
                throw new RuntimeException("Restaurant non trouvé pour modification.");
            }

            return new RestaurantEditSession(em, tx, locked);
        } catch (PessimisticLockException | LockTimeoutException e) {
            if (tx.isActive())
                tx.rollback();
            em.clear();
            throw new RuntimeException(
                    "Conflit de modification: ce restaurant est en cours de modification par un autre utilisateur. Rechargez la fiche et réessayez.",
                    e);
        } catch (RuntimeException e) {
            if (tx.isActive())
                tx.rollback();
            em.clear();
            throw e;
        }
    }

    /**
     * Supprime un restaurant sous verrou pessimiste (timeout 0) afin de sérialiser
     * les suppressions.
     * Nettoie le contexte de persistance en cas d'erreur de verrou ou de
     * persistance.
     */
    public void deleteRestaurant(Restaurant restaurant) {
        EntityTransaction tx = em.getTransaction();
        try {
            // IMPORTANT : Détacher l'entité du contexte de persistance
            // Sinon em.find() retournera l'instance déjà gérée SANS acquérir de verrou !
            if (em.contains(restaurant)) {
                em.detach(restaurant);
            }

            tx.begin();
            var props = new HashMap<String, Object>();
            props.put("jakarta.persistence.lock.timeout", 0);
            Restaurant locked = em.find(Restaurant.class, restaurant.getId(), LockModeType.PESSIMISTIC_WRITE, props);
            if (locked == null)
                throw new RuntimeException("Restaurant non trouvé pour suppression.");
            // em.remove utilise les cascades pour supprimer aussi les évaluations associées
            em.remove(locked);
            em.flush();
            tx.commit();
        } catch (PessimisticLockException | LockTimeoutException e) {
            if (tx.isActive())
                tx.rollback();
            em.clear();
            throw new RuntimeException(
                    "Conflit de modification: ce restaurant est en cours de modification par un autre utilisateur. Rechargez la fiche et réessayez.",
                    e);
        } catch (PersistenceException e) {
            if (tx.isActive())
                tx.rollback();
            em.clear();
            throw new RuntimeException("Erreur de persistance lors de la suppression du restaurant.", e);
        } catch (RuntimeException e) {
            if (tx.isActive())
                tx.rollback();
            em.clear();
            throw e;
        }
    }
}
