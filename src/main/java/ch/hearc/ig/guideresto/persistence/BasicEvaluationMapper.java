package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.Restaurant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.LinkedHashSet;
import java.util.Set;

public class BasicEvaluationMapper extends AbstractMapper<BasicEvaluation> {
    private static final Logger logger = LogManager.getLogger(BasicEvaluationMapper.class);

    private static final String SELECT_BY_ID = "SELECT numero, appreciation, date_eval, adresse_ip, fk_rest FROM LIKES WHERE numero = ?";
    private static final String SELECT_ALL = "SELECT numero, appreciation, date_eval, adresse_ip, fk_rest FROM LIKES ORDER BY numero";
    private static final String INSERT = "INSERT INTO LIKES (appreciation, date_eval, adresse_ip, fk_rest) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE LIKES SET appreciation = ?, date_eval = ?, adresse_ip = ?, fk_rest = ? WHERE numero = ?";
    private static final String DELETE = "DELETE FROM LIKES WHERE numero = ?";

    private static final String EXISTS_QUERY = "SELECT 1 FROM LIKES WHERE numero = ?";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM LIKES";
    private static final String SEQUENCE_QUERY = "SELECT SEQ_EVAL.CURRVAL FROM DUAL";

    @Override
    protected String getSequenceQuery() { return SEQUENCE_QUERY; }
    @Override
    protected String getExistsQuery() { return EXISTS_QUERY; }
    @Override
    protected String getCountQuery() { return COUNT_QUERY; }

    @Override
    public BasicEvaluation findById(int id) {
        BasicEvaluation cached = findInCache(id);
        if (cached != null) return cached;
        try (Connection connection = ConnectionUtils.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BasicEvaluation be = mapRow(rs);
                    addToCache(be);
                    return be;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche du Like/Dislike: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public Set<BasicEvaluation> findAll() {
        if (!identityMap().isEmpty()) {
            return new LinkedHashSet<>(identityMap().values());
        }
        if (!cache.isEmpty()) {
            return new LinkedHashSet<>(cache.values());
        }
        Set<BasicEvaluation> res = new LinkedHashSet<>();
        try (Connection connection = ConnectionUtils.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                BasicEvaluation be = mapRow(rs);
                res.add(be);
                addToCache(be);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du chargement des Likes/Dislikes: {}", e.getMessage());
        }
        return res;
    }

    @Override
    public BasicEvaluation create(BasicEvaluation evaluation) {
        if (evaluation == null) return null;
        try (Connection connection = ConnectionUtils.getConnection();
             PreparedStatement stmt = connection.prepareStatement(INSERT, new String[]{"NUMERO"})) {

            stmt.setString(1, evaluation.getLikeRestaurant() ? "T" : "F");
            stmt.setDate(2, new java.sql.Date(evaluation.getVisitDate().getTime()));
            stmt.setString(3, evaluation.getIpAddress());
            stmt.setInt(4, evaluation.getRestaurant().getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        evaluation.setId(generatedKeys.getInt(1));
                        addToCache(evaluation);
                        logger.info("Like/Dislike créé avec l'ID : {}", evaluation.getId());
                        return evaluation;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la création du Like/Dislike : {}", e.getMessage());
        }
        return null;
    }

    @Override
    public boolean update(BasicEvaluation evaluation) {
        if (evaluation == null || evaluation.getId() == null) return false;
        try (Connection connection = ConnectionUtils.getConnection();
             PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
            stmt.setString(1, evaluation.getLikeRestaurant() ? "T" : "F");
            stmt.setDate(2, new java.sql.Date(evaluation.getVisitDate().getTime()));
            stmt.setString(3, evaluation.getIpAddress());
            stmt.setInt(4, evaluation.getRestaurant().getId());
            stmt.setInt(5, evaluation.getId());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                addToCache(evaluation);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du Like/Dislike : {}", e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(BasicEvaluation evaluation) {
        if (evaluation == null || evaluation.getId() == null) return false;
        return deleteById(evaluation.getId());
    }

    @Override
    public boolean deleteById(int id) {
        try (Connection connection = ConnectionUtils.getConnection();
             PreparedStatement stmt = connection.prepareStatement(DELETE)) {
            stmt.setInt(1, id);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                removeFromCache(id);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression du Like/Dislike : {}", e.getMessage());
        }
        return false;
    }

    public Set<BasicEvaluation> findByRestaurantId(int restaurantId) {
        Set<BasicEvaluation> res = new LinkedHashSet<>();
        String sql = "SELECT numero, appreciation, date_eval, adresse_ip, fk_rest FROM LIKES WHERE fk_rest = ? ORDER BY numero";
        try (Connection connection = ConnectionUtils.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, restaurantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BasicEvaluation be = mapRow(rs);
                    res.add(be);
                    addToCache(be);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche des Likes/Dislikes: {}", e.getMessage());
        }
        return res;
    }

    private BasicEvaluation mapRow(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("numero");
        String appreciation = rs.getString("appreciation");
        Date dateEval = rs.getDate("date_eval");
        String ip = rs.getString("adresse_ip");
        Integer restId = rs.getInt("fk_rest");
        Restaurant rest = null; // non chargé ici
        boolean isLike = "T".equalsIgnoreCase(appreciation);
        return new BasicEvaluation(id, dateEval, rest, isLike, ip);
    }
}
