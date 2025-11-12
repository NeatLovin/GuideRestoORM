package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static ch.hearc.ig.guideresto.persistence.ConnectionUtils.getConnection;

public class RestaurantMapper extends AbstractMapper<Restaurant> {

    private static final String SELECT_BY_ID = "SELECT numero, fk_vill, fk_type, nom, adresse, description, site_web FROM RESTAURANTS WHERE numero = ?";
    private static final String SELECT_ALL = "SELECT numero, fk_vill, fk_type, nom, adresse, description, site_web FROM RESTAURANTS ORDER BY nom";
    private static final String INSERT = "INSERT INTO RESTAURANTS (fk_vill, fk_type, nom, adresse, description, site_web) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE RESTAURANTS SET fk_vill = ?, fk_type = ?, nom = ?, adresse = ?, description = ?, site_web = ? WHERE numero = ?";
    private static final String DELETE = "DELETE FROM RESTAURANTS WHERE numero = ?";

    private static final String EXISTS_QUERY = "SELECT 1 FROM RESTAURANTS WHERE numero = ?";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM RESTAURANTS";
    private static final String SEQUENCE_QUERY = "SELECT SEQ_RESTAURANTS.CURRVAL FROM DUAL";

    private CityMapper cityMapper = new CityMapper();
    private RestaurantTypeMapper typeMapper = new RestaurantTypeMapper();

    @Override
    public Restaurant findById(int id) {
        Restaurant inCache = findInCache(id);
        if (inCache != null) return inCache;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Restaurant restaurant = mapResultSetToRestaurant(resultSet);
                    restaurant.getEvaluations().clear();
                    loadEvaluations(restaurant);
                    loadCompleteEvaluations(restaurant);
                    addToCache(restaurant);
                    return restaurant;
                }
            }
        } catch (SQLException e) {

            e.printStackTrace();
        }

        return null;
    }

    private Restaurant mapResultSetToRestaurant(ResultSet rs) throws SQLException {
        // Récupérer la ville
        City city = cityMapper.findById(rs.getInt("fk_vill"));

        // Récupérer le type
        RestaurantType type = typeMapper.findById(rs.getInt("fk_type"));

        // Créer la localisation
        Localisation address = new Localisation(rs.getString("adresse"), city);

        // Créer le restaurant
        Restaurant restaurant = new Restaurant(
                rs.getInt("numero"),
                rs.getString("nom"),
                rs.getString("description"),
                rs.getString("site_web"),
                address,
                type
        );

        return restaurant;
    }

    @Override
    public Set<Restaurant> findAll() {
        if (!identityMap().isEmpty()) {
            return new LinkedHashSet<>(identityMap().values());
        }
        Set<Restaurant> restaurants = new LinkedHashSet<>();
        String sql = "SELECT r.NUMERO, r.NOM, r.ADRESSE, r.DESCRIPTION, r.SITE_WEB, " +
                "r.FK_TYPE, r.FK_VILL, " +
                "t.NUMERO AS TYPE_ID, t.LIBELLE AS TYPE_LABEL, t.DESCRIPTION AS TYPE_DESC, " +
                "v.NUMERO AS VILLE_ID, v.CODE_POSTAL, v.NOM_VILLE " +
                "FROM RESTAURANTS r " +
                "INNER JOIN TYPES_GASTRONOMIQUES t ON r.FK_TYPE = t.NUMERO " +
                "INNER JOIN VILLES v ON r.FK_VILL = v.NUMERO";

        try (Connection connection = ConnectionUtils.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("NUMERO");
                if (!cache.containsKey(id) && !identityMap().containsKey(id)) {
                    Restaurant restaurant = mapResultSetToRestaurant(rs);
                    addToCache(restaurant);
                    restaurants.add(restaurant);
                } else {
                    Restaurant cached = identityMap().containsKey(id) ? identityMap().get(id) : cache.get(id);
                    restaurants.add(cached);
                }
            }

            // Charger les évaluations pour chaque restaurant
            for (Restaurant restaurant : restaurants) {
                restaurant.getEvaluations().clear();
                loadEvaluations(restaurant);           // Likes/Dislikes
                loadCompleteEvaluations(restaurant);   // Commentaires et notes
            }

        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération de tous les restaurants : {}", e.getMessage());
        }

        return restaurants;
    }




    @Override
    public Restaurant create(Restaurant restaurant) {
        Connection conn = ConnectionUtils.getConnection();
        String sql = "INSERT INTO RESTAURANTS (nom, adresse, description, site_web, fk_type, fk_vill) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"numero"})) {
            stmt.setString(1, restaurant.getName());
            stmt.setString(2, restaurant.getAddress().getStreet());
            stmt.setString(3, restaurant.getDescription());
            stmt.setString(4, restaurant.getWebsite());
            stmt.setInt(5, restaurant.getType().getId());
            stmt.setInt(6, restaurant.getAddress().getCity().getId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        restaurant.setId(generatedKeys.getInt(1));
                        addToCache(restaurant);
                        logger.info("Restaurant créé avec l'ID: {}", restaurant.getId());
                        return restaurant;
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la création du restaurant: {}", ex.getMessage());
        }
        return null;
    }

    @Override
    public boolean update(Restaurant restaurant) {
        Connection conn = ConnectionUtils.getConnection();
        String sql = "UPDATE RESTAURANTS SET nom = ?, adresse = ?, description = ?, " +
                "site_web = ?, fk_type = ?, fk_vill = ? WHERE numero = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, restaurant.getName());
            stmt.setString(2, restaurant.getAddress().getStreet());
            stmt.setString(3, restaurant.getDescription());
            stmt.setString(4, restaurant.getWebsite());
            stmt.setInt(5, restaurant.getType().getId());
            stmt.setInt(6, restaurant.getAddress().getCity().getId());
            stmt.setInt(7, restaurant.getId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                addToCache(restaurant);
                logger.info("Restaurant mis à jour: {}", restaurant.getId());
                return true;
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la mise à jour du restaurant: {}", ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(Restaurant restaurant) {
        return deleteById(restaurant.getId());
    }

    @Override
    public boolean deleteById(int id) {
        Connection conn = ConnectionUtils.getConnection();

        try {
            // Supprimer d'abord les notes liées aux commentaires
            String sqlNotes = "DELETE FROM NOTES WHERE fk_comm IN " +
                    "(SELECT numero FROM COMMENTAIRES WHERE fk_rest = ?)";
            try (PreparedStatement stmtNotes = conn.prepareStatement(sqlNotes)) {
                stmtNotes.setInt(1, id);
                stmtNotes.executeUpdate();
            }

            // Supprimer les commentaires
            String sqlComments = "DELETE FROM COMMENTAIRES WHERE fk_rest = ?";
            try (PreparedStatement stmtComments = conn.prepareStatement(sqlComments)) {
                stmtComments.setInt(1, id);
                stmtComments.executeUpdate();
            }

            // Supprimer les likes
            String sqlLikes = "DELETE FROM LIKES WHERE fk_rest = ?";
            try (PreparedStatement stmtLikes = conn.prepareStatement(sqlLikes)) {
                stmtLikes.setInt(1, id);
                stmtLikes.executeUpdate();
            }

            // Supprimer le restaurant
            String sqlRestaurant = "DELETE FROM RESTAURANTS WHERE numero = ?";
            try (PreparedStatement stmtRestaurant = conn.prepareStatement(sqlRestaurant)) {
                stmtRestaurant.setInt(1, id);
                int affectedRows = stmtRestaurant.executeUpdate();

                if (affectedRows > 0) {
                    removeFromCache(id);
                    logger.info("Restaurant supprimé: {}", id);
                    return true;
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la suppression du restaurant: {}", ex.getMessage());
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
    public Set<Restaurant> findByName(String name) {
        Set<Restaurant> restaurants = new HashSet<>();
        Connection conn = ConnectionUtils.getConnection();
        String sql = "SELECT r.numero, r.nom, r.adresse, r.description, r.site_web, " +
                "r.fk_type, r.fk_vill " +
                "FROM RESTAURANTS r WHERE UPPER(r.nom) LIKE UPPER(?) ORDER BY r.nom";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    restaurants.add(mapResultSetToRestaurant(rs));
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche de restaurants par nom: {}", ex.getMessage());
        }
        return restaurants;
    }

    public Set<Restaurant> findByCityName(String cityName) {
        Set<Restaurant> restaurants = new HashSet<>();
        Connection conn = ConnectionUtils.getConnection();
        String sql = "SELECT r.numero, r.nom, r.adresse, r.description, r.site_web, " +
                "r.fk_type, r.fk_vill " +
                "FROM RESTAURANTS r " +
                "INNER JOIN VILLES v ON r.fk_vill = v.numero " +
                "WHERE UPPER(v.nom_ville) LIKE UPPER(?) ORDER BY r.nom";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + cityName + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    restaurants.add(mapResultSetToRestaurant(rs));
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche de restaurants par ville: {}", ex.getMessage());
        }
        return restaurants;
    }

    public Set<Restaurant> findByType(int typeId) {
        Set<Restaurant> restaurants = new HashSet<>();
        Connection conn = ConnectionUtils.getConnection();
        String sql = "SELECT r.numero, r.nom, r.adresse, r.description, r.site_web, " +
                "r.fk_type, r.fk_vill " +
                "FROM RESTAURANTS r WHERE r.fk_type = ? ORDER BY r.nom";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, typeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    restaurants.add(mapResultSetToRestaurant(rs));
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la recherche de restaurants par type: {}", ex.getMessage());
        }
        return restaurants;
    }

    private void loadEvaluations(Restaurant restaurant) {
        String sql = "SELECT NUMERO, APPRECIATION, DATE_EVAL, ADRESSE_IP FROM LIKES WHERE FK_REST = ?";

        try (Connection connection = ConnectionUtils.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, restaurant.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String appreciation = rs.getString("APPRECIATION");
                    Date dateEval = rs.getDate("DATE_EVAL");
                    String ipAddress = rs.getString("ADRESSE_IP");
                    Integer numero = rs.getInt("NUMERO");

                    // 'T' = True = Like, 'F' = False = Dislike
                    boolean isLike = "T".equalsIgnoreCase(appreciation);

                    BasicEvaluation evaluation = new BasicEvaluation(
                            numero,
                            dateEval,
                            restaurant,
                            isLike,
                            ipAddress
                    );

                    restaurant.getEvaluations().add(evaluation);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du chargement des évaluations pour le restaurant {} : {}",
                    restaurant.getId(), e.getMessage());
        }
    }

    private void loadCompleteEvaluations(Restaurant restaurant) {
        String sql = "SELECT c.NUMERO, c.DATE_EVAL, c.COMMENTAIRE, c.NOM_UTILISATEUR, " +
                "n.NUMERO AS NOTE_ID, n.NOTE, " +
                "cr.NUMERO AS CRIT_ID, cr.NOM AS CRIT_NOM, cr.DESCRIPTION AS CRIT_DESC " +
                "FROM COMMENTAIRES c " +
                "LEFT JOIN NOTES n ON c.NUMERO = n.FK_COMM " +
                "LEFT JOIN CRITERES_EVALUATION cr ON n.FK_CRIT = cr.NUMERO " +
                "WHERE c.FK_REST = ? " +
                "ORDER BY c.NUMERO";

        try (Connection connection = ConnectionUtils.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, restaurant.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                Map<Integer, CompleteEvaluation> evaluationsMap = new HashMap<>();

                while (rs.next()) {
                    Integer evalId = rs.getInt("NUMERO");

                    if (!evaluationsMap.containsKey(evalId)) {
                        CompleteEvaluation evaluation = new CompleteEvaluation(
                                evalId,
                                rs.getDate("DATE_EVAL"),
                                restaurant,
                                rs.getString("COMMENTAIRE"),
                                rs.getString("NOM_UTILISATEUR")
                        );
                        evaluationsMap.put(evalId, evaluation);
                        restaurant.getEvaluations().add(evaluation);
                    }

                    Integer noteId = rs.getInt("NOTE_ID");
                    if (!rs.wasNull()) {
                        CompleteEvaluation evaluation = evaluationsMap.get(evalId);
                        EvaluationCriteria criteria = new EvaluationCriteria(
                                rs.getInt("CRIT_ID"),
                                rs.getString("CRIT_NOM"),
                                rs.getString("CRIT_DESC")
                        );
                        Grade grade = new Grade(
                                noteId,
                                rs.getInt("NOTE"),
                                evaluation,
                                criteria
                        );
                        evaluation.getGrades().add(grade);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du chargement des évaluations complètes : {}", e.getMessage());
        }
    }






}
