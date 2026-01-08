# GuideResto ORM

Un canevas de projet simple permettant aux étudiants de la HEG-Arc de pratiquer la persistance à l'aide du framework Hibernate et de démontrer leur aptitude à transformer un projet JDBC en projet guidé par un ORM.

## But

Le but de ce projet est de permettre l'exécution en parallèle du cours de modules de pratique. Vous apprendrez ainsi la persistance des données pas à pas.

## Evaluation

Les conditions d'évaluation de ce cours sont indiquées dans les supports théoriques.

## Démarrer

Pour commencer, veuillez:

1. Copier le fichier `src/main/java/resources/hibernate.properties.template` et l'appeler `hibernate.properties`
2. Y renseigner vos identifiants de base de données
3. (optionnel) Copier vos Data Mappers et vos services dans le projet

La suite des instructions est disponible dans le cours sur CyberLearn.

**Note:** ce projet est pour l'heure configuré pour Oracle via `hibernate.properties` (ou `hibernate.properties.template`).
Vous pouvez récupérer vos Data Mappers ainsi que vos services et les injecter dans le projet pour démarrer plus rapidement.

## Test manuel concurrence

Lancez deux instances de l'application, éditez ou supprimez le même restaurant en même temps.
Une seule modification/suppression passera, l'autre affichera :

> Conflit de modification: ce restaurant est en cours de modification par un autre utilisateur. Rechargez la fiche et réessayez.

## Credits

Le modèle de domaine a été réalisé par Cédric Baudet. Consultez [ce dépôt](https://github.com/cedricbaudet/GuideResto) pour accéder aux sources originales.
