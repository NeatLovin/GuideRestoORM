package ch.hearc.ig.guideresto.presentation;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.*;
import ch.hearc.ig.guideresto.service.RestaurantService;
import ch.hearc.ig.guideresto.service.CityService;
import ch.hearc.ig.guideresto.service.RestaurantTypeService;
import ch.hearc.ig.guideresto.service.EvaluationCriteriaService;
import ch.hearc.ig.guideresto.service.EvaluationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author cedric.baudet
 * @author alain.matile
 */
public class Application {

    private static Scanner scanner;
    private static final Logger logger = LogManager.getLogger(Application.class);

    // Services partagés
    private static EvaluationService evaluationService; // ajouté
    private static EvaluationCriteriaService criteriaService; // ajouté
    private static RestaurantService restaurantService; // ajouté

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("guideRestoJPA");
        EntityManager em = emf.createEntityManager();

        // Instanciation des mappers JPA
        CityMapper cityMapper = new CityMapper(em);
        RestaurantTypeMapper typeMapper = new RestaurantTypeMapper(em);
        RestaurantMapper restaurantMapper = new RestaurantMapper(em);
        GradeMapper gradeMapper = new GradeMapper(em);
        CompleteEvaluationMapper completeEvaluationMapper = new CompleteEvaluationMapper(em, gradeMapper);
        EvaluationCriteriaMapper evaluationCriteriaMapper = new EvaluationCriteriaMapper(em);
        BasicEvaluationMapper basicEvaluationMapper = new BasicEvaluationMapper(em);

        // Instanciation des services
        CityService cityService = new CityService(em, cityMapper);
        RestaurantTypeService typeService = new RestaurantTypeService(em, typeMapper);
        restaurantService = new RestaurantService(em, cityMapper, restaurantMapper);
        evaluationService = new EvaluationService(em, completeEvaluationMapper, gradeMapper, basicEvaluationMapper);
        criteriaService = new EvaluationCriteriaService(em, evaluationCriteriaMapper);

        System.out.println("Bienvenue dans GuideResto ! Que souhaitez-vous faire ?");
        int choice;
        do {
            printMainMenu();
            choice = readInt();
            proceedMainMenu(choice, cityService, typeService, restaurantService);
        } while (choice != 0);

        em.close();
        emf.close();
    }

    /**
     * Affichage du menu principal de l'application
     */
    private static void printMainMenu() {
        System.out.println("======================================================");
        System.out.println("Que voulez-vous faire ?");
        System.out.println("1. Afficher la liste de tous les restaurants");
        System.out.println("2. Rechercher un restaurant par son nom");
        System.out.println("3. Rechercher un restaurant par ville");
        System.out.println("4. Rechercher un restaurant par son type de cuisine");
        System.out.println("5. Saisir un nouveau restaurant");
        System.out.println("0. Quitter l'application");
    }

    /**
     * On gère le choix saisi par l'utilisateur
     *
     * @param choice Un nombre entre 0 et 5.
     */
    private static void proceedMainMenu(int choice, CityService cityService, RestaurantTypeService typeService,
            RestaurantService restaurantService) {
        switch (choice) {
            case 1:
                showRestaurantsList(restaurantService);
                break;
            case 2:
                searchRestaurantByName(restaurantService);
                break;
            case 3:
                searchRestaurantByCity(restaurantService);
                break;
            case 4:
                searchRestaurantByType(restaurantService, typeService);
                break;
            case 5:
                addNewRestaurant(cityService, typeService, restaurantService);
                break;
            case 0:
                System.out.println("Au revoir !");
                break;
            default:
                System.out.println("Erreur : saisie incorrecte. Veuillez réessayer");
                break;
        }
    }

    /**
     * Affiche la liste de tous les restaurants, sans filtre
     */
    private static void showRestaurantsList(RestaurantService restaurantService) {
        System.out.println("Liste des restaurants : ");
        java.util.Set<ch.hearc.ig.guideresto.business.Restaurant> restaurants = restaurantService.findAllRestaurants();
        Restaurant restaurant = pickRestaurant(restaurants);
        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * On affiche à l'utilisateur une liste de restaurants numérotés, et il doit en
     * sélectionner un !
     *
     * @param restaurants Liste à afficher
     * @return L'instance du restaurant choisi par l'utilisateur
     */
    private static Restaurant pickRestaurant(Set<Restaurant> restaurants) {
        if (restaurants.isEmpty()) { // Si la liste est vide on s'arrête là
            System.out.println("Aucun restaurant n'a été trouvé !");
            return null;
        }

        String result;
        for (Restaurant currentRest : restaurants) {
            result = "";
            result = "\"" + result + currentRest.getName() + "\" - " + currentRest.getAddress().getStreet() + " - ";
            result = result + currentRest.getAddress().getCity().getZipCode() + " "
                    + currentRest.getAddress().getCity().getCityName();
            System.out.println(result);
        }

        System.out.println(
                "Veuillez saisir le nom exact du restaurant dont vous voulez voir le détail, ou appuyez sur Enter pour revenir en arrière");
        String choice = readString();

        return searchRestaurantByName(restaurants, choice);
    }

    /**
     * Affiche une liste de restaurants dont le nom contient une chaîne de
     * caractères saisie par l'utilisateur
     */
    private static void searchRestaurantByName(RestaurantService restaurantService) {
        System.out.println("Veuillez entrer une partie du nom recherché : ");
        String research = readString();
        java.util.Set<ch.hearc.ig.guideresto.business.Restaurant> filteredList = restaurantService
                .findRestaurantsByName(research);
        Restaurant restaurant = pickRestaurant(filteredList);
        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * Affiche une liste de restaurants dont le nom de la ville contient une chaîne
     * de caractères saisie par l'utilisateur
     */
    private static void searchRestaurantByCity(RestaurantService restaurantService) {
        System.out.println("Veuillez entrer une partie du nom de la ville désirée : ");
        String research = readString();
        java.util.Set<ch.hearc.ig.guideresto.business.Restaurant> filteredList = restaurantService
                .findRestaurantsByCityName(research);
        Restaurant restaurant = pickRestaurant(filteredList);
        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * L'utilisateur choisit une ville parmi celles présentes dans le système.
     *
     * @return La ville sélectionnée, ou null si aucune ville n'a été choisie.
     */
    private static City pickCity(CityService cityService) {
        Set<City> cities = cityService.findAllCities();
        System.out.println("Voici la liste des villes possibles, veuillez entrer le NPA de la ville désirée : ");
        for (City currentCity : cities) {
            System.out.println(currentCity.getZipCode() + " " + currentCity.getCityName());
        }
        System.out.println("Entrez \"NEW\" pour créer une nouvelle ville");
        String choice = readString();
        if (choice.equals("NEW")) {
            City city = new City();
            System.out.println("Veuillez entrer le NPA de la nouvelle ville : ");
            city.setZipCode(readNonBlankString());
            System.out.println("Veuillez entrer le nom de la nouvelle ville : ");
            city.setCityName(readNonBlankString());
            cityService.createCity(city);
            return city;
        }
        return searchCityByZipCode(cities, choice);
    }

    /**
     * L'utilisateur choisit un type de restaurant parmis ceux présents dans le
     * système.
     *
     * @return Le type sélectionné, ou null si aucun type n'a été choisi.
     */
    private static RestaurantType pickRestaurantType(RestaurantTypeService typeService) {
        Set<RestaurantType> types = typeService.findAllTypes();
        System.out.println("Voici la liste des types possibles, veuillez entrer le libellé exact du type désiré : ");
        for (RestaurantType currentType : types) {
            System.out.println("\"" + currentType.getLabel() + "\" : " + currentType.getDescription());
        }
        String choice = readString();
        return searchTypeByLabel(types, choice);
    }

    /**
     * L'utilisateur commence par sélectionner un type de restaurant, puis
     * sélectionne un des restaurants proposés s'il y en a.
     * Si l'utilisateur sélectionne un restaurant, ce dernier lui sera affiché.
     */
    private static void searchRestaurantByType(RestaurantService restaurantService, RestaurantTypeService typeService) {
        RestaurantType chosenType = pickRestaurantType(typeService);
        java.util.Set<ch.hearc.ig.guideresto.business.Restaurant> filteredList = new java.util.LinkedHashSet<>();
        if (chosenType != null) {
            filteredList.addAll(restaurantService.findRestaurantsByType(chosenType.getId()));
        }
        Restaurant restaurant = pickRestaurant(filteredList);
        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * Le programme demande les informations nécessaires à l'utilisateur puis crée
     * un nouveau restaurant dans le système.
     */
    private static void addNewRestaurant(CityService cityService, RestaurantTypeService typeService,
            RestaurantService restaurantService) {
        System.out.println("Vous allez ajouter un nouveau restaurant !");
        System.out.println("Quel est son nom ?");
        String name = readNonBlankString();
        System.out.println("Veuillez entrer une courte description : ");
        String description = readString();
        System.out.println("Veuillez entrer l'adresse de son site internet : ");
        String website = readString();
        System.out.println("Rue : ");
        String street = readNonBlankString();
        City city;
        do {
            city = pickCity(cityService);
        } while (city == null);
        RestaurantType restaurantType;
        do {
            restaurantType = pickRestaurantType(typeService);
        } while (restaurantType == null);

        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setDescription(description);
        restaurant.setWebsite(website);
        Localisation localisation = new Localisation();
        localisation.setStreet(street);
        localisation.setCity(city);
        restaurant.setAddress(localisation);
        restaurant.setType(restaurantType);

        restaurantService.createRestaurant(restaurant, localisation, city);
        showRestaurant(restaurant);
    }

    /**
     * Affiche toutes les informations du restaurant passé en paramètre, puis
     * affiche le menu des actions disponibles sur ledit restaurant
     *
     * @param restaurant Le restaurant à afficher
     */
    private static void showRestaurant(Restaurant restaurant) {
        System.out.println("Affichage d'un restaurant : ");
        StringBuilder sb = new StringBuilder();
        sb.append(restaurant.getName()).append("\n");
        sb.append(restaurant.getDescription()).append("\n");
        sb.append(restaurant.getType().getLabel()).append("\n");
        sb.append(restaurant.getWebsite()).append("\n");
        sb.append(restaurant.getAddress().getStreet()).append(", ");
        sb.append(restaurant.getAddress().getCity().getZipCode()).append(" ")
                .append(restaurant.getAddress().getCity().getCityName()).append("\n");
        sb.append("Nombre de likes : ").append(countLikes(restaurant.getEvaluations(), true)).append("\n");
        sb.append("Nombre de dislikes : ").append(countLikes(restaurant.getEvaluations(), false)).append("\n");
        sb.append("\nEvaluations reçues : ").append("\n");

        String text;
        for (Evaluation currentEval : restaurant.getEvaluations()) {
            text = getCompleteEvaluationDescription(currentEval);
            if (text != null) { // On va recevoir des null pour les BasicEvaluation donc on ne les traite pas !
                sb.append(text).append("\n");
            }
        }

        System.out.println(sb);

        int choice;
        do { // Tant que l'utilisateur n'entre pas 0 ou 6, on lui propose à nouveau les
             // actions
            showRestaurantMenu();
            choice = readInt();
            proceedRestaurantMenu(choice, restaurant);
        } while (choice != 0 && choice != 6); // 6 car le restaurant est alors supprimé...
    }

    /**
     * Parcourt la liste et compte le nombre d'évaluations basiques positives ou
     * négatives en fonction du paramètre likeRestaurant
     *
     * @param evaluations    La liste des évaluations à parcourir
     * @param likeRestaurant Veut-on le nombre d'évaluations positives ou négatives
     *                       ?
     * @return Le nombre d'évaluations positives ou négatives trouvées
     */
    private static int countLikes(Set<Evaluation> evaluations, Boolean likeRestaurant) {
        int count = 0;
        for (Evaluation currentEval : evaluations) {
            if (currentEval instanceof BasicEvaluation
                    && ((BasicEvaluation) currentEval).getLikeRestaurant() == likeRestaurant) {
                count++;
            }
        }
        return count;
    }

    /**
     * Retourne un String qui contient le détail complet d'une évaluation si elle
     * est de type "CompleteEvaluation". Retourne null s'il s'agit d'une
     * BasicEvaluation
     *
     * @param eval L'évaluation à afficher
     * @return Un String qui contient le détail complet d'une CompleteEvaluation, ou
     *         null s'il s'agit d'une BasicEvaluation
     */
    private static String getCompleteEvaluationDescription(Evaluation eval) {
        StringBuilder result = new StringBuilder();

        if (eval instanceof CompleteEvaluation ce) {
            result.append("Evaluation de : ").append(ce.getUsername()).append("\n");
            result.append("Commentaire : ").append(ce.getComment()).append("\n");
            for (Grade currentGrade : ce.getGrades()) {
                result.append(currentGrade.getCriteria().getName()).append(" : ").append(currentGrade.getGrade())
                        .append("/5").append("\n");
            }
        }

        return result.isEmpty() ? null : result.toString();
    }

    /**
     * Affiche dans la console un ensemble d'actions réalisables sur le restaurant
     * actuellement sélectionné !
     */
    private static void showRestaurantMenu() {
        System.out.println("======================================================");
        System.out.println("Que souhaitez-vous faire ?");
        System.out.println("1. J'aime ce restaurant !");
        System.out.println("2. Je n'aime pas ce restaurant !");
        System.out.println("3. Faire une évaluation complète de ce restaurant !");
        System.out.println("4. Editer ce restaurant");
        System.out.println("5. Editer l'adresse du restaurant");
        System.out.println("6. Supprimer ce restaurant");
        System.out.println("0. Revenir au menu principal");
    }

    /**
     * Traite le choix saisi par l'utilisateur
     *
     * @param choice     Un numéro d'action, entre 0 et 6. Si le numéro ne se trouve
     *                   pas dans cette plage, l'application ne fait rien et va
     *                   réafficher le menu complet.
     * @param restaurant L'instance du restaurant sur lequel l'action doit être
     *                   réalisée
     */
    private static void proceedRestaurantMenu(int choice, Restaurant restaurant) {
        // Pour une vraie application, il faudrait passer les mappers ici aussi
        switch (choice) {
            case 1:
                addBasicEvaluation(restaurant, true);
                break;
            case 2:
                addBasicEvaluation(restaurant, false);
                break;
            case 3:
                evaluateRestaurant(restaurant);
                break;
            case 4:
                editRestaurant(restaurant);
                break;
            case 5:
                editRestaurantAddress(restaurant);
                break;
            case 6:
                deleteRestaurant(restaurant);
                break;
            default:
                break;
        }
    }

    /**
     * Ajoute au restaurant passé en paramètre un like ou un dislike, en fonction du
     * second paramètre.
     * L'IP locale de l'utilisateur est enregistrée. S'il s'agissait d'une
     * application web, il serait préférable de récupérer l'adresse IP publique de
     * l'utilisateur.
     *
     * @param restaurant Le restaurant qui est évalué
     * @param like       Est-ce un like ou un dislike ?
     */
    private static void addBasicEvaluation(Restaurant restaurant, Boolean like) {
        String ipAddress;
        try {
            ipAddress = Inet4Address.getLocalHost().toString();
        } catch (UnknownHostException ex) {
            logger.error("Error - Couldn't retreive host IP address");
            ipAddress = "Indisponible";
        }
        BasicEvaluation eval = new BasicEvaluation(null, new Date(), restaurant, like, ipAddress);
        evaluationService.createBasicEvaluation(eval);
        // Mise à jour immédiate de l'état en mémoire
        if (restaurant.getEvaluations() == null) {
            restaurant.setEvaluations(new LinkedHashSet<>());
        }
        restaurant.getEvaluations().add(eval);
        System.out.println("Votre vote a été pris en compte !");
    }

    /**
     * Crée une évaluation complète pour le restaurant. L'utilisateur doit saisir
     * toutes les informations (dont un commentaire et quelques notes)
     *
     * @param restaurant Le restaurant à évaluer
     */
    private static void evaluateRestaurant(Restaurant restaurant) {
        System.out.println("Merci d'évaluer ce restaurant !");
        System.out.println("Quel est votre nom d'utilisateur ? ");
        String username = readString();
        System.out.println("Quel commentaire aimeriez-vous publier?");
        String comment = readString();
        CompleteEvaluation eval = new CompleteEvaluation(null, new Date(), restaurant, comment, username);
        Set<EvaluationCriteria> criterias = criteriaService.findAllCriteria();
        for (EvaluationCriteria crit : criterias) {
            int gradeValue;
            do {
                System.out.println("Note pour '" + crit.getName() + "' (1-5) : ");
                gradeValue = readInt();
            } while (gradeValue < 1 || gradeValue > 5);
            Grade g = new Grade(null, gradeValue, eval, crit);
            g.setEvaluation(eval);
            g.setCriteria(crit);
            eval.getGrades().add(g);
        }
        evaluationService.createCompleteEvaluation(eval);
        // Mise à jour immédiate de l'état en mémoire
        if (restaurant.getEvaluations() == null) {
            restaurant.setEvaluations(new LinkedHashSet<>());
        }
        restaurant.getEvaluations().add(eval);
        System.out.println("Votre évaluation a bien été enregistrée, merci !");
    }

    /**
     * Force l'utilisateur à saisir à nouveau toutes les informations du restaurant
     * (sauf la clé primaire) pour le mettre à jour.
     * Par soucis de simplicité, l'utilisateur doit tout resaisir.
     *
     * @param restaurant Le restaurant à modifier
     */
    private static void editRestaurant(Restaurant restaurant) {
        // IMPORTANT : on acquiert le verrou pessimiste AVANT la saisie utilisateur,
        // sinon on ne bloque jamais les autres sessions pendant l'édition.
        RestaurantService.RestaurantEditSession session = null;
        try {
            session = restaurantService.beginEditRestaurant(restaurant.getId());
            Restaurant locked = session.getLockedRestaurant();

            System.out.println("Edition d'un restaurant !");
            System.out.println("Nouveau nom : ");
            locked.setName(readNonBlankString());
            System.out.println("Nouvelle description : ");
            locked.setDescription(readString());
            System.out.println("Nouveau site web : ");
            locked.setWebsite(readString());

            session.commit();
            System.out.println("Merci, le restaurant a bien été modifié !");
        } catch (RuntimeException e) {
            if (session != null)
                session.rollback();
            System.out.println(e.getMessage());
        }
    }

    /**
     * Permet à l'utilisateur de mettre à jour l'adresse du restaurant.
     * Par soucis de simplicité, l'utilisateur doit tout resaisir.
     *
     * @param restaurant Le restaurant dont l'adresse doit être mise à jour.
     */
    private static void editRestaurantAddress(Restaurant restaurant) {
        RestaurantService.RestaurantEditSession session = null;
        try {
            session = restaurantService.beginEditRestaurant(restaurant.getId());
            Restaurant locked = session.getLockedRestaurant();

            System.out.println("Edition de l'adresse d'un restaurant !");
            System.out.println("Nouvelle rue : ");
            locked.getAddress().setStreet(readNonBlankString());

            session.commit();
            System.out.println("L'adresse a bien été modifiée ! Merci !");
        } catch (RuntimeException e) {
            if (session != null)
                session.rollback();
            System.out.println(e.getMessage());
        }
    }

    /**
     * Après confirmation par l'utilisateur, supprime complètement le restaurant et
     * toutes ses évaluations du référentiel.
     */
    private static void deleteRestaurant(Restaurant restaurant) {
        System.out.println("Etes-vous sûr de vouloir supprimer ce restaurant ? (O/n)");
        String choice = readString();
        if (choice.equals("o") || choice.equals("O")) {
            try {
                restaurantService.deleteRestaurant(restaurant);
                System.out.println("Le restaurant a bien été supprimé !");
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Suppression annulée.");
        }
    }

    /**
     * Recherche dans le Set le restaurant comportant le nom passé en paramètre.
     * Retourne null si le restaurant n'est pas trouvé.
     *
     * @param restaurants Set de restaurants
     * @param name        Nom du restaurant à rechercher
     * @return L'instance du restaurant ou null si pas trouvé
     */
    private static Restaurant searchRestaurantByName(Set<Restaurant> restaurants, String name) {
        for (Restaurant current : restaurants) {
            if (current.getName().equalsIgnoreCase(name)) {
                return current;
            }
        }
        return null;
    }

    /**
     * Recherche dans le Set la ville comportant le code NPA passé en paramètre.
     * Retourne null si la ville n'est pas trouvée
     *
     * @param cities  Set de villes
     * @param zipCode NPA de la ville à rechercher
     * @return L'instance de la ville ou null si pas trouvé
     */
    private static City searchCityByZipCode(Set<City> cities, String zipCode) {
        for (City current : cities) {
            if (current.getZipCode().equalsIgnoreCase(zipCode)) {
                return current;
            }
        }
        return null;
    }

    /**
     * Recherche dans le Set le type comportant le libellé passé en paramètre.
     * Retourne null si aucun type n'est trouvé.
     *
     * @param types Set de types de restaurant
     * @param label Libellé du type recherché
     * @return L'instance RestaurantType ou null si pas trouvé
     */
    private static RestaurantType searchTypeByLabel(Set<RestaurantType> types, String label) {
        for (RestaurantType current : types) {
            if (current.getLabel().equalsIgnoreCase(label)) {
                return current;
            }
        }
        return null;
    }

    /**
     * readInt ne repositionne pas le scanner au début d'une ligne donc il faut le
     * faire manuellement sinon
     * des problèmes apparaissent quand on demande à l'utilisateur de saisir une
     * chaîne de caractères.
     *
     * @return Un nombre entier saisi par l'utilisateur au clavier
     */
    private static int readInt() {
        int i = 0;
        boolean success = false;
        do { // Tant que l'utilisateur n'aura pas saisi un nombre entier, on va lui demander
             // une nouvelle saisie
            try {
                i = scanner.nextInt();
                success = true;
            } catch (InputMismatchException e) {
                System.out.println("Erreur ! Veuillez entrer un nombre entier s'il vous plaît !");
            } finally {
                scanner.nextLine();
            }

        } while (!success);

        return i;
    }

    /**
     * Méthode readString pour rester consistant avec readInt !
     *
     * @return Une chaîne de caractères saisie par l'utilisateur au clavier
     */
    private static String readString() {
        return scanner.nextLine();
    }

    /**
     * Oracle traite la chaîne vide "" comme NULL.
     * Cette méthode force une saisie non vide (après trim) pour éviter des
     * violations de contraintes NOT NULL.
     */
    private static String readNonBlankString() {
        String value;
        do {
            value = readString();
            if (value != null) {
                value = value.trim();
            }
            if (value == null || value.isEmpty()) {
                System.out.println("Erreur : valeur obligatoire, veuillez réessayer :");
            }
        } while (value == null || value.isEmpty());
        return value;
    }

}
