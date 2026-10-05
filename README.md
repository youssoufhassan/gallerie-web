## Équipe

- **Dev1 :** Hassan Youssouf
- **Dev2 :** Adoum Mahamat Tahir
- **Dev3 :** Samy Medjtoh 



# Projet M2D – Recherche d’images par similarité
## Description du projet
Ce projet est en cours de réalisation dans le cadre du cours de Développement Logiciel (L3 Informatique, Université de Bordeaux).  
L’objectif est de développer une **application web client-serveur** permettant de gérer des images couleur (formats JPEG et PNG) et de rechercher des images **similaires** selon différents descripteurs.

Le serveur gère : 
- L’ajout, la suppression et la récupération des images.
- La gestion des méta-données et des mots-clés associés aux images.
- La construction de listes d’images selon des attributs ou la similarité.
- La persistance des données via un dossier `images` et une base de données.

Le client permet à l’utilisateur : 
- De parcourir les images disponibles et les visualiser.
- D’afficher les images similaires et les méta-données associées.
- D’ajouter ou supprimer des images et des mots-clés.
- De rechercher des images selon certains attributs.


## Structure du projet

- `src/main/java/pdl/backend/dao` : classes DAO pour gérer les images
- `src/main/java/pdl/backend/controller` : endpoints REST pour accéder aux images
- `src/main/resources/images` : images de test
- `src/main/resources/application.yaml` : configuration Spring Boot
- `src/test/java/pdl/backend` : tests unitaires (peuvent être désactivés temporairement)

##  Environnement de test

### Systèmes d’exploitation testés
Le serveur de l’application a été testé sur le système suivant :

- Debian 12 (Bookworm)

### Navigateurs web testés
Le client de l’application a été testé sur les navigateurs suivants :

- Mozilla Firefox 140.8.0 ESR  
- Brave 1.88.132 (14 mars 2026)

---

## Lancer le projet

1. Installer **Java 17** et **Maven**
2. Cloner le dépôt : 
```bash
git clone <URL_du_projet>

