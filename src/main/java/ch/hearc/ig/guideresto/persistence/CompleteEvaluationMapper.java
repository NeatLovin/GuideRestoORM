package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Grade;
import ch.hearc.ig.guideresto.business.Restaurant;

import java.sql.*;
import java.util.LinkedHashSet;
import java.util.Set;

public class CompleteEvaluationMapper extends AbstractMapper<CompleteEvaluation> {

    // Requêtes pour COMMENTAIRES
    private static final String SELECT_BY_ID = "SELECT numero, date_eval, commentaire, nom_utilisateur, fk_rest FROM COMMENTAIRES WHERE numero = ?";
    private static final String SELECT_ALL = "SELECT numero, date_eval, commentaire, nom_utilisateur, fk_rest FROM COMMENTAIRES ORDER BY numero";
    private static final String INSERT = "INSERT INTO COMMENTAIRES (date_eval, commentaire, nom_utilisateur, fk_rest) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE COMMENTAIRES SET date_eval = ?, commentaire = ?, nom_utilisateur = ?, fk_rest = ? WHERE numero = ?";
    private static final String DELETE = "DELETE FROM COMMENTAIRES WHERE numero = ?";

    // Métadonnées
    private static final String EXISTS_QUERY = "SELECT 1 FROM COMMENTAIRES WHERE numero = ?";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM COMMENTAIRES";
    private static final String SEQUENCE_QUERY = "SELECT SEQ_EVAL.CURRVAL FROM DUAL";

    private final GradeMapper gradeMapper = new GradeMapper();

    @Override
    protected String getSequenceQuery() { return SEQUENCE_QUERY; }

    @Override
    protected String getExistsQuery() { return EXISTS_QUERY; }

    @Override
    protected String getCountQuery() { return COUNT_QUERY; }

    @Override
    public CompleteEvaluation findById(int id) {
        CompleteEvaluation cached = findInCache(id);
        if (cached != null) return cached;
        Connection conn = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CompleteEvaluation evaluation = mapRow(rs);
                    // Charger les notes
                    evaluation.setGrades(gradeMapper.findByEvaluationId(evaluation.getId()));
                    addToCache(evaluation);
                    return evaluation;
                }
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return null;
    }

    @Override
    public Set<CompleteEvaluation> findAll() {
        if (!identityMap().isEmpty()) {
            return new LinkedHashSet<>(identityMap().values());
        }
        if (!cache.isEmpty()) {
            return new LinkedHashSet<>(cache.values());
        }
        Set<CompleteEvaluation> result = new LinkedHashSet<>();
        Connection conn = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                CompleteEvaluation evaluation = mapRow(rs);
                // Charger les notes pour chaque évaluation
                evaluation.setGrades(gradeMapper.findByEvaluationId(evaluation.getId()));
                result.add(evaluation);
                addToCache(evaluation);
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return result;
    }

    @Override
    public CompleteEvaluation create(CompleteEvaluation evaluation) {
        if (evaluation == null) return null;
        if (evaluation.getRestaurant() == null || evaluation.getRestaurant().getId() == null) {
            logger.error("CompleteEvaluation.create: restaurant must be provided");
            return null;
        }
        Connection conn = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(INSERT)) {
            stmt.setDate(1, new java.sql.Date(evaluation.getVisitDate().getTime()));
            stmt.setString(2, evaluation.getComment());
            stmt.setString(3, evaluation.getUsername());
            stmt.setInt(4, evaluation.getRestaurant().getId());

            stmt.executeUpdate();
            Integer id = getSequenceValue();
            if (id != null && id > 0) {
                evaluation.setId(id);
            }

            // Insérer les notes via GradeMapper dans la même transaction
            if (evaluation.getGrades() != null) {
                for (Grade grade : evaluation.getGrades()) {
                    grade.setEvaluation(evaluation);
                    gradeMapper.createWithConnection(grade, conn);
                }
            }

            addToCache(evaluation);
            return evaluation;
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return null;
    }

    @Override
    public boolean update(CompleteEvaluation evaluation) {
        if (evaluation == null || evaluation.getId() == null) return false;
        if (evaluation.getRestaurant() == null || evaluation.getRestaurant().getId() == null) {
            logger.error("CompleteEvaluation.update: restaurant must be provided");
            return false;
        }
        Connection conn = ConnectionUtils.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE)) {
            stmt.setDate(1, new java.sql.Date(evaluation.getVisitDate().getTime()));
            stmt.setString(2, evaluation.getComment());
            stmt.setString(3, evaluation.getUsername());
            stmt.setInt(4, evaluation.getRestaurant().getId());
            stmt.setInt(5, evaluation.getId());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                addToCache(evaluation);
                return true;
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(CompleteEvaluation evaluation) {
        if (evaluation == null || evaluation.getId() == null) return false;
        return deleteById(evaluation.getId());
    }

    @Override
    public boolean deleteById(int id) {
        Connection conn = ConnectionUtils.getConnection();
        try {
            // Supprimer d'abord les notes associées
            try (PreparedStatement stmtNotes = conn.prepareStatement("DELETE FROM NOTES WHERE fk_comm = ?")) {
                stmtNotes.setInt(1, id);
                stmtNotes.executeUpdate();
            }
            // Supprimer l'évaluation
            try (PreparedStatement stmt = conn.prepareStatement(DELETE)) {
                stmt.setInt(1, id);
                int affected = stmt.executeUpdate();
                if (affected > 0) {
                    removeFromCache(id);
                    return true;
                }
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return false;
    }

    // Finders additionnels
    public Set<CompleteEvaluation> findByRestaurantId(int restaurantId) {
        Set<CompleteEvaluation> result = new LinkedHashSet<>();
        Connection conn = ConnectionUtils.getConnection();
        String sql = "SELECT numero, date_eval, commentaire, nom_utilisateur, fk_rest FROM COMMENTAIRES WHERE fk_rest = ? ORDER BY numero";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, restaurantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CompleteEvaluation evaluation = mapRow(rs);
                    evaluation.setGrades(gradeMapper.findByEvaluationId(evaluation.getId()));
                    addToCache(evaluation);
                    result.add(evaluation);
                }
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return result;
    }

    public Set<CompleteEvaluation> findByUsername(String username) {
        Set<CompleteEvaluation> result = new LinkedHashSet<>();
        if (username == null) return result;
        Connection conn = ConnectionUtils.getConnection();
        String sql = "SELECT numero, date_eval, commentaire, nom_utilisateur, fk_rest FROM COMMENTAIRES WHERE UPPER(nom_utilisateur) = ? ORDER BY numero";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CompleteEvaluation evaluation = mapRow(rs);
                    evaluation.setGrades(gradeMapper.findByEvaluationId(evaluation.getId()));
                    addToCache(evaluation);
                    result.add(evaluation);
                }
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return result;
    }

    // Mapping simple d'une ligne COMMENTAIRES
    private CompleteEvaluation mapRow(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("numero");
        java.util.Date date = rs.getDate("date_eval");
        String comment = rs.getString("commentaire");
        String username = rs.getString("nom_utilisateur");
        // FK restaurant
        Integer restId = rs.getInt("fk_rest");
        // On ne charge pas le Restaurant ici pour éviter des cycles/charges; on peut l’enrichir ailleurs si souhaité
        Restaurant restaurant = null; // ou charger via RestaurantMapper si nécessaire
        return new CompleteEvaluation(id, date, restaurant, comment, username);
    }
}
