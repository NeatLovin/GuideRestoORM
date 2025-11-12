package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.Grade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

public class GradeMapper extends AbstractMapper<Grade> {

    // Requêtes SQL
    private static final String SELECT_BY_ID = "SELECT numero, note, fk_comm, fk_crit FROM NOTES WHERE numero = ?";
    private static final String SELECT_ALL = "SELECT numero, note, fk_comm, fk_crit FROM NOTES ORDER BY numero";
    private static final String INSERT = "INSERT INTO NOTES (note, fk_comm, fk_crit) VALUES (?, ?, ?)";
    private static final String UPDATE = "UPDATE NOTES SET note = ?, fk_comm = ?, fk_crit = ? WHERE numero = ?";
    private static final String DELETE = "DELETE FROM NOTES WHERE numero = ?";

    // Métadonnées (existence, count, séquence courante)
    private static final String EXISTS_QUERY = "SELECT 1 FROM NOTES WHERE numero = ?";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM NOTES";
    private static final String SEQUENCE_QUERY = "SELECT SEQ_NOTES.CURRVAL FROM DUAL";

    @Override
    protected String getSequenceQuery() { return SEQUENCE_QUERY; }

    @Override
    protected String getExistsQuery() { return EXISTS_QUERY; }

    @Override
    protected String getCountQuery() { return COUNT_QUERY; }

    // CRUD de base
    @Override
    public Grade findById(int id) {
        Grade cached = findInCache(id);
        if (cached != null) return cached;
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Grade grade = mapRow(rs);
                    addToCache(grade);
                    return grade;
                }
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return null;
    }

    @Override
    public Set<Grade> findAll() {
        if (!identityMap().isEmpty()) {
            return new LinkedHashSet<>(identityMap().values());
        }
        if (!cache.isEmpty()) {
            return new LinkedHashSet<>(cache.values());
        }
        Set<Grade> result = new LinkedHashSet<>();
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Grade grade = mapRow(rs);
                result.add(grade);
                addToCache(grade);
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return result;
    }

    @Override
    public Grade create(Grade object) {
        Connection connection = ConnectionUtils.getConnection();
        return createWithConnection(object, connection);
    }

    public Grade createWithConnection(Grade object, Connection connection) {
        if (object == null) return null;
        if (object.getGrade() == null) {
            logger.error("Grade.create: 'note' cannot be null");
            return null;
        }
        Integer evalId = object.getEvaluation() != null ? object.getEvaluation().getId() : null;
        Integer critId = object.getCriteria() != null ? object.getCriteria().getId() : null;
        if (evalId == null || critId == null) {
            logger.error("Grade.create: 'fk_comm' and 'fk_crit' must be provided");
            return null;
        }

        try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
            stmt.setInt(1, object.getGrade());
            stmt.setInt(2, evalId);
            stmt.setInt(3, critId);

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
    public boolean update(Grade object) {
        if (object == null || object.getId() == null) return false;
        if (object.getGrade() == null) {
            logger.error("Grade.update: 'note' cannot be null");
            return false;
        }
        Integer evalId = object.getEvaluation() != null ? object.getEvaluation().getId() : null;
        Integer critId = object.getCriteria() != null ? object.getCriteria().getId() : null;
        if (evalId == null || critId == null) {
            logger.error("Grade.update: 'fk_comm' and 'fk_crit' must be provided");
            return false;
        }
        Connection connection = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
            stmt.setInt(1, object.getGrade());
            stmt.setInt(2, evalId);
            stmt.setInt(3, critId);
            stmt.setInt(4, object.getId());

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
    public boolean delete(Grade object) {
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

    // Méthodes de recherche utiles
    public Set<Grade> findByEvaluationId(int evaluationId) {
        Set<Grade> result = new LinkedHashSet<>();
        Connection connection = ConnectionUtils.getConnection();
        String sql = "SELECT numero, note, fk_comm, fk_crit FROM NOTES WHERE fk_comm = ? ORDER BY numero";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, evaluationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Grade grade = mapRow(rs);
                    result.add(grade);
                    addToCache(grade);
                }
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return result;
    }

    public Set<Grade> findByEvaluation(CompleteEvaluation evaluation) {
        if (evaluation == null || evaluation.getId() == null) {
            return new LinkedHashSet<>();
        }
        return findByEvaluationId(evaluation.getId());
    }

    public Set<Grade> findByCriteriaId(int criteriaId) {
        Set<Grade> result = new LinkedHashSet<>();
        Connection connection = ConnectionUtils.getConnection();
        String sql = "SELECT numero, note, fk_comm, fk_crit FROM NOTES WHERE fk_crit = ? ORDER BY numero";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, criteriaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Grade grade = mapRow(rs);
                    result.add(grade);
                    addToCache(grade);
                }
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return result;
    }

    public Set<Grade> findByCriteria(EvaluationCriteria criteria) {
        if (criteria == null || criteria.getId() == null) {
            return new LinkedHashSet<>();
        }
        return findByCriteriaId(criteria.getId());
    }

    public Grade findOneByEvaluationAndCriteria(int evaluationId, int criteriaId) {
        Connection connection = ConnectionUtils.getConnection();
        String sql = "SELECT numero, note, fk_comm, fk_crit FROM NOTES WHERE fk_comm = ? AND fk_crit = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, evaluationId);
            stmt.setInt(2, criteriaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Grade grade = mapRow(rs);
                    addToCache(grade);
                    return grade;
                }
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return null;
    }

    // Mapping d'une ligne vers l'objet métier
    private Grade mapRow(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("numero");
        Integer note = rs.getInt("note");
        int evalId = rs.getInt("fk_comm");
        int critId = rs.getInt("fk_crit");

        // Colonnes NOT NULL -> création directe d'objets légers (id uniquement)
        CompleteEvaluation eval = new CompleteEvaluation(evalId, null, null, null, null);
        EvaluationCriteria crit = new EvaluationCriteria(critId, null, null);

        return new Grade(id, note, eval, crit);
    }
}
