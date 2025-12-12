package com.minicompass.demo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SwingDemo extends JFrame {

    final ApiClient apiClient;
    final ActionHandler actionHandler;

    // UI – choix de la base de données
    private final JTextField dbNameField = new JTextField();
    private final JButton applyDbButton = new JButton("Utiliser cette base");

    // UI – liste des collections
    private final DefaultListModel<String> collectionsModel = new DefaultListModel<>();
    private final JList<String> collectionsList = new JList<>(collectionsModel);

    // UI – tableau des documents
    private final DefaultTableModel documentsTableModel = new DefaultTableModel();
    private final JTable documentsTable = new JTable(documentsTableModel);

    // UI – filtre champ = valeur
    private final JTextField fieldFilterText = new JTextField();
    private final JTextField valueFilterText = new JTextField();
    private final JButton filterButton = new JButton("Filtrer");
    private final JButton clearFilterButton = new JButton("Effacer le filtre");

    // UI – boutons CRUD
    private final JButton refreshCollectionsButton = new JButton("Recharger les collections");
    private final JButton refreshDocumentsButton = new JButton("Recharger les documents");
    private final JButton addButton = new JButton("Ajouter");
    private final JButton editButton = new JButton("Modifier");
    private final JButton deleteButton = new JButton("Supprimer");

    private boolean databaseSelected = false;

    public SwingDemo() {
        this.apiClient = new ApiClient("http://localhost:3000");
        this.actionHandler = new ActionHandler(apiClient);

        initUI();
        setMainUiEnabled(false);
    }

    private void initUI() {
        setTitle("Mini Compass – Explorateur MongoDB");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);

        // ----------------- Bandeau choix de la BD -----------------
        JPanel dbPanel = new JPanel(new GridBagLayout());
        dbPanel.setBorder(BorderFactory.createTitledBorder("Connexion à une base MongoDB"));

        GridBagConstraints gbcDb = new GridBagConstraints();
        gbcDb.insets = new Insets(4, 4, 4, 4);
        gbcDb.fill = GridBagConstraints.HORIZONTAL;

        gbcDb.gridx = 0;
        gbcDb.gridy = 0;
        dbPanel.add(new JLabel("Nom de la base de données :"), gbcDb);

        gbcDb.gridx = 1;
        gbcDb.gridy = 0;
        gbcDb.weightx = 1.0;
        dbNameField.setText("");
        dbPanel.add(dbNameField, gbcDb);

        gbcDb.gridx = 2;
        gbcDb.gridy = 0;
        gbcDb.weightx = 0;
        dbPanel.add(applyDbButton, gbcDb);

        applyDbButton.addActionListener(e -> onApplyDatabase());

        // ----------------- Panneau gauche (collections) -----------------
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel collectionsLabel = new JLabel("Collections MongoDB");
        leftPanel.add(collectionsLabel, BorderLayout.NORTH);

        collectionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leftPanel.add(new JScrollPane(collectionsList), BorderLayout.CENTER);

        refreshCollectionsButton.addActionListener(e -> loadCollections());
        leftPanel.add(refreshCollectionsButton, BorderLayout.SOUTH);

        collectionsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && databaseSelected) {
                String selected = collectionsList.getSelectedValue();
                if (selected != null) {
                    loadDocuments(selected, false);
                }
            }
        });

        // ----------------- Panneau filtre -----------------
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtre (champ = valeur)"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        filterPanel.add(new JLabel("Champ :"), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        filterPanel.add(fieldFilterText, gbc);

        row++;
        gbc.weightx = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        filterPanel.add(new JLabel("Valeur :"), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        filterPanel.add(valueFilterText, gbc);

        row++;
        gbc.weightx = 0;

        JPanel filterButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterButtonsPanel.add(filterButton);
        filterButtonsPanel.add(clearFilterButton);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        filterPanel.add(filterButtonsPanel, gbc);

        filterButton.addActionListener(e -> onFilter());
        clearFilterButton.addActionListener(e -> onClearFilter());

        // ----------------- Panneau documents + CRUD -----------------
        JPanel documentsPanel = new JPanel(new BorderLayout(5, 5));
        documentsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel documentsLabel = new JLabel("Documents de la collection sélectionnée");
        documentsPanel.add(documentsLabel, BorderLayout.NORTH);

        documentsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        documentsPanel.add(new JScrollPane(documentsTable), BorderLayout.CENTER);

        JPanel docsButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        docsButtonsPanel.add(refreshDocumentsButton);
        docsButtonsPanel.add(addButton);
        docsButtonsPanel.add(editButton);
        docsButtonsPanel.add(deleteButton);
        documentsPanel.add(docsButtonsPanel, BorderLayout.SOUTH);

        refreshDocumentsButton.addActionListener(e -> onRefreshDocuments());
        addButton.addActionListener(e -> onAddDocument());
        editButton.addActionListener(e -> onEditDocument(true));
        deleteButton.addActionListener(e -> onDeleteDocument());

        // ----------------- Panneau droit -----------------
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.add(filterPanel, BorderLayout.NORTH);
        rightPanel.add(documentsPanel, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                rightPanel);
        splitPane.setDividerLocation(260);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(dbPanel, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);
    }

    // ---------------------------------------------------------------------
    // Gestion de la base de données
    // ---------------------------------------------------------------------

    private void onApplyDatabase() {
        String dbName = dbNameField.getText().trim();

        int choice = JOptionPane.showConfirmDialog(
                this,
                (dbName.isEmpty()
                        ? "Aucune base spécifiée.\nUtiliser la base par défaut côté serveur ?"
                        : "Vous allez utiliser la base MongoDB : \"" + dbName + "\".\n\nContinuer ?"),
                "Confirmer la base de données",
                JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        actionHandler.handleApplyDatabase(dbName);
        databaseSelected = true;
        setMainUiEnabled(true);
        loadCollections();
    }

    private void setMainUiEnabled(boolean enabled) {
        collectionsList.setEnabled(enabled);
        refreshCollectionsButton.setEnabled(enabled);

        fieldFilterText.setEnabled(enabled);
        valueFilterText.setEnabled(enabled);
        filterButton.setEnabled(enabled);
        clearFilterButton.setEnabled(enabled);

        documentsTable.setEnabled(enabled);
        refreshDocumentsButton.setEnabled(enabled);
        addButton.setEnabled(enabled);
        editButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
    }

    // ---------------------------------------------------------------------
    // Chargement des collections
    // ---------------------------------------------------------------------

    private void loadCollections() {
        if (!databaseSelected) {
            JOptionPane.showMessageDialog(
                    this,
                    "Veuillez d'abord choisir une base de données.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        collectionsModel.clear();
        documentsTableModel.setRowCount(0);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private String[] collections;

            @Override
            protected Void doInBackground() {
                try {
                    collections = actionHandler.handleFetchCollections();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    collections = null;
                }
                return null;
            }

            @Override
            protected void done() {
                if (collections == null) {
                    JOptionPane.showMessageDialog(
                            SwingDemo.this,
                            "Erreur lors de la récupération des collections.\nVérifiez la base ou le serveur.",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                for (String name : collections) {
                    collectionsModel.addElement(name);
                }
            }
        };

        worker.execute();
    }

    // ---------------------------------------------------------------------
    // Chargement des documents
    // ---------------------------------------------------------------------

    private void loadDocuments(String collectionName, boolean useFilter) {
        documentsTableModel.setRowCount(0);

        final String field = fieldFilterText.getText().trim();
        final String value = valueFilterText.getText().trim();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private ApiClient.DocumentSet documentSet;

            @Override
            protected Void doInBackground() {
                try {
                    if (useFilter && !field.isEmpty() && !value.isEmpty()) {
                        documentSet = actionHandler.handleFilter(collectionName, field, value);
                    } else {
                        documentSet = actionHandler.handleFetch(collectionName);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    documentSet = null;
                }
                return null;
            }

            @Override
            protected void done() {
                if (documentSet == null) {
                    JOptionPane.showMessageDialog(
                            SwingDemo.this,
                            "Erreur lors de la récupération des documents.",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String[] fieldNames = documentSet.getFieldNames();
                Object[][] docs = documentSet.getDocuments();
                documentsTableModel.setDataVector(docs, fieldNames);
            }
        };

        worker.execute();
    }

    // ---------------------------------------------------------------------
    // Boutons de filtre / refresh
    // ---------------------------------------------------------------------

    private void onFilter() {
        if (!databaseSelected) {
            JOptionPane.showMessageDialog(
                    this,
                    "Veuillez d'abord choisir une base de données.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String selected = collectionsList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Veuillez d'abord sélectionner une collection.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        loadDocuments(selected, true);
    }

    private void onClearFilter() {
        fieldFilterText.setText("");
        valueFilterText.setText("");

        String selected = collectionsList.getSelectedValue();
        if (selected != null && databaseSelected) {
            loadDocuments(selected, false);
        }
    }

    private void onRefreshDocuments() {
        String selected = collectionsList.getSelectedValue();
        if (selected != null && databaseSelected) {
            loadDocuments(selected, false);
        }
    }

    // ---------------------------------------------------------------------
    // Actions CRUD
    // ---------------------------------------------------------------------

    private void onAddDocument() {
        if (!ensureCollectionSelected())
            return;

        String collectionName = collectionsList.getSelectedValue();
        Map<String, Object> newDoc = promptForDocumentFields(null);
        if (newDoc == null || newDoc.isEmpty()) {
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private boolean success = false;

            @Override
            protected Void doInBackground() {
                try {
                    success = actionHandler.handleInsert(collectionName, newDoc);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    success = false;
                }
                return null;
            }

            @Override
            protected void done() {
                if (success) {
                    JOptionPane.showMessageDialog(
                            SwingDemo.this,
                            "Document ajouté avec succès.",
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadDocuments(collectionName, false);
                } else {
                    JOptionPane.showMessageDialog(
                            SwingDemo.this,
                            "Erreur lors de l'ajout du document.",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void onEditDocument(boolean useReplace) {
        if (!ensureCollectionSelected())
            return;
        int row = documentsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Veuillez sélectionner un document à modifier.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String collectionName = collectionsList.getSelectedValue();
        String id = getSelectedDocumentId(row);
        if (id == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Impossible de trouver le champ _id du document sélectionné.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<String, Object> currentValues = getRowAsMap(row);
        Map<String, Object> editedDoc = promptForDocumentFields(currentValues);
        if (editedDoc == null || editedDoc.isEmpty()) {
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private boolean success = false;

            @Override
            protected Void doInBackground() {
                try {
                    success = actionHandler.handleUpdate(collectionName, id, editedDoc);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    success = false;
                }
                return null;
            }

            @Override
            protected void done() {
                if (success) {
                    JOptionPane.showMessageDialog(
                            SwingDemo.this,
                            "Document modifié avec succès.",
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadDocuments(collectionName, false);
                } else {
                    JOptionPane.showMessageDialog(
                            SwingDemo.this,
                            "Erreur lors de la modification du document.",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void onDeleteDocument() {
        if (!ensureCollectionSelected())
            return;
        int row = documentsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Veuillez sélectionner un document à supprimer.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String collectionName = collectionsList.getSelectedValue();
        String id = getSelectedDocumentId(row);
        if (id == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Impossible de trouver le champ _id du document sélectionné.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Supprimer le document avec _id = " + id + " ?",
                "Confirmer la suppression",
                JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION)
            return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private boolean success = false;

            @Override
            protected Void doInBackground() {
                try {
                    success = actionHandler.handleDelete(collectionName, id);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    success = false;
                }
                return null;
            }

            @Override
            protected void done() {
                if (success) {
                    JOptionPane.showMessageDialog(
                            SwingDemo.this,
                            "Document supprimé avec succès.",
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadDocuments(collectionName, false);
                } else {
                    JOptionPane.showMessageDialog(
                            SwingDemo.this,
                            "Erreur lors de la suppression du document.",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private boolean ensureCollectionSelected() {
        if (!databaseSelected) {
            JOptionPane.showMessageDialog(
                    this,
                    "Veuillez d'abord choisir une base de données.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        if (collectionsList.getSelectedValue() == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Veuillez d'abord sélectionner une collection.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
    }

    private String getSelectedDocumentId(int row) {
        int idCol = documentsTableModel.findColumn("_id");
        if (idCol < 0)
            return null;
        Object value = documentsTableModel.getValueAt(row, idCol);
        return value != null ? value.toString() : null;
    }

    private Map<String, Object> getRowAsMap(int row) {
        Map<String, Object> map = new LinkedHashMap<>();
        int colCount = documentsTableModel.getColumnCount();
        for (int col = 0; col < colCount; col++) {
            String fieldName = documentsTableModel.getColumnName(col);
            Object value = documentsTableModel.getValueAt(row, col);
            map.put(fieldName, value);
        }
        return map;
    }

    private Map<String, Object> promptForDocumentFields(Map<String, Object> initialValues) {
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));

        java.util.List<JTextField[]> rows = new java.util.ArrayList<>();

        // Helper pour ajouter une ligne (champ + valeur)
        Runnable addRow = () -> {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JTextField fieldNameField = new JTextField(12);
            JTextField valueField = new JTextField(12);

            row.add(new JLabel("Champ :"));
            row.add(fieldNameField);
            row.add(new JLabel("Valeur :"));
            row.add(valueField);

            rows.add(new JTextField[] { fieldNameField, valueField });
            fieldsPanel.add(row);
        };

        // Pré-remplir à partir des valeurs initiales (si modification)
        if (initialValues != null) {
            for (var entry : initialValues.entrySet()) {
                String key = entry.getKey();
                if ("_id".equals(key))
                    continue; // ne pas éditer l'id

                addRow.run();
                JTextField[] last = rows.get(rows.size() - 1);
                last[0].setText(key);
                last[1].setText(entry.getValue() != null ? entry.getValue().toString() : "");
            }
        }

        // Ajouter au moins une ligne vide
        if (rows.isEmpty()) {
            addRow.run();
        }

        // Bouton "Ajouter un champ"
        JButton addFieldButton = new JButton("Ajouter un champ");
        addFieldButton.addActionListener(e -> {
            addRow.run();
            fieldsPanel.revalidate();
            fieldsPanel.repaint();
        });

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.add(new JScrollPane(fieldsPanel), BorderLayout.CENTER);
        mainPanel.add(addFieldButton, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(
                this,
                mainPanel,
                "Éditer un document",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        Map<String, Object> map = new LinkedHashMap<>();
        for (JTextField[] row : rows) {
            String fieldName = row[0].getText().trim();
            String value = row[1].getText().trim();

            if (!fieldName.isEmpty()) {
                map.put(fieldName, value);
            }
        }

        return map;
    }
}
