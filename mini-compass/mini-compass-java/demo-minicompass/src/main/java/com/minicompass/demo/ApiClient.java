package com.minicompass.demo;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Client Java de base pour communiquer avec l'API Mini Compass.
 *
 * Utilise java.net.http.HttpClient (Java 11+).
 *
 * Fournit des méthodes pour :
 * - GET /collections
 * - GET /collections/{name}/documents
 * - POST /collections/{name}/documents
 * - PUT /collections/{name}/documents/{id}
 * - DELETE /collections/{name}/documents/{id}
 * - PATCH /collections/{name}/documents/{id} (si implémenté côté API)
 *
 * Les réponses sont retournées sous forme de String (JSON brut).
 */
public class ApiClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private String databaseName;

    public ApiClient(String baseUrl) {
        // Exemple : "http://localhost:3000"
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.databaseName = null; 
    }

    /** Change la base MongoDB utilisée pour les prochaines requêtes. */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    // --------------------------------------------------------------------
    // 1. STRUCTURE DocumentSet (ensemble de documents)
    // --------------------------------------------------------------------

    public static class DocumentSet {
        private final String[] fieldNames;
        private final Object[][] documents;

        public DocumentSet(String[] fieldNames, Object[][] documents) {
            this.fieldNames = fieldNames;
            this.documents = documents;
        }

        public String[] getFieldNames() {
            return fieldNames;
        }

        public Object[][] getDocuments() {
            return documents;
        }
    }

    // --------------------------------------------------------------------
    // 2. MÉTHODES PUBLIQUES
    // --------------------------------------------------------------------

    /**
     * GET /collections
     */
    public String[] fetchCollections() throws IOException, InterruptedException {
        String path = applyDatabase(PathBuilder.collectionsPath());
        String jsonResponse = sendRequest("GET", path, null);

        return parseCollections(jsonResponse);
    }

    /**
     * GET /collections/{name}/documents?limit=...
     */
    public DocumentSet fetchDocuments(String collectionName, int limit) throws IOException, InterruptedException {
        String path = PathBuilder.documentsPath(collectionName) + PathBuilder.paginationQuery(limit, 0, databaseName);
        String jsonResponse = sendRequest("GET", path, null);

        return parseDocumentsToDocumentSet(jsonResponse);
    }

    /**
     * GET /collections/{name}/documents?limit=...&field=...&value=...
     */
    public DocumentSet fetchDocumentsByField(String collectionName, String field, String value, int limit) throws IOException, InterruptedException {
        String path = PathBuilder.documentsPath(collectionName) + PathBuilder.searchQuery(limit, field, value, databaseName);
        String jsonResponse = sendRequest("GET", path, null);

        return parseDocumentsToDocumentSet(jsonResponse);
    }

    /**
     * POST /collections/{name}/documents
     */
    public String insertDocument(String collectionName, Map<String, Object> fields) throws IOException, InterruptedException {
        String path = applyDatabase(PathBuilder.documentsPath(collectionName));
        String body = mapToJson(fields);

        String jsonResponse = sendRequest("POST", path, body);

        JSONObject root = new JSONObject(jsonResponse);

        if (root.has("error")) {
            return null;
        }
        return root.optString("insertedId", null);
    }

    /**
     * PUT /collections/{name}/documents/{id}
     */
    public boolean replaceDocument(String collectionName, String id, Map<String, Object> fields) throws IOException, InterruptedException {
        String path = applyDatabase(PathBuilder.singleDocumentPath(collectionName, id));
        String body = mapToJson(fields);

        String jsonResponse = sendRequest("PUT", path, body);
        JSONObject root = new JSONObject(jsonResponse);

        return !root.has("error");
    }

    /**
     * PATCH /collections/{name}/documents/{id}
     */
    public boolean updateDocument(String collectionName, String id, Map<String, Object> partialFields) throws IOException, InterruptedException {
        String path = applyDatabase(PathBuilder.singleDocumentPath(collectionName, id));
        String body = mapToJson(partialFields);

        String jsonResponse = sendRequest("PATCH", path, body);

        JSONObject root = new JSONObject(jsonResponse);
        return !root.has("error");
    }

    /**
     * DELETE /collections/{name}/documents/{id}
     */
    public boolean deleteDocument(String collectionName, String id) throws IOException, InterruptedException {
        String path = applyDatabase(PathBuilder.singleDocumentPath(collectionName, id));

        String jsonResponse = sendRequest("DELETE", path, null);

        JSONObject root = new JSONObject(jsonResponse);
        return !root.has("error");
    }

    public boolean existsDocument(String collectionName, String field, String value) throws IOException, InterruptedException {
        String path = PathBuilder.existsDocumentPath(collectionName) + PathBuilder.fieldQuery(field, value, databaseName);

        String jsonResponse = sendRequest("GET", path, null);

        JSONObject root = new JSONObject(jsonResponse);
        return root.optBoolean("exists", false);
    }

    public boolean hasDuplicateValue(String collectionName, String field, String value) throws IOException, InterruptedException {
        String path = PathBuilder.hasDuplicatePath(collectionName) + PathBuilder.fieldQuery(field, value, databaseName);

        String jsonResponse = sendRequest("GET", path, null);
        JSONObject root = new JSONObject(jsonResponse);

        return root.optBoolean("duplicate", false);
    }

    // ---------------------------------------------------------
    // Méthodes utilitaires
    // ---------------------------------------------------------

    /**
     * Ajoute ?db=... au path si un nom de base est défini.
     * À utiliser uniquement sur des paths sans query string (pas de "?").
     */
    private String applyDatabase(String path) {
        if (databaseName == null || databaseName.isBlank()) {
            return path;
        }
        Map<String, String> params = new LinkedHashMap<>();
        params.put("db", databaseName);
        return path + PathBuilder.buildQuery(params);
    }

    private String mapToJson(Map<String, Object> map) {
        if (map == null)
            return "{}";
        return new JSONObject(map).toString();
    }

    private String sendRequest(String method, String path, String jsonBody) throws IOException, InterruptedException {
        URI uri = URI.create(baseUrl + path);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json");

        if (jsonBody != null && !jsonBody.isEmpty()) {
            builder.header("Content-Type", "application/json; charset=UTF-8");
            builder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        HttpRequest request = builder.build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        int statusCode = response.statusCode();
        String body = response.body();

        // Logging simple pour le debug
        System.out.println("[" + method + "] " + uri);
        System.out.println("Status: " + statusCode);
        System.out.println("Body: " + body);
        System.out.println("---------------------------------");

        return body;
    }

    private String[] parseCollections(String collectionsJson) {
        if (collectionsJson == null || collectionsJson.isEmpty()) {
            return new String[0];
        }

        JSONObject root = new JSONObject(collectionsJson);
        JSONArray arr = root.optJSONArray("collections");
        if (arr == null) {
            return new String[0];
        }

        String[] result = new String[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            result[i] = arr.optString(i, "");
        }
        return result;
    }

    private DocumentSet parseDocumentsToDocumentSet(String documentsJson) {
        if (documentsJson == null || documentsJson.isEmpty()) {
            return new DocumentSet(new String[0], new Object[0][0]);
        }

        JSONObject root = new JSONObject(documentsJson);
        JSONArray docs = root.optJSONArray("documents");
        if (docs == null || docs.length() == 0) {
            return new DocumentSet(new String[0], new Object[0][0]);
        }

        JSONObject first = docs.getJSONObject(0);
        Set<String> keySet = first.keySet();

        // TIP: Si tu veux cacher _id dans l’affichage, tu peux filtrer ici.
        // keySet.remove("_id");

        String[] fieldNames = keySet.toArray(new String[0]);
        Object[][] documents = new Object[docs.length()][fieldNames.length];

        for (int i = 0; i < docs.length(); i++) {
            JSONObject doc = docs.getJSONObject(i);
            for (int j = 0; j < fieldNames.length; j++) {
                String field = fieldNames[j];
                Object value = doc.opt(field);

                if (value instanceof JSONObject || value instanceof JSONArray) {
                    value = value.toString();
                }
                documents[i][j] = value;
            }
        }

        return new DocumentSet(fieldNames, documents);
    }
}
