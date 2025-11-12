package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.RestaurantType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

public class RestaurantTypeMapper extends AbstractMapper<RestaurantType> {
    private static final String SELECT_BY_ID = "SELECT numero, libelle, description FROM TYPES_GASTRONOMIQUES WHERE numero = ?";
    private static final String SELECT_ALL = "SELECT numero, libelle, description FROM TYPES_GASTRONOMIQUES ORDER BY libelle";
    private static final String INSERT = "INSERT INTO TYPES_GASTRONOMIQUES (libelle, description) VALUES (?, ?)";
    private static final String UPDATE = "UPDATE TYPES_GASTRONOMIQUES SET libelle = ?, description = ? WHERE numero = ?";
    private static final String DELETE = "DELETE FROM TYPES_GASTRONOMIQUES WHERE numero = ?";

    private static final String EXISTS_QUERY = "SELECT 1 FROM TYPES_GASTRONOMIQUES WHERE numero = ?";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM TYPES_GASTRONOMIQUES";
    private static final String SEQUENCE_QUERY = "SELECT SEQ_TYPES_GASTRONOMIQUES.CURRVAL FROM DUAL";


    @Override
    public RestaurantType findById(int id) {
        RestaurantType cached = findInCache(id);
        if (cached != null) return cached;

        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    RestaurantType type = mapRow(rs);
                    addToCache(type);
                    return type;
                }
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return null;
    }


    @Override
    public Set<RestaurantType> findAll() {
        if (!identityMap().isEmpty()) {
            return new LinkedHashSet<>(identityMap().values());
        }
        if (!cache.isEmpty()) {
            return new LinkedHashSet<>(cache.values());
        }

        Set<RestaurantType> result = new LinkedHashSet<>();
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                RestaurantType type = mapRow(rs);
                result.add(type);
                addToCache(type);
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return result;
    }

    private RestaurantType mapRow(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("numero");
        String libelle = rs.getString("libelle");
        String description = rs.getString("description");

        return new RestaurantType(id, libelle, description);
    }

    @Override
    public RestaurantType create(RestaurantType object) {
        if (object == null) {
            return null;
        }

        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
            stmt.setString(1, object.getLabel());
            stmt.setString(2, object.getDescription());

            stmt.executeUpdate();
            Integer id = getSequenceValue();
            if (id != null && id > 0) {
                object.setId(id);
                addToCache(object);
            }

            return object;
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return null;
    }

    @Override
    public boolean update(RestaurantType object) {
        if (object == null || object.getId() == null) {
            return false;
        }

        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
            stmt.setString(1, object.getLabel());
            stmt.setString(2, object.getDescription());
            stmt.setInt(3, object.getId());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                addToCache(object);
                return true;
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(RestaurantType object) {
        if (object == null || object.getId() == null) {
            return false;
        }
        return deleteById(object.getId());
    }

    @Override
    public boolean deleteById(int id) {
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(DELETE)) {
            stmt.setInt(1, id);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                removeFromCache(id);
                return true;
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return false;
    }


    @Override
    protected String getSequenceQuery() {
        return SEQUENCE_QUERY;
    }

    @Override
    protected String getExistsQuery() {
        return EXISTS_QUERY;
    }

    @Override
    protected String getCountQuery() {
        return COUNT_QUERY;
    }
}
