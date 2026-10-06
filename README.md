# M2D — Recherche d'images par similarité

Application web **client-serveur** développée dans le cadre du cours de **Développement Logiciel — L3 Informatique, Université de Bordeaux**.

Le projet permet de **stocker, gérer, consulter et rechercher des images couleur** aux formats JPEG et PNG. La recherche peut notamment s'appuyer sur différents **descripteurs d'images** afin d'identifier des images présentant des caractéristiques similaires.

---

## Fonctionnalités

### Gestion des images

Le serveur permet de :

* Ajouter des images ;
* Supprimer des images ;
* Récupérer les images ;
* Consulter les métadonnées associées ;
* Associer des mots-clés aux images ;
* Modifier ou supprimer les mots-clés.

### Recherche

L'application permet de rechercher des images selon différents critères :

* Attributs des images ;
* Métadonnées ;
* Mots-clés ;
* Similarité entre images.

Le système peut également construire des listes d'images selon certains attributs ou selon leur niveau de similarité.

### Interface web

Le client permet à l'utilisateur de :

* Parcourir les images disponibles ;
* Visualiser les images ;
* Consulter leurs métadonnées ;
* Consulter les images similaires ;
* Ajouter ou supprimer des images ;
* Gérer les mots-clés ;
* Effectuer des recherches.

---

## Architecture

Le projet suit une architecture **client-serveur**.

```text
┌─────────────────────────────┐
│          Client Web         │
│                             │
│  Consultation              │
│  Recherche                 │
│  Métadonnées               │
│  Gestion des images        │
└──────────────┬──────────────┘
               │
               │ HTTP / REST
               ▼
┌─────────────────────────────┐
│       Serveur Spring Boot   │
│                             │
│  Controllers REST           │
│  DAO                         │
│  Gestion des images         │
│  Métadonnées                │
│  Recherche / similarité     │
└──────────────┬──────────────┘
               │
       ┌───────┴────────┐
       ▼                ▼
┌────────────┐   ┌──────────────┐
│ Base de    │   │ Dossier      │
│ données    │   │ images       │
└────────────┘   └──────────────┘
```

Le serveur expose des **endpoints REST** utilisés par le client web pour accéder aux différentes fonctionnalités de l'application.

---

## Technologies

### Backend

* **Java 17**
* **Spring Boot**
* **Maven**
* API REST

### Persistance

* Base de données relationnelle
* DAO pour l'accès aux données
* Stockage des fichiers images

### Frontend

* Application web client
* Communication avec le backend via API REST

### Environnement

* Debian 12
* Java 17
* Maven

---

## Structure du projet

```text
src/
├── main/
│   ├── java/
│   │   └── pdl/
│   │       └── backend/
│   │           ├── dao/
│   │           └── controller/
│   │
│   └── resources/
│       ├── images/
│       └── application.yaml/
│
└── test/
    └── java/
        └── pdl/
            └── backend/
```

### Principaux répertoires

| Répertoire                             | Rôle                            |
| -------------------------------------- | ------------------------------- |
| `src/main/java/pdl/backend/dao`        | Accès et gestion des données    |
| `src/main/java/pdl/backend/controller` | Endpoints REST                  |
| `src/main/resources/images`            | Images utilisées pour les tests |
| `src/main/resources/application.yaml`  | Configuration Spring Boot       |
| `src/test/java/pdl/backend`            | Tests du backend                |

---

## Environnement de test

### Système d'exploitation

Le serveur a été testé sur :

* **Debian 12 (Bookworm)**

### Navigateurs

L'interface web a été testée avec :

* **Mozilla Firefox 140.8.0 ESR**
* **Brave 1.88.132**

---

## Prérequis

Pour exécuter le projet, installer :

* **Java 17**
* **Maven**
* Un navigateur web récent

Vérifier les installations :

```bash
java -version
mvn -version
```

---

## Installation

### 1. Cloner le dépôt

```bash
git clone <URL_DU_PROJET>
cd <DOSSIER_DU_PROJET>
```

### 2. Compiler le projet

```bash
mvn clean install
```

### 3. Lancer l'application

```bash
mvn spring-boot:run
```

Le serveur démarre alors avec la configuration définie dans :

```text
src/main/resources/application.yaml
```

---

## Tests

Les tests du backend sont situés dans :

```text
src/test/java/pdl/backend
```

Ils peuvent être exécutés avec :

```bash
mvn test
```

Si certains tests nécessitent une configuration ou des dépendances particulières, consulter la configuration du projet avant leur exécution.

---

## Équipe

Projet réalisé en équipe dans le cadre de la L3 Informatique à l'Université de Bordeaux.

| Membre                  | Rôle        |
| ----------------------- | ----------- |
| **Hassan Youssouf**     | Développeur |
| **Adoum Mahamat Tahir** | Développeur |
| **Samy Medjtoh**        | Développeur |

---

## Objectifs pédagogiques

Ce projet nous a permis de mettre en pratique plusieurs notions de développement logiciel :

* Conception d'une application client-serveur ;
* Développement d'une API REST ;
* Architecture backend avec Spring Boot ;
* Accès aux données avec une couche DAO ;
* Gestion et stockage de fichiers ;
* Manipulation d'images ;
* Gestion de métadonnées ;
* Recherche par similarité ;
* Développement d'une interface web ;
* Tests logiciels ;
* Travail collaboratif en équipe.

---

## Statut

**Projet universitaire — L3 Informatique, Université de Bordeaux**

Projet réalisé en équipe dans le cadre du cours de **Développement Logiciel**.

---
