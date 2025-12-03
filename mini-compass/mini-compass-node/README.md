# API Mini Compass — Starter kit (Express + MongoDB)

Ce projet est un **point de départ** pour votre travail de 3 semaines :  
créer une interface Java (Swing) qui parle à une API Express et manipule des collections MongoDB.

Ici, vous avez seulement **l'API (backend)**.  
Votre travail sera de créer le **client Java** qui envoie des requêtes HTTP à cette API.

## 1. Prérequis

Avant de commencer, vous devez avoir installé :

1. **Node.js** (version LTS recommandée)  
   - Télécharger depuis le site officiel de Node.js.
2. **MongoDB**
   - soit en local (MongoDB Community Server),
   - soit un cluster gratuit MongoDB Atlas.
3. Un éditeur de code :
   - VS Code, WebStorm, etc.

## 3 Configuration de la connexion à MongoDB
3.1. Copier le fichier .env.example

Dans le dossier racine :

cp .env.example .env


Sous Windows PowerShell, vous pouvez faire :

Copy-Item .env.example .env

3.2. Modifier le fichier .env

Ouvrez .env dans votre éditeur et ajustez :

MONGODB_URI=mongodb://localhost:27017
DB_NAME=mini_compass
PORT=3000


MONGODB_URI

Si vous utilisez MongoDB local -> laissez mongodb://localhost:27017

Si vous utilisez MongoDB Atlas -> mettez l'URI fournie par Atlas.

DB_NAME

Nom de la base utilisée pour le projet (ex. mini_compass).

PORT

Port sur lequel l'API Express va écouter (par défaut 3000).

## 4. Lancer le serveur
### 4.1. Mode normal
```sh
npm start
```

Vous devriez voir :

✅ Connecté à MongoDB (base: mini_compass)
🚀 Serveur Express démarré sur http://localhost:3000

### 4.2. Mode développement (avec rechargement automatique)
```sh
npm run dev
```

Ce mode utilise nodemon pour redémarrer le serveur automatiquement
quand vous modifiez les fichiers.

## 5. Tester l’API avec un navigateur ou Postman

### 5.1. Tester la route de base

Dans votre navigateur, ouvrez :
```sh
http://localhost:3000/
```

Vous devriez voir :

> API Mini Compass - Express + MongoDB

### 5.2. Lister les collections

Si votre base contient déjà des collections, testez :

```sh
GET http://localhost:3000/collections
```

Exemple de réponse :
```json
{
  "collections": ["students", "courses", "orders"]
}
```

### 5.3. Voir les documents d’une collection

Par exemple, pour la collection students :
```sh
GET http://localhost:3000/collections/students/documents
```

Vous pouvez aussi limiter le nombre de documents :
```sh
GET http://localhost:3000/collections/students/documents?limit=20
```

### 5.4. Ajouter un document

Requête :

- Méthode : POST
- URL : http://localhost:3000/collections/students/documents
- Body (JSON), par exemple :
```json
{
  "name": "Alice",
  "program": "Tech Info",
  "age": 19
}
```

### 5.5. Supprimer un document

- Méthode : `DELETE`
- URL : `http://localhost:3000/collections/students/documents/<ID_DU_DOCUMENT>`

Remplacez `<ID_DU_DOCUMENT>` par la valeur du champ `_id` récupérée dans les autres réponses.

5.6. Modifier un document

- Méthode : `PUT`
- URL : `http://localhost:3000/collections/students/documents/<ID_DU_DOCUMENT>`
- Body : nouvel objet JSON complet qui remplacera l’ancien.

### 6. Et maintenant ? (pour le projet)

Votre tâche dans le cours sera de :

1. Laisser tourner cette API en arrière-plan.
2. Créer une application Java Swing qui :
   - affiche la liste des collections ;
   - affiche les documents dans une table ;
   - permet d’ajouter, modifier et supprimer des documents ;
   - utilise les routes définies dans ce serveur.

Vous n’avez pas besoin de modifier ce backend pour commencer.
Plus tard, vous pourrez éventuellement l’étendre (filtres, pagination, etc.) si le projet le demande