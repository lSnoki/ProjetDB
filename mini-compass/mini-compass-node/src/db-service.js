// ----------------------------------------------------------------------------
// Service responsable de communiquer avec MongoDB.
//
// Ce fichier utilise :
//   - getDb(dbName)  défini dans db.js
//   - le pilote officiel MongoDB (ObjectId, find, insertOne, etc.)
// ----------------------------------------------------------------------------

const { ObjectId } = require("mongodb");
const { getDb } = require("./db");

/**
 * Retourne la liste des collections d'une base.
 *
 * @param {string|null} dbName  Nom de la base MongoDB (ou null pour la base par défaut)
 * @returns {Promise<string[]>}
 */
async function listCollections(dbName = null) {
  const db = getDb(dbName);
  const collections = await db.listCollections().toArray();
  
  return collections.map((c) => c.name);
}

/**
 * Retourne les documents d'une collection.
 *
 * options :
 *  - databaseName : nom de la base MongoDB (ou null pour la base par défaut)
 *  - limit        : limite le nombre de documents (par défaut 50)
 *  - searchField  : filtrage simple sur un champ (ex: "name")
 *  - searchValue  : valeur à rechercher pour ce champ (ex: "Alice")
 *  - skip         : pagination (nombre de documents à ignorer)
 */
async function getDocuments(collectionName, options = {}) {
  const {
    databaseName = null,
    limit = 50,
    searchField = null,
    searchValue = null,
    skip = 0,
  } = options;

  const db = getDb(databaseName);

  let query = {};

  // TODO ÉTUDIANT 1 :
  // Si searchField ET searchValue sont fournis, filtrer les documents
  if (searchField && searchValue !== null && searchValue !== undefined) {
    let filtreValue = searchValue;
    // Convertir en nombre ou booléen
    if (!isNaN(searchValue)) {
      filtreValue = Number(searchValue);
    } else if (searchValue === "true" || searchValue === "false") {
      filtreValue = searchValue === "true";
    }

    query[searchField] = filtreValue;
  }

  let cursor = db.collection(collectionName).find(query);

  // TODO ÉTUDIANT 2 :
  // Utiliser "skip" pour ignorer un certain  nombre de documents
  // avant de commencer à les retourner.
    if (skip > 0) {
  cursor = cursor.skip(skip);
}

  const docs = await cursor.limit(limit).toArray();
  return docs;
}

/**
 * Trouve UN document qui correspond au filtre donné.
 *    
 * Exemples de filtre :
 *   { name: "Alice" }
 *   { codePermanent: "ABC1234" }
 *
 * @param {string} collectionName
 * @param {object} filter
 * @param {string|null} dbName
 * @returns {Promise<object|null>}
 */
async function findDocument(collectionName, filter, dbName = null) {
  const db = getDb(dbName);

  // TODO ÉTUDIANT 3 :
  // Utiliser findOne pour récupérer UN document qui correspond au filtre.
   if (typeof filter !== "object" || filter === null || Array.isArray(filter)) {
    throw new Error("Le filtre doit être un objet valide");
  }

  const findDocument = db.collection(collectionName);
  const document = await findDocument.findOne(filter);

  return document;
}

/**
 * Vérifie si AU MOINS UN document correspond au filtre donné.
 *
 * @param {string} collectionName
 * @param {object} filter
 * @param {string|null} dbName
 * @returns {Promise<boolean>}
 */
async function hasDocument(collectionName, filter, dbName = null) {
  // TODO ÉTUDIANT 4 :
  // Réutiliser findDocument pour savoir si un document existe.
  if (typeof filter !== "object" || filter === null || Array.isArray(filter)) {
  throw new Error("Le filtre doit être un objet valide");
  }

  const document = await findDocument(collectionName, filter, dbName);
      
  if (document) {
    return true;
  }
  return false;
}

/**
 * Vérifie si une valeur est présente PLUS D'UNE FOIS dans un champ donné.
 *
 * Exemple :
 *   hasDuplicate("students", "codePermanent", "ABC1234")
 *   → true si au moins 2 étudiants ont ce même codePermanent.
 *
 * @param {string} collectionName
 * @param {string} fieldName
 * @param {*} value
 * @param {string|null} dbName
 * @returns {Promise<boolean>} true si au moins 2 documents ont cette valeur
 */
async function hasDuplicate(collectionName, fieldName, value, dbName = null) {
  const db = getDb(dbName);

  // TODO ÉTUDIANT 5 :
  // Construire un filtre { [fieldName]: value } et compter
  // combien de documents correspondent.
  // Vérifier que fieldName est un string non vide
  if (typeof fieldName !== "string" || fieldName.trim() === "") {
    throw new Error("Le fieldName est un string vide");
  }

  // Convertir value au bon type (même logique que getDocuments)
  let filtreValue = value;
 
  if (!isNaN(value) && value !== "" && value !== null) {
    filtreValue = Number(value);
  } else if (value === "true" || value === "false") {
    filtreValue = value === "true";
  }

  // Construire le filtre
  const filter = {[fieldName]:filtreValue};

  // Compter combien de documents correspondent
  const count = await db.collection(collectionName).countDocuments(filter);

  return count;
}

/**
 * Insère un document dans une collection.
 *
 * @param {string} collectionName
 * @param {object} doc
 * @param {string|null} dbName
 * @returns {Promise<string>} _id inséré sous forme de chaîne
 */
async function insertDocument(collectionName, doc, dbName = null) {
  const db = getDb(dbName);

  // TODO ÉTUDIANT 6 : 
  // Utiliser insertOne pour insérer le document.
  if (typeof doc == "object" && Object.keys(doc).length !== 0){
    const insert = await db.collection(collectionName).insertOne(doc)
  }else if (typeof doc !== "object") {
    console.error("Erreur: Doit être un objet");
  
  }else if (Object.keys(doc).length == 0) {
  
    console.error("Erreur: Objet vide");
  }
  
  return null;
}

/**
 * Supprime un document par son _id.
 *
 * @param {string} collectionName
 * @param {string} id - valeur du champ _id sous forme de chaîne
 * @param {string|null} dbName
 * @returns {Promise<boolean>} true si un document a été supprimé
 */
async function deleteDocument(collectionName, id, dbName = null) {
  const db = getDb(dbName);

  // TODO ÉTUDIANT 7 : 
  const verifier = await db.collection(collectionName).findOne({_id: new ObjectId(id)})
  
  if (verifier == null) {
    throw new Error("Le document n'existe pas");
  
  }else if (ObjectId.isValid(id)) {
    try {
      const deleted = await db.collection(collectionName).deleteOne({_id: new ObjectId(id)})
    } catch (error) {
      console.error("Erreur", error);
    }
  }
  return false;
  }
/**
 * Remplace complètement un document.
 * ATTENTION : les anciens champs non présents dans newDoc seront perdus.
 *
 * @param {string} collectionName
 * @param {string} id
 * @param {object} newDoc
 * @param {string|null} dbName
 * @returns {Promise<boolean>} true si un document a été remplacé
 */
async function replaceDocument(collectionName, id, newDoc, dbName = null) {
  const db = getDb(dbName);

  // TODO ÉTUDIANT 8 : 
  // Utiliser replaceOne avec un filtre sur _id.

  const verifier = await db.collection(collectionName).findOne({_id: new ObjectId(id)})
  
  if (verifier == null) {
    throw new Error("Le document n'existe pas");
  
  }else if (ObjectId.isValid(id) && Object.keys(newDoc).length !== 0) {
    const replace = await db.collection(collectionName).replaceOne({_id: new ObjectId(id)}, newDoc)
  
  }else{
    throw new Error("Modification non effectué");
  }

  return false;
}

/**
 * Mise à jour partielle d'un document (style PATCH).
 *
 * Ne remplace PAS tout le document, ne met à jour que les champs envoyés.
 *
 * Exemple :
 *   updateDocument("students", "65a...", { age: 20 })
 *
 * @param {string} collectionName
 * @param {string} id
 * @param {object} partialDoc - champs à mettre à jour
 * @param {string|null} dbName
 * @returns {Promise<boolean>} true si un document a été mis à jour
 */
async function updateDocument(collectionName, id, partialDoc, dbName = null) {
  const db = getDb(dbName);

  // TODO ÉTUDIANT 9 :
  
    const verif = await db.collection(collectionName).findOne({_id: new ObjectId(id)})
    if (verif == null) {
      throw new Error("Le document n'existe pas");

    }else if (ObjectId.isValid(id) && Object.keys(partialDoc).length !== 0 && !(partialDoc.hasOwnProperty('_id'))) {
      const updt = await db.collection(collectionName).updateOne({_id: new ObjectId(id)}, {$set: partialDoc});

    }else {
      throw new Error("Modification non effectuée");
    }
}

// BONUS (optionnel) : joinDocuments pourrait être ajouté ici plus tard
// pour des opérations plus avancées (agrégations, $lookup, etc.)

module.exports = {
  listCollections,
  getDocuments,
  findDocument,
  hasDocument,
  hasDuplicate,
  insertDocument,
  deleteDocument,
  replaceDocument,
  updateDocument,
};
