package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.IBusinessObject;
import java.util.Map;
import java.util.Set;

public abstract class AbstractMapper<T extends IBusinessObject> {

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
     * Utilitaire: tente de retrouver l'objet dans l'Identity Map.
     * Retourne null si absent.
     */
    protected T findInCache(int id) {
        return identityMap().get(id);
    }

    /**
     * Ajoute un objet à l'identity map (garantit une seule instance par id)
     * 
     * @param objet l'objet à ajouter
     */
    protected void addToCache(T objet) {
        if (objet != null && objet.getId() != null) {
            identityMap().put(objet.getId(), objet);
        }
    }

    /**
     * Retire un objet de l'identity map
     * 
     * @param id l'ID de l'objet à retirer
     */
    protected void removeFromCache(Integer id) {
        if (id != null) {
            identityMap().remove(id);
        }
    }
}
