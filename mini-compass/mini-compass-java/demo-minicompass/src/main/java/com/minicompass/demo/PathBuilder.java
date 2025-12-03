package com.minicompass.demo;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class PathBuilder {
    /** /collections */
    public static String collectionsPath() {
        return "/collections";
    }

    /** /collections/{collection}/documents */
    public static String documentsPath(String collectionName) {
        return "/collections/" + encode(require(collectionName)) + "/documents";
    }

    /** /collections/{collection}/documents/{id} */
    public static String singleDocumentPath(String collectionName, String id) {
        return documentsPath(collectionName) + "/" + encode(require(id));
    }

    public static String existsDocumentPath(String collectionName) {
        return documentsPath(collectionName) + "/exists";
    }

    public static String hasDuplicatePath(String collectionName) {
        return documentsPath(collectionName) + "/has-duplicate";
    }

    // ------------------------------------------------------------
    // 2. QUERY BUILDERS explicites (recherche, pagination, etc.)
    // ------------------------------------------------------------

    public static String buildQuery(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder("?");
        boolean first = true;

        for (var entry : params.entrySet()) {
            if (entry.getValue() == null)
                continue;
            if (!first)
                sb.append("&");
            sb.append(encode(entry.getKey())).append("=").append(encode(entry.getValue()));
            first = false;
        }

        return sb.toString();
    }

    /** Génère ?limit=50&field=name&value=Alice */
    public static String searchQuery(int limit, String field, String value) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("limit", String.valueOf(limit));
        params.put("field", field);
        params.put("value", value);
        return buildQuery(params);
    }

    /** Génère ?limit=50&field=name&value=Alice + éventuellement db=... */
    public static String searchQuery(int limit, String field, String value, String databaseName) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("limit", String.valueOf(limit));
        params.put("field", field);
        params.put("value", value);
        return buildQuery(withDatabase(params, databaseName));
    }

    /** Génère ?limit=50&field=name&value=Alice */
    public static String fieldQuery(String field, String value) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("field", field);
        params.put("value", value);
        return buildQuery(params);
    }

    /** Génère ?field=name&value=Alice + éventuellement db=... */
    public static String fieldQuery(String field, String value, String databaseName) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("field", field);
        params.put("value", value);
        return buildQuery(withDatabase(params, databaseName));
    }

    /** Génère ?limit=50&skip=20 */
    public static String paginationQuery(int limit, int skip) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("limit", String.valueOf(limit));
        params.put("skip", String.valueOf(skip));
        return buildQuery(params);
    }

    /** Génère ?db=maBase&limit=50&skip=20 */
    public static String paginationQuery(int limit, int skip, String databaseName) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("limit", String.valueOf(limit));
        params.put("skip", String.valueOf(skip));
        return buildQuery(withDatabase(params, databaseName));
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    private static Map<String, String> withDatabase(Map<String, String> params, String databaseName) {
        // Ajoute db=... au besoin
        if (databaseName == null || databaseName.isBlank()) {
            return params;
        }
        Map<String, String> all = new LinkedHashMap<>();
        all.put("db", databaseName);
        if (params != null) {
            all.putAll(params);
        }
        return all;
    }

    private static String encode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String require(String s) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("Value must not be null or blank");
        }
        return s;
    }
}
