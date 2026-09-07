# Book API

Backend Spring Boot de gestion de livres et d'avis de lecteurs, avec calcul
de statistiques et classement pondéré des ouvrages.

Projet réalisé dans le cadre du cours *java avancé* de l'ISMIN.
L'énoncé d'origine est conservé dans [SUJET.md](SUJET.md), et le contrat
d'API complet dans [openapi.yaml](openapi.yaml).

## Sommaire

- [Prérequis](#prérequis)
- [Lancer le projet](#lancer-le-projet)
- [Lancer les tests](#lancer-les-tests)
- [Endpoints](#endpoints)
- [Codes de retour](#codes-de-retour)
- [Score pondéré](#score-pondéré)
- [Architecture](#architecture)
- [Base de données](#base-de-données)

## Prérequis

| Outil | Version |
| --- | --- |
| JDK | 21 |
| Maven | fourni par le wrapper (`mvnw`) |

Aucune base de données à installer : SQLite tient dans un simple fichier,
créé automatiquement au premier démarrage.

## Lancer le projet

```bash
./mvnw spring-boot:run
```

Le backend écoute sur **http://localhost:8080**.

Au démarrage, Flyway crée les tables si nécessaire, puis Hibernate vérifie
que les entités Java correspondent bien au schéma. Un écart fait échouer le
démarrage plutôt que corrompre les données.

Vérification rapide, une fois lancé :

```bash
curl http://localhost:8080/api/book
```

## Lancer les tests

```bash
./mvnw test
```

23 tests, écrits selon le principe GIVEN / WHEN / THEN. Ils s'exécutent sur
une base dédiée (`target/test.sqlite`) et ne touchent jamais la base de
développement. Chaque test repart d'une base vide, ce qui les rend
indépendants de leur ordre d'exécution.

Le style du code est vérifié séparément :

```bash
./mvnw checkstyle:check
```

Les deux commandes sont rejouées par la CI à chaque push sur `main`.

## Endpoints

### Livres

| Méthode | Route | Description |
| --- | --- | --- |
| `GET` | `/api/book` | Lister tous les livres |
| `GET` | `/api/book/{id}` | Obtenir un livre par son identifiant technique |
| `POST` | `/api/book` | Ajouter un livre |
| `PUT` | `/api/book` | Créer ou mettre à jour un livre |
| `DELETE` | `/api/book/{id}` | Supprimer un livre |

### Avis

| Méthode | Route | Description |
| --- | --- | --- |
| `GET` | `/api/review` | Lister tous les avis |
| `GET` | `/api/review/{id}` | Obtenir un avis par son identifiant |
| `POST` | `/api/review` | Publier un avis |
| `PUT` | `/api/review` | Créer ou mettre à jour un avis |
| `DELETE` | `/api/review/{id}` | Supprimer un avis |

### Statistiques

| Méthode | Route | Description |
| --- | --- | --- |
| `GET` | `/api/books/{isbn}/statistics` | Statistiques d'un livre |
| `GET` | `/api/books/ranking` | Classement pondéré des livres |

### Exemples

Ajouter un livre :

```bash
curl -X POST http://localhost:8080/api/book \
  -H "Content-Type: application/json" \
  -d '{"isbn":9782070368228,"writer":"George Orwell","title":"1984","publishingDate":"1949-06-08"}'
```

Publier un avis :

```bash
curl -X POST http://localhost:8080/api/review \
  -H "Content-Type: application/json" \
  -d '{"bookIsbn":9782070368228,"reviewDate":"2026-09-07","reviewText":"Un classique","reviewRating":5}'
```

Consulter les statistiques :

```bash
curl http://localhost:8080/api/books/9782070368228/statistics
```

```json
{
  "mark": 4.571429,
  "numberOfReviews": 7,
  "markRepartition": { "1": 0, "2": 0, "3": 0, "4": 3, "5": 4 },
  "score": 4.6029415
}
```

## Codes de retour

| Code | Signification | Exemple |
| --- | --- | --- |
| `200` | Succès | lecture, création, mise à jour |
| `204` | Supprimé, sans contenu | `DELETE` réussi |
| `400` | Données invalides | note hors de 1–5, ISBN qui n'a pas 10 ou 13 chiffres, titre absent |
| `404` | Ressource absente | livre ou avis inexistant |
| `409` | Conflit | ISBN déjà utilisé par un autre livre |

Une requête invalide ne provoque jamais de `500` : elle est refusée avant
d'atteindre la base, avec un message expliquant le problème.

## Score pondéré

Le classement ne repose pas sur la moyenne seule, sinon un livre noté une
fois 5/5 devancerait un livre noté cinquante fois 4,8.

```
score = (v / (v + m)) × R  +  (m / (v + m)) × C
```

| Symbole | Signification |
| --- | --- |
| `R` | note moyenne du livre |
| `v` | nombre d'avis reçus par le livre |
| `C` | note moyenne de l'ensemble du catalogue |
| `m` | seuil de confiance, fixé à 10 |

Tant qu'un livre a peu d'avis, son score reste proche de la moyenne
générale `C`. Plus il en accumule, plus son propre `R` domine.

Les livres sans aucun avis sont **exclus du classement** : la formule leur
attribuerait exactement `C`, ce qui les placerait au-dessus de livres
réellement notés mais légèrement en dessous de cette moyenne. Leurs
statistiques restent consultables individuellement.

## Architecture

```
controller  →  service  →  repository  →  base de données
  (HTTP)      (calculs)     (accès)         (SQLite)
```

| Package | Rôle |
| --- | --- |
| `controller` | Réception des requêtes HTTP, validation, codes de retour |
| `service` | Logique métier : statistiques et classement |
| `repository` | Interfaces Spring Data, aucune requête SQL écrite à la main |
| `model` | Entités JPA persistées : `Book`, `Review` |
| `dto` | Objets de réponse non persistés : `Statistic`, `Score` |
| `config` | Configuration CORS |

Les calculs vivent dans le service et non dans le contrôleur, ce qui permet
de les tester sans démarrer de serveur web.

## Base de données

SQLite, dans le fichier `base.sqlite` à la racine. Ce fichier est généré et
n'est pas versionné.

Le schéma est géré par **Flyway** : chaque évolution est un script numéroté
dans `src/main/resources/db/migration/`, appliqué une seule fois et tracé
dans la table `flyway_schema_history`.

Hibernate est configuré en `ddl-auto=validate` : il ne modifie jamais le
schéma, il vérifie seulement que les entités lui correspondent. Flyway
construit, Hibernate contrôle.

Pour repartir d'une base vierge en développement, supprimer `base.sqlite`
et relancer l'application. **Ne jamais modifier une migration déjà
appliquée** : Flyway en conserve une empreinte et refuserait de démarrer.
