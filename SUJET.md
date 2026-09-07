# Lock'app Lite — Gestion de livres et d'avis

Ce projet Java Spring Boot permet d'enregistrer des livres, de publier des avis et de consulter les informations stockées depuis une API HTTP.

L'application est construite progressivement : création des modèles, ajout de la persistance, mise en place des routes CRUD, puis développement de traitements métier autour des notes.

## Chapitre 1 — Modèles

Création du modèle `Book`, qui représente un livre avec les informations suivantes :

- un identifiant technique ;
- un ISBN ;
- un titre ;
- un auteur ;
- une date de publication.

L'ISBN constitue l'identifiant fonctionnel du livre. Il doit contenir 10 ou 13 caractères et ne peut pas être utilisé par plusieurs livres.

Création du modèle `Review`, qui représente un avis publié sur un livre :

- un identifiant ;
- l'ISBN du livre concerné ;
- une note ;
- un commentaire ;
- une date de publication.

Un livre peut recevoir plusieurs avis. La note associée à un avis est comprise entre 1 et 5.

## Chapitre 2 — Base de données

Ajout de Spring Data JPA et de SQLite afin de rendre les livres et les avis persistants.

La base de données contient :

- une table pour les livres ;
- une table pour les avis ;
- une relation permettant d'associer plusieurs avis à un même livre.

Des repositories Spring Data JPA permettent d'enregistrer, rechercher, modifier et supprimer les données sans écrire manuellement toutes les requêtes SQL.

Flyway est utilisé pour créer et faire évoluer la structure de la base de données à partir de migrations versionnées.

## Chapitre 3 — Controllers CRUD

Création des controllers REST permettant de gérer les livres et les avis depuis une API HTTP.

### Livres

| Méthode | Route | Description |
| --- | --- | --- |
| `POST` | `/api/book` | Ajouter un livre |
| `GET` | `/api/book` | Rechercher ou lister les livres |
| `PUT` | `/api/book/{isbn}` | Modifier un livre |
| `DELETE` | `/api/book/{isbn}` | Supprimer un livre |

La recherche d'un livre peut être filtrée par ISBN, titre, auteur ou date de publication.

### Avis

| Méthode | Route | Description |
| --- | --- | --- |
| `POST` | `/api/review` | Publier un avis |
| `GET` | `/api/review` | Rechercher ou lister les avis |
| `PUT` | `/api/review/{id}` | Modifier un avis |
| `DELETE` | `/api/review/{id}` | Supprimer un avis |

Les controllers renvoient un code HTTP adapté au résultat de chaque opération : création, succès, données invalides, ressource absente ou conflit d'ISBN.

Le contrat détaillé de l'API est disponible dans le fichier `swagger.yml`.

À la fin de ce chapitre, le projet fournit une API CRUD connectée à la base de données SQLite et consommable par l'interface Angular.

## Chapitre 4 — Statistiques et classement des livres

Ajout d'un service chargé d'exploiter les avis afin de produire des informations utiles sur chaque livre.

Le service peut notamment calculer :

- la note moyenne d'un livre ;
- son nombre total d'avis ;
- la répartition des notes de 1 à 5 ;
- l'évolution récente de sa note ;
- un score pondéré utilisé pour le classement.

Le classement ne repose pas uniquement sur la moyenne. Il tient également compte du nombre d'avis afin d'éviter qu'un livre ayant reçu une seule excellente note soit automatiquement classé devant un livre évalué de nombreuses fois.

Une proposition consiste à utiliser le score pondéré suivant :

```text
score = (v / (v + m)) × R + (m / (v + m)) × C
```

avec :

- `R` : la note moyenne du livre ;
- `v` : le nombre d'avis reçus par le livre ;
- `C` : la note moyenne de l'ensemble des livres ;
- `m` : le nombre minimal d'avis choisi comme seuil de confiance.

Lorsqu'un livre possède peu d'avis, son score reste proche de la moyenne générale `C`. Plus son nombre d'avis augmente, plus le classement repose sur sa propre moyenne `R`. La valeur de `m` peut être ajustée selon la quantité d'avis disponible, par exemple à `10` pour un catalogue de petite taille.

Exemple pour un livre ayant une moyenne de `4,5` sur `20` avis, avec une moyenne générale de `3,8` et un seuil de confiance fixé à `10` :

```text
score = (20 / 30) × 4,5 + (10 / 30) × 3,8
score ≈ 4,27
```

Les principales routes prévues sont :

| Méthode | Route | Description |
| --- | --- | --- |
| `GET` | `/api/books/{isbn}/statistics` | Consulter les statistiques d'un livre |
| `GET` | `/api/books/ranking` | Consulter le classement pondéré des livres |

Ce chapitre ajoute la logique métier principale du backend : la base de données conserve les livres et les avis, tandis que le service Java agrège les notes et les transforme en statistiques exploitables.


## Lancement du projet

### Backend

```bash
./mvnw spring-boot:run
```

### Tests

```bash
./mvnw test
```

### Frontend

```bash
cd frontend
npm install
npm start
```

Le backend est accessible localement sur le port `6007`.
