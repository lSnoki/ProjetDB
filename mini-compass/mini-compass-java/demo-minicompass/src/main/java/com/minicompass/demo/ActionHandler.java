package com.minicompass.demo;

import java.io.IOException;
import java.util.Map;

import com.minicompass.demo.ApiClient.DocumentSet;

/**
 * Classe responsable de gérer les actions "métier" déclenchées par l'interface Swing.
 *
 * Elle encapsule tous les appels à ApiClient.
 */
public class ActionHandler {

    private final ApiClient apiClient;

    public ActionHandler(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    // -----------------------------------------------------------------------
    // Connexion à la base de données
    // -----------------------------------------------------------------------

    /**
     * Applique le choix de la base de données.
     * Si dbName est vide ou null, on utilise la base par défaut côté serveur.
     */
    public void handleApplyDatabase(String dbName) {
        String dbToUse = (dbName == null || dbName.isBlank()) ? null : dbName.trim();
        apiClient.setDatabaseName(dbToUse);
    }

    /**
     * Récupère la liste des collections pour la base de données courante.
     */
    public String[] handleFetchCollections() throws IOException, InterruptedException {
        return apiClient.fetchCollections();
    }

    // -----------------------------------------------------------------------
    // Lecture de documents
    // -----------------------------------------------------------------------

    /**
     * Récupère les documents d'une collection, sans filtre.
     */
    public DocumentSet handleFetch(String collectionName)
            throws IOException, InterruptedException {

        return apiClient.fetchDocuments(collectionName, 50);
    }

    /**
     * TODO #1 : Filtrer les documents d'une collection.
     *
     * Utiliser ApiClient pour récupérer uniquement les documents
     * dont le champ "field" a la valeur "value".
     *
     * Utiliser :
     *   return apiClient.fetchDocumentsByField(...)
     */
    public DocumentSet handleFilter(String collectionName, String field, String value)
            throws IOException, InterruptedException {

        return null; // temporaire
    }

    // -----------------------------------------------------------------------
    // CRUD
    // -----------------------------------------------------------------------

    /**
     * TODO #2 : Insérer un document dans une collection.
     *
     * newDoc contient quelques champs (clé -> valeur) saisis dans l'interface.
     * Utiliser :
     *   String id = apiClient.insertDocument(...);
     *   return (id != null);
     */
    public boolean handleInsert(String collectionName, Map<String, Object> newDoc)
            throws IOException, InterruptedException {
                
        return false; // temporaire
    }

    /**
     * TODO #3 : Modifier un document existant.
     *
     * On vous donne l'_id du document et les nouvelles valeurs (editedDoc).
     * Deux options acceptées :
     *  - remplacement complet (PUT) via replaceDocument(...)
     *  - ou mise à jour partielle (PATCH) via updateDocument(...)
     *
     * Utiliser (par exemple) :
     *   return apiClient.replaceDocument(...);
     */
    public boolean handleUpdate(String collectionName, String id, Map<String, Object> editedDoc)
            throws IOException, InterruptedException {

        return false; // temporaire
    }

    /**
     * TODO #4 : Supprimer un document.
     *
     * On vous donne l'_id du document.
     * Utiliser :
     *   return apiClient.deleteDocument(...);
     */
    public boolean handleDelete(String collectionName, String id)
            throws IOException, InterruptedException {

        return false; // temporaire
    }
}
