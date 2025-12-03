package com.minicompass.demo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.io.IOException;

/**
 * Exemple minimal de fenêtre Swing utilisant MiniCompassApiClient.
 *
 * - À gauche : liste des collections (JList)
 * - À droite : zone de texte affichant les documents (JSON brut)
 *
 * TODO pour les étudiants :
 *  - Utiliser parseCollections(...) pour afficher proprement la liste.
 *  - Remplacer la JTextArea de droite par un JTable avec les champs des documents.
 */
public class SwingDemo extends JFrame {

    private final ApiClient apiClient;

    // Choix de la base de données
    private final JTextField dbNameField = new JTextField();
    private final JButton applyDbButton = new JButton("Utiliser cette base");
    private boolean databaseSelected = false;
    
    // UI : liste des collections
    private final DefaultListModel<String> collectionsModel = new DefaultListModel<>();
    private final JList<String> collectionsList = new JList<>(collectionsModel);

    // UI : tableau des documents
    private final DefaultTableModel documentsTableModel = new DefaultTableModel();
    private final JTable documentsTable = new JTable(documentsTableModel);

    // UI : filtrage simple champ = valeur
    private final JTextField fieldFilterText = new JTextField();
    private final JTextField valueFilterText = new JTextField();

    private final JButton refreshCollectionsButton = new JButton("Recharger les collections");
    private final JButton refreshDocumentsButton = new JButton("Recharger les documents");
    private final JButton filterButton = new JButton("Filtrer");
    private final JButton clearFilterButton = new JButton("Effacer le filtre");

    public SwingDemo() {
        this.apiClient = new ApiClient("http://localhost:3000");
        initUI();
        setMainUiEnabled(false);
    }

private void initUI() {
        setTitle("Mini Compass – Explorateur MongoDB");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // ------------------------------------------------------------------
        // Bandeau supérieur : choix de la base de données
        // ------------------------------------------------------------------
        JPanel dbPanel = new JPanel(new GridBagLayout());
        dbPanel.setBorder(BorderFactory.createTitledBorder("Connexion à la base MongoDB"));

        GridBagConstraints gbcDb = new GridBagConstraints();
        gbcDb.insets = new Insets(4, 4, 4, 4);
        gbcDb.fill = GridBagConstraints.HORIZONTAL;

        gbcDb.gridx = 0;
        gbcDb.gridy = 0;
        dbPanel.add(new JLabel("Nom de la base de données :"), gbcDb);

        gbcDb.gridx = 1;
        gbcDb.gridy = 0;
        gbcDb.weightx = 1.0;
        dbNameField.setText(""); // tu peux mettre une valeur par défaut si tu veux
        dbPanel.add(dbNameField, gbcDb);

        gbcDb.gridx = 2;
        gbcDb.gridy = 0;
        gbcDb.weightx = 0;
        dbPanel.add(applyDbButton, gbcDb);

        applyDbButton.addActionListener(e -> onApplyDatabase());

        // ------------------------------------------------------------------
        // Panel gauche : liste des collections
        // ------------------------------------------------------------------
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel collectionsLabel = new JLabel("Collections MongoDB");
        leftPanel.add(collectionsLabel, BorderLayout.NORTH);

        collectionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane collectionsScroll = new JScrollPane(collectionsList);
        leftPanel.add(collectionsScroll, BorderLayout.CENTER);

        refreshCollectionsButton.addActionListener(e -> {
            if (databaseSelected) {
                loadCollections();
            }
        });
        leftPanel.add(refreshCollectionsButton, BorderLayout.SOUTH);

        collectionsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && databaseSelected) {
                String selected = collectionsList.getSelectedValue();
                if (selected != null) {
                    loadDocuments(selected, false);
                }
            }
        });

        // ------------------------------------------------------------------
        // Panel haut-droit : filtrage champ = valeur
        // ------------------------------------------------------------------
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

        filterButton.addActionListener(e -> {
            if (!databaseSelected) {
                JOptionPane.showMessageDialog(
                        this,
                        "Veuillez d'abord choisir et confirmer une base de données.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }
            String selected = collectionsList.getSelectedValue();
            if (selected != null) {
                loadDocuments(selected, true);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Veuillez d'abord sélectionner une collection.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        clearFilterButton.addActionListener(e -> {
            fieldFilterText.setText("");
            valueFilterText.setText("");
            if (!databaseSelected) return;
            String selected = collectionsList.getSelectedValue();
            if (selected != null) {
                loadDocuments(selected, false);
            }
        });

        // ------------------------------------------------------------------
        // Panel bas-droit : documents
        // ------------------------------------------------------------------
        JPanel documentsPanel = new JPanel(new BorderLayout(5, 5));
        documentsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel documentsLabel = new JLabel("Documents de la collection sélectionnée");
        documentsPanel.add(documentsLabel, BorderLayout.NORTH);

        documentsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane documentsScroll = new JScrollPane(documentsTable);
        documentsPanel.add(documentsScroll, BorderLayout.CENTER);

        JPanel docsButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        docsButtonsPanel.add(refreshDocumentsButton);
        documentsPanel.add(docsButtonsPanel, BorderLayout.SOUTH);

        refreshDocumentsButton.addActionListener(e -> {
            if (!databaseSelected) {
                JOptionPane.showMessageDialog(
                        this,
                        "Veuillez d'abord choisir et confirmer une base de données.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }
            String selected = collectionsList.getSelectedValue();
            if (selected != null) {
                loadDocuments(selected, false);
            }
        });

        // ------------------------------------------------------------------
        // Panel droit : filtre + documents
        // ------------------------------------------------------------------
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.add(filterPanel, BorderLayout.NORTH);
        rightPanel.add(documentsPanel, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                rightPanel
        );
        splitPane.setDividerLocation(250);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(dbPanel, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Gère le clic sur "Utiliser cette base".
     * - demande confirmation
     * - configure le client
     * - active le reste de l'UI
     * - charge les collections
     */
    private void onApplyDatabase() {
        String dbName = dbNameField.getText().trim();

        if (dbName.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Aucun nom de base n'est renseigné.\n" +
                    "Souhaitez-vous utiliser la base par défaut configurée côté serveur ?",
                    "Confirmer la base de données",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        } else {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Vous allez utiliser la base MongoDB : \"" + dbName + "\".\n\n" +
                    "Continuer ?",
                    "Confirmer la base de données",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // Appliquer le choix côté client
        apiClient.setDatabaseName(dbName.isEmpty() ? null : dbName);

        // Activer le reste de l’UI
        databaseSelected = true;
        setMainUiEnabled(true);

        // Charger les collections de cette base
        loadCollections();
    }

    /**
     * Active / désactive tous les éléments de l'UI sauf le choix de base de données.
     */
    private void setMainUiEnabled(boolean enabled) {
        collectionsList.setEnabled(enabled);
        refreshCollectionsButton.setEnabled(enabled);

        fieldFilterText.setEnabled(enabled);
        valueFilterText.setEnabled(enabled);
        filterButton.setEnabled(enabled);
        clearFilterButton.setEnabled(enabled);

        documentsTable.setEnabled(enabled);
        refreshDocumentsButton.setEnabled(enabled);
    }

    // ------------------------------------------------------------------
    // Chargement des collections et des documents
    // ------------------------------------------------------------------

    private void loadCollections() {
        collectionsModel.clear();
        documentsTableModel.setRowCount(0);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private String[] collections;

            @Override
            protected Void doInBackground() {
                try {
                    collections = apiClient.fetchCollections();
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
                            "Erreur lors de la récupération des collections.\n" +
                            "Vérifiez le nom de la base de données ou le serveur.",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                for (String name : collections) {
                    collectionsModel.addElement(name);
                }
            }
        };

        worker.execute();
    }

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
                        documentSet = apiClient.fetchDocumentsByField(
                                collectionName, field, value, 50
                        );
                    } else {
                        documentSet = apiClient.fetchDocuments(collectionName, 50);
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
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                String[] fieldNames = documentSet.getFieldNames();
                Object[][] docs = documentSet.getDocuments();
                documentsTableModel.setDataVector(docs, fieldNames);
            }
        };

        worker.execute();
    }
}
