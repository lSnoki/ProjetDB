const { MongoClient } = require("mongodb");

const MONGO_URL = "mongodb://127.0.0.1:27017";
const DEFAULT_DB_NAME = "cegep_bd1";

let client;

async function connectToMongo() {
  if (!client) {
    client = new MongoClient(MONGO_URL);
    await client.connect();
    console.log("Connecté à MongoDB");
  }
  return client;
}

/**
 * Retourne une référence vers une base de données.
 * Si dbName est nul, on utilise la base par défaut.
 */
function getDb(dbName) {
  if (!client) {
    throw new Error("MongoDB non connecté. Appeler connectToMongo() d'abord.");
  }
  const name = dbName || DEFAULT_DB_NAME;
  return client.db(name);
}

module.exports = {
  connectToMongo,
  getDb,
};
