package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

import static java.lang.Thread.sleep;

public class AdminPanel {
    public JFrame frame = new JFrame("AdminPanel");
    public JPanel panel = new JPanel();
    public JPanel rightButtonPanel;
    public JScrollPane scrollPane;
    private JTable table;
    public JTextField queryField = new JTextField(20);
    private JPasswordField pwField = new JPasswordField(20);
    private JLabel label = new JLabel("Please log in", SwingConstants.CENTER);
    private JButton newQuery = new JButton("New Query");
    private JButton submitChange = new JButton("Yes");
    private JButton cancelChange = new JButton("No");
    private String query = "";
    private int height = 10;
    private int width = 5;
    private String[][] data;
    private String[] columnNames;
    private Connection clientDB;
    private int id;

    //Höhe und Breite des Bildschirms, um die Elemente an die Größe des Bildschirms anzupassen (Dynamisch)
    private int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    private int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

    Statement stmt;

    public AdminPanel() {
        frame.setSize(500, 500);
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setUndecorated(true);

        //Farben
        panel.setBackground(new Color(20, 20, 20));
        pwField.setBackground(new Color(20, 20, 20));
        pwField.setForeground(new Color(255, 255, 255));
        pwField.setCaretColor(new Color(255, 255, 255));
        queryField.setBackground(new Color(20, 20, 20));
        queryField.setForeground(new Color(255, 255, 255));
        queryField.setCaretColor(new Color(255, 255, 255));
        label.setForeground(new Color(255, 255, 255));
        label.setFont(new Font("Helvetica", Font.BOLD, 30));

        panel.add(label);

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        //Position für das Passwortfeld
        gbc.gridy = 1; // Zeile 1 (unter dem Label)
        gbc.insets = new Insets(10, (int) (screenWidth / 3), 10, (int) (screenWidth / 3)); //Abstand (oben, links, unten, rechts)
        gbc.weightx = 1.0;
        panel.add(pwField, gbc); // Passwortfeld hinzufügen

        pwField.addActionListener(e -> {
            clientDB = getConnection(pwField.getText());
            if (clientDB != null) {
                label.setText("Type SQL statement in here");
                panel.add(queryField, gbc);
                panel.remove(pwField);
                panel.repaint();
            } else {
                label.setText("Wrong password! Try again");
            }
            //Speichert das eingegeben Passwort
        });

        queryField.addActionListener(new ActionListener() { //Diese Methode wird überschrieben, da sie im Originalen (In der Klasse JTextField) keinen Inhalt im Methodenkörper hat
            @Override
            public void actionPerformed(ActionEvent e) { //Der Action Listener wird automatisch nur bei der Eingabe durch die Enter Taste aktiv

                query = queryField.getText();
                try {
                    assert clientDB != null; //Gibt einen Error (mit try catch abgefangen), wenn die Datenbank nicht existiert

                    //SQL Befehl wird ausgeführt
                    stmt = clientDB.createStatement();
                    stmt.executeQuery(query);
                    ResultSet rs = stmt.getResultSet();
                    ResultSetMetaData md = rs.getMetaData();

                    //Breite und Höhe der Tabelle wird anhand der Anzahl der ausgegebenen Daten festgesetzt
                    width = md.getColumnCount();
                    height = 0;
                    rs.beforeFirst();
                    while (rs.next()) {
                        height++;
                    }

                    //Ein zweidimensionales Array wird mit den Daten des ResultSets gefüllt. Außerdem werden die Attributsnamen in einem Array gespeichert
                    data = new String[height][width];

                    columnNames = new String[width];
                    for (int i = 0; i < width; i++) {
                        columnNames[i] = md.getColumnName(i + 1);
                    }

                    //Diese beiden Schleifen gehen alle Datenfelder durch (ResultSet ist immer eine Zeile, rowIndex gibt das abzuspeichernde Attribut (Spalte) an).
                    rs.beforeFirst();
                    int rowIndex = 0;
                    while (rs.next()) {
                        for (int i = 0; i < width; i++) {
                            data[rowIndex][i] = rs.getString(i + 1);
                        }
                        rowIndex++;
                    }

                    //Es wird eine Tabelle (JTable) erstellt, bei der eine Methode verändert wird, um festzulegen, dass man die Tabelle bearbeiten kann. Table
                    // Modelgit die Struktur der Tabelle an. Die Tabelle wird dabei automatisch mit dem zweidimensionalen String Array data gefüllt
                    DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return true;
                        }
                    };

                    table = new JTable(tableModel);
                    updateDatabase(tableModel, columnNames, md);

                    //Die Tabelle wird scrollable gemacht (Falls sie über den Bildschirm hinausgeht)
                    if (scrollPane != null) {
                        panel.remove(scrollPane);
                    }
                    scrollPane = new JScrollPane(table);
                    panel.setLayout(new BorderLayout());

                    //Farben für die Tabelle
                    table.setBackground(new Color(20, 20, 20));
                    table.setForeground(new Color(255, 255, 255));
                    scrollPane.getViewport().setBackground(new Color(20, 20, 20));

                    //Das Eingabefeld wird unsichtbar
                    panel.remove(label);
                    panel.remove(queryField);

                    //Button für einen neuen SQL Befehl
                    rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Rechts ausrichten
                    rightButtonPanel.setOpaque(false);
                    newQuery.setPreferredSize(new Dimension(150, 40));
                    rightButtonPanel.add(newQuery);
                    panel.add(rightButtonPanel, BorderLayout.SOUTH);

                    panel.add(scrollPane);
                    panel.revalidate();
                    panel.repaint();
                } catch (SQLException s) {
                    s.printStackTrace();
                }

            }
        });

        newQuery.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Entfernt die alte Tabelle
                if (scrollPane != null) {
                    panel.remove(scrollPane);
                }
                //Entfernt den Button
                panel.remove(rightButtonPanel);

                //Zeigt das Label wieder an
                panel.setLayout(new GridBagLayout());
                label.setText("Type SQL statement in here");
                GridBagConstraints gbcLabel = new GridBagConstraints();
                gbcLabel.gridy = 0;
                gbcLabel.insets = new Insets(10, 10, 10, 10);
                panel.add(label, gbcLabel);

                //Zeigt das Query-Eingabefeld erneut an
                GridBagConstraints gbcQuery = new GridBagConstraints();
                gbcQuery.gridy = 1;
                gbcQuery.weightx = 1.0;
                gbcQuery.insets = new Insets(10, 10, 10, 10);
                panel.add(queryField, gbcQuery);

                //Setze den Text im Query-Feld zurück
                queryField.setText("");

                // Aktualisiere das Panel
                panel.revalidate();
                panel.repaint();
            }
        });

        //Das Fenster wird aktualisiert und sichtbar gemacht
        frame.add(panel);
        frame.setVisible(true);
    }


    public void updateDatabase(DefaultTableModel tableModel, String[] columnNames, ResultSetMetaData md) {
        tableModel.addTableModelListener(e -> {

            //Das zu verändernde Feld wird mit row und column gespeichert
            int row = e.getFirstRow();
            int column = e.getColumn();

            //Die anfangs eingegebene Query wird verändert, indem das Select immer zu Select id wird. Das hat den Grund, dass in bestimmten Fällen die ID nicht im Datensatz vorhanden ist. So wird
            //sichergestellt, dass die ID immer an erster Stelle des ResultSets ist. Die ID wird danach abgespeichert.
            try {
                String shortenedQuery = "";
                if (query.contains("FROM")) {
                    shortenedQuery = query.substring(query.indexOf("FROM"));
                }
                stmt = clientDB.createStatement();
                stmt.executeQuery("SELECT id " + shortenedQuery + " ORDER BY id ASC");
                ResultSet trs = stmt.getResultSet();

                trs.first();
                for (int i = 0; i < row; i++) {
                    trs.next();
                }
                id = trs.getInt(1);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            //Neu eingegebener Datenwert und dessen Attribut wird abgefragt.
            final String newValue = (String) tableModel.getValueAt(row, column);
            final String attributeName = columnNames[column];

                //Dialog zum Bestätigen
            //Entfernt die alte Tabelle
            if (scrollPane != null) {
                panel.remove(scrollPane);
            }
            //Entfernt den Button
            panel.remove(rightButtonPanel);

            panel.setLayout(new GridBagLayout());
            label.setText("Do you want to update '" + attributeName + "' to '" + newValue + "'?");
            GridBagConstraints gbcLabel = new GridBagConstraints();
            gbcLabel.gridy = 0;
            gbcLabel.insets = new Insets(10, 10, 10, 10);
            panel.add(label, gbcLabel);

            //Erstelle ein Panel für die Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Buttons nebeneinander ausrichten
            buttonPanel.setOpaque(false);

            //Füge Buttons zum Panel hinzu
            buttonPanel.add(submitChange);
            buttonPanel.add(cancelChange);

            //Füge das Button-Panel ins Hauptpanel ein
            GridBagConstraints gbcButtons = new GridBagConstraints();
            gbcButtons.gridy = 1;
            gbcButtons.insets = new Insets(10, 10, 10, 10);
            panel.add(buttonPanel, gbcButtons);
            panel.revalidate();
            panel.repaint();

            //Entferne vorherige Listener, um Mehrfachausführung zu verhindern
            for (ActionListener al : submitChange.getActionListeners()) {
                submitChange.removeActionListener(al);
            }

            //Es wird geprüft, ob der Nutzer wirklich den Datenwert ändern will
            submitChange.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //Die Datenbank wird beim Datenwert der vorher abgefragten ID und dem Attribut um den neuen Wert aktualisiert.
                    try {
                        String tableName = md.getTableName(column + 1);
                        stmt = clientDB.createStatement();
                        stmt.execute("UPDATE " + tableName + " SET " + attributeName + " = '" + newValue + "' WHERE id = " + id);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                    reloadTable();
                }
            });

            cancelChange.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    reloadTable();
                }
            });

        });
    }

    //Nach einer Änderung der Datenbank kann mit dieser Methode die Tabelle erneut angezeigt werden (Mit den Änderungen)
    private void reloadTable() {
        panel.removeAll(); //Entfernt alle UI-Komponenten
        panel.setLayout(new BorderLayout());

        //Tabelle wieder hinzufügen
        scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        //Farben für die Tabelle
        table.setBackground(new Color(20, 20, 20));
        table.setForeground(new Color(255, 255, 255));
        scrollPane.getViewport().setBackground(new Color(20, 20, 20));

        //Button für eine neue Query erneut hinzufügen
        rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightButtonPanel.setOpaque(false);
        rightButtonPanel.add(newQuery);
        panel.add(rightButtonPanel, BorderLayout.SOUTH);

        panel.revalidate();
        panel.repaint();
    }

    //Das Frame wird immer neu gezeichnet (While Schleife in der Main.java), damit jede Änderung sofort aktualisiert und angezeigt wird.
    public void update() {
        frame.repaint();
    }

    //Die Verbindung zur Datenbank (Auf Tilo's Server) wird hergestellt
    public Connection getConnection(String pPW) {
        String url = "jdbc:mariadb://db.ontubs.de:3306/gambling";
        String user = "carl";
        String password = pPW;

        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
