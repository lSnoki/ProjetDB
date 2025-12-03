const express = require("express");
const cors = require("cors");
const bodyParser = require("body-parser");

const { connectToMongo } = require("./db");
const {
  getRouteParam,
  getQueryNumber,
  getQueryString,
  sendOk,
  sendCreated,
  sendError,
  asyncHandler,
} = require("./utils");

const {
  listCollections,
  getDocuments,
  findDocument,
  hasDocument,
  hasDuplicate,
  insertDocument,
  deleteDocument,
  replaceDocument,
  updateDocument,
} = require("./db-service");


// ----------------------------------------------------------------------------
// Configuration du serveur Express
// ----------------------------------------------------------------------------
const app = express();
const PORT = 3000;

app.use(cors());
app.use(bodyParser.json());

// ----------------------------------------------------------------------------
// ROUTES API
// ----------------------------------------------------------------------------

/**
 * GET /
 * → Page d’accueil (simple message)
 */
app.get("/", (req, res) => {
  res.send("API Mini Compass - Express + MongoDB");
});

/**
 * GET /collections?db=maBase
 * → Retourne la liste des collections de la base
 */
app.get(
  "/collections",
  asyncHandler(async (req, res) => {
    const dbName = getQueryString(req, "db", null);

    const collections = await listCollections(dbName);
    sendOk(res, { collections });
  })
);

/**
 * GET /collections/:name/documents
 * → Retourne les documents d’une collection
 */
app.get(
  "/collections/:name/documents",
  asyncHandler(async (req, res) => {
    const collectionName = getRouteParam(req, "name");

    const dbName = getQueryString(req, "db", null);
    const limit = getQueryNumber(req, "limit", 50);
    const skip = getQueryNumber(req, "skip", 0);
    const field = getQueryString(req, "field", null);
    const value = getQueryString(req, "value", null);

    const docs = await getDocuments(collectionName, {
      databaseName: dbName,
      limit,
      skip,
      searchField: field,
      searchValue: value,
    });

    sendOk(res, { documents: docs });
  })
);

/**
 * GET /collections/:name/documents/find?field=f&value=v&db=...
 * → Retourne un document correspondant
 */
app.get(
  "/collections/:name/documents/find",
  asyncHandler(async (req, res) => {
    const collectionName = getRouteParam(req, "name");

    const dbName = getQueryString(req, "db", null);
    const field = getQueryString(req, "field", null);
    const value = getQueryString(req, "value", null);

    if (!field || !value) {
      return sendError(res, "Paramètres 'field' et 'value' requis");
    }

    const result = await findDocument(collectionName, { [field]: value }, dbName);
    sendOk(res, { document: result });
  })
);

/**
 * GET /collections/:name/documents/exists?field=f&value=v&db=...
 */
app.get(
  "/collections/:name/documents/exists",
  asyncHandler(async (req, res) => {
    const collectionName = getRouteParam(req, "name");

    const dbName = getQueryString(req, "db", null);
    const field = getQueryString(req, "field", null);
    const value = getQueryString(req, "value", null);

    if (!field || !value) {
      return sendError(res, "Paramètres 'field' et 'value' requis");
    }

    const exists = await hasDocument(collectionName, { [field]: value }, dbName);
    sendOk(res, { exists });
  })
);

/**
 * GET /collections/:name/has-duplicate?field=f&value=v&db=...
 */
app.get(
  "/collections/:name/has-duplicate",
  asyncHandler(async (req, res) => {
    const collectionName = getRouteParam(req, "name");

    const dbName = getQueryString(req, "db", null);
    const field = getQueryString(req, "field", null);
    const value = getQueryString(req, "value", null);

    if (!field || !value) {
      return sendError(res, "Paramètres 'field' et 'value' requis");
    }

    const duplicate = await hasDuplicate(collectionName, field, value, dbName);
    sendOk(res, { duplicate });
  })
);

/**
 * POST /collections/:name/documents
 * → Insertion d’un document
 */
app.post(
  "/collections/:name/documents",
  asyncHandler(async (req, res) => {
    const collectionName = getRouteParam(req, "name");
    const dbName = getQueryString(req, "db", null);
    const doc = req.body;

    if (!doc || typeof doc !== "object") {
      return sendError(res, "Document invalide (JSON requis)");
    }

    const id = await insertDocument(collectionName, doc, dbName);
    sendCreated(res, { insertedId: id });
  })
);

/**
 * DELETE /collections/:name/documents/:id
 */
app.delete(
  "/collections/:name/documents/:id",
  asyncHandler(async (req, res) => {
    const collectionName = getRouteParam(req, "name");
    const id = getRouteParam(req, "id");
    const dbName = getQueryString(req, "db", null);

    const success = await deleteDocument(collectionName, id, dbName);
    sendOk(res, { success });
  })
);

/**
 * PUT /collections/:name/documents/:id
 * → Remplacement complet
 */
app.put(
  "/collections/:name/documents/:id",
  asyncHandler(async (req, res) => {
    const collectionName = getRouteParam(req, "name");
    const id = getRouteParam(req, "id");
    const dbName = getQueryString(req, "db", null);
    const newDoc = req.body;

    const success = await replaceDocument(collectionName, id, newDoc, dbName);
    sendOk(res, { success });
  })
);

/**
 * PATCH /collections/:name/documents/:id
 * → Mise à jour partielle
 */
app.patch(
  "/collections/:name/documents/:id",
  asyncHandler(async (req, res) => {
    const collectionName = getRouteParam(req, "name");
    const id = getRouteParam(req, "id");
    const dbName = getQueryString(req, "db", null);
    const partialDoc = req.body;

    const success = await updateDocument(collectionName, id, partialDoc, dbName);
    sendOk(res, { success });
  })
);

// ----------------------------------------------------------------------------
// Lancement du serveur après connexion à MongoDB
// ----------------------------------------------------------------------------
connectToMongo().then(() => {
  app.listen(PORT, () => {
    console.log(`Serveur Mini Compass en cours d'exécution sur http://localhost:${PORT}`);
  });
});
