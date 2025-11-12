package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.IBusinessObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractMapper<T extends IBusinessObject> {

    protected static final Logger logger = LogManager.getLogger();

    // Cache simple en mémoire pour mapper les objets par id (scope: instance)
    protected final Map<Integer, T> cache = new HashMap<>();

    public abstract T findById(int id);
    public abstract Set<T> findAll();
    public abstract T create(T object);
    public abstract boolean update(T object);
    public abstract boolean delete(T object);
    public abstract boolean deleteById(int id);

    protected abstract String getSequenceQuery();
    protected abstract String getExistsQuery();
    protected abstract String getCountQuery();

    /**
     * Retourne la map d'identité partagée pour ce mapper (scope: thread courant)
     */
    @SuppressWarnings("unchecked")
    protected Map<Integer, T> identityMap() {
        return (Map<Integer, T>) IdentityMapContext.current().mapFor(this.getClass());
    }

    /**
     * Utilitaire: tente de retrouver l'objet dans l'Identity Map ou le cache local.
     * Retourne null si absent.
     */
    protected T findInCache(int id) {
        T obj = identityMap().get(id);
        if (obj != null) return obj;
        return cache.get(id);
    }

    /**
     * Vérifie si un objet avec l'ID donné existe.
     * @param id the ID to check
     * @return true si l'objet existe, false sinon
     */
    public boolean exists(int id) {
        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(getExistsQuery())) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Compte le nombre d'objets en base de données.
     * @return
     */
    public int count() {
        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(getCountQuery());
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
            return 0;
        }
    }

    /**
     * Obtient la valeur de la séquence actuelle en base de données
     * @return Le nombre de villes
     * @En cas d'erreur SQL
     */
    protected Integer getSequenceValue() {
        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(getSequenceQuery());
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
            return 0;
        }
    }

    /**
     * Vérifie si le cache est actuellement vide
     * @return true si le cache ne contient aucun objet, false sinon
     */
    protected boolean isCacheEmpty() {
        return cache.isEmpty() && identityMap().isEmpty();
    }

    /**
     * Vide le cache
     */
    protected void resetCache() {
        cache.clear();
        identityMap().clear();
    }

    /**
     * Ajoute un objet au cache et à l'identity map (garantit une seule instance par id)
     * @param objet l'objet à ajouter
     */
    protected void addToCache(T objet) {
        if (objet != null && objet.getId() != null) {
            cache.put(objet.getId(), objet);
            identityMap().put(objet.getId(), objet);
        }
    }

    /**
     * Retire un objet du cache et de l'identity map
     * @param id l'ID de l'objet à retirer du cache
     */
    protected void removeFromCache(Integer id) {
        if (id != null) {
            cache.remove(id);
            identityMap().remove(id);
        }
    }
}
