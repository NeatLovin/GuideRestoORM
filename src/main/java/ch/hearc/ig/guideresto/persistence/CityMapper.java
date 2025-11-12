package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

public class CityMapper extends AbstractMapper<City> {

    private static final String SELECT_BY_ID = "SELECT numero, code_postal, nom_ville FROM VILLES WHERE numero = ?";
    private static final String SELECT_ALL = "SELECT numero, code_postal, nom_ville FROM VILLES ORDER BY nom_ville";
    private static final String INSERT = "INSERT INTO VILLES (code_postal, nom_ville) VALUES (?, ?)";
    private static final String UPDATE = "UPDATE VILLES SET code_postal = ?, nom_ville = ? WHERE numero = ?";
    private static final String DELETE = "DELETE FROM VILLES WHERE numero = ?";

    private static final String EXISTS_QUERY = "SELECT 1 FROM VILLES WHERE numero = ?";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM VILLES";
    private static final String SEQUENCE_QUERY = "SELECT SEQ_VILLES.CURRVAL FROM DUAL";

    @Override
    protected String getSequenceQuery() { return SEQUENCE_QUERY; }
    @Override
    protected String getExistsQuery() { return EXISTS_QUERY; }
    @Override
    protected String getCountQuery() { return COUNT_QUERY; }

    @Override
    public City findById(int id) {
        City cached = findInCache(id);
        if (cached != null) return cached;

        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    City city = mapRow(rs);
                    addToCache(city);
                    return city;
                }
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return null;
    }

    @Override
    public Set<City> findAll() {
        if (!identityMap().isEmpty()) {
            return new LinkedHashSet<>(identityMap().values());
        }
        if (!cache.isEmpty()) {
            return new LinkedHashSet<>(cache.values());
        }

        Set<City> result = new LinkedHashSet<>();
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                City city = mapRow(rs);
                result.add(city);
                addToCache(city);
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return result;
    }

    @Override
    public City create(City object) {
        if (object == null) return null;

        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
            stmt.setString(1, object.getZipCode());
            stmt.setString(2, object.getCityName());

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
    public boolean update(City object) {
        if (object == null || object.getId() == null) return false;

        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
            stmt.setString(1, object.getZipCode());
            stmt.setString(2, object.getCityName());
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
    public boolean delete(City object) {
        if (object == null || object.getId() == null) return false;
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

    public Set<City> findByZipCode(String zipCode) {
        Set<City> result = new LinkedHashSet<>();
        if (zipCode == null) return result;

        Connection connection = ConnectionUtils.getConnection();
        String sql = "SELECT numero, code_postal, nom_ville FROM VILLES WHERE code_postal = ? ORDER BY nom_ville";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, zipCode);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    City city = mapRow(rs);
                    result.add(city);
                    addToCache(city);
                }
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return result;
    }

    public Set<City> findByName(String namePart) {
        Set<City> result = new LinkedHashSet<>();
        if (namePart == null) return result;

        Connection connection = ConnectionUtils.getConnection();
        String sql = "SELECT numero, code_postal, nom_ville FROM VILLES WHERE UPPER(nom_ville) LIKE ? ORDER BY nom_ville";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + namePart.toUpperCase() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    City city = mapRow(rs);
                    result.add(city);
                    addToCache(city);
                }
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return result;
    }

    private City mapRow(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("numero");
        String zip = rs.getString("code_postal");
        String name = rs.getString("nom_ville");
        return new City(id, zip, name);
    }
}
