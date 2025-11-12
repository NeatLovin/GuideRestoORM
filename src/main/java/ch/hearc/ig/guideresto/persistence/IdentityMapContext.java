package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.IBusinessObject;

import java.util.HashMap;
import java.util.Map;

/**
 * IdentityMapContext conserve, par thread (scope par requête/transaction),
 * une Identity Map par classe de mapper. Chaque map associe id → instance unique.
 */
public final class IdentityMapContext {

    private static final ThreadLocal<IdentityMapContext> CURRENT = ThreadLocal.withInitial(IdentityMapContext::new);

    // key: Mapper class (e.g., CityMapper.class), value: (id -> entity)
    private final Map<Class<?>, Map<Integer, IBusinessObject>> maps = new HashMap<>();

    private IdentityMapContext() { }

    public static IdentityMapContext current() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }

    @SuppressWarnings("unchecked")
    public <T extends IBusinessObject> Map<Integer, T> mapFor(Class<?> mapperClass) {
        return (Map<Integer, T>) maps.computeIfAbsent(mapperClass, k -> new HashMap<>());
    }
}

