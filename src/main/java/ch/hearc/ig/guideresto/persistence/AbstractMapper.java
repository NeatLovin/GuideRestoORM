package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.IBusinessObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractMapper<T extends IBusinessObject> {

    // Cache simple en mémoire pour mapper les objets par id (scope: instance)
    protected final Map<Integer, T> cache = new HashMap<>();

    public abstract T findById(int id);

    public abstract Set<T> findAll();

    public abstract T create(T object);

    public abstract boolean update(T object);

    public abstract boolean delete(T object);

    public abstract boolean deleteById(int id);

    /**
     * Retourne la map d'identité partagée pour ce mapper (scope: thread courant)
     */
    protected Map<Integer, T> identityMap() {
        return IdentityMapContext.current().mapFor(this.getClass());
    }

    /**
     * Utilitaire: tente de retrouver l'objet dans l'Identity Map ou le cache local.
     * Retourne null si absent.
     */
    protected T findInCache(int id) {
        T obj = identityMap().get(id);
        if (obj != null)
            return obj;
        return cache.get(id);
    }

    /**
     * Vérifie si le cache est actuellement vide
     * 
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
     * Ajoute un objet au cache et à l'identity map (garantit une seule instance par
     * id)
     * 
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
     * 
     * @param id l'ID de l'objet à retirer du cache
     */
    protected void removeFromCache(Integer id) {
        if (id != null) {
            cache.remove(id);
            identityMap().remove(id);
        }
    }
}
