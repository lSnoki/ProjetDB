/**
 * Récupère un paramètre de route (ex: /collections/:name).
 */
function getRouteParam(req, name, defaultValue = undefined) {
  if (req.params && req.params[name] !== undefined) {
    return req.params[name];
  }
  return defaultValue;
}

/**
 * Récupère un paramètre de query string sous forme de string.
 * Exemple: GET /collections?limit=20
 */
function getQueryString(req, name, defaultValue = undefined) {
  if (req.query && req.query[name] !== undefined) {
    return String(req.query[name]);
  }
  return defaultValue;
}

/**
 * Récupère un paramètre de query string sous forme de nombre.
 * Retourne defaultValue si absent ou invalide.
 */
function getQueryNumber(req, name, defaultValue = undefined) {
  const value = getQueryString(req, name, undefined);
  if (value === undefined) {
    return defaultValue;
  }
  const n = Number(value);
  if (Number.isNaN(n)) {
    return defaultValue;
  }
  return n;
}

/**
 * Envoie une réponse JSON avec un statut donné.
 */
function sendJson(res, statusCode, payload) {
  res.status(statusCode).json(payload);
}

/**
 * Réponse standard 200 OK.
 */
function sendOk(res, payload) {
  sendJson(res, 200, payload);
}

/**
 * Réponse standard 201 Created.
 */
function sendCreated(res, payload) {
  sendJson(res, 201, payload);
}

/**
 * Réponse standard 204 No Content.
 */
function sendNoContent(res) {
  res.status(204).send();
}

/**
 * Envoie une erreur structurée.
 * Toujours le même format: { error: "message lisible" }
 */
function sendError(res, statusCode, message) {
  sendJson(res, statusCode, { error: message });
}

/**
 * Petit helper pour gérer les handlers async sans
 * recopier try/catch partout.
 *
 * Exemple:
 * app.get("/...", asyncHandler(async (req, res) => {
 *   // ton code ici (throw si erreur)
 * }));
 */
function asyncHandler(fn) {
  return (req, res, next) => {
    Promise.resolve(fn(req, res, next)).catch((err) => {
      console.error("Erreur route:", err);
      // Si une erreur non gérée remonte, on envoie une 500
      sendError(res, 500, "Erreur serveur interne");
    });
  };
}

module.exports = {
  getRouteParam,
  getQueryString,
  getQueryNumber,
  sendJson,
  sendOk,
  sendCreated,
  sendNoContent,
  sendError,
  asyncHandler,
};
