package Admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class AdminPanel {
    public JFrame frame = new JFrame("AdminPanel");
    public JPanel panel = new JPanel();
    public JScrollPane scrollPane;
    public JTextField textField = new JTextField(20);
    private String query = "";
    private int height = 10;
    private int width = 5;
    private JTable table;
    private String[][] data;
    private String[] columnNames;
    private Connection clientDB = getConnection();


    Statement stmt;
    Connection conn;

    public AdminPanel() {
        frame.setSize(500, 500);
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        panel.setLayout(new BorderLayout());
        panel.add(textField, BorderLayout.SOUTH);

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                query = textField.getText();
                System.out.println("Eingegebener Text: " + query);

                try {
                    assert clientDB != null;
                    stmt = clientDB.createStatement();



                    //Eingegebener SQL Befehl wird ausgeführt und im Resultset gespeichert
                    stmt.executeQuery(query);
                    ResultSet rs = stmt.getResultSet();
                    ResultSetMetaData md = rs.getMetaData();

                    //Anzahl an Spalten (Attributen) wird ermittelt
                    width = md.getColumnCount();

                    //Anzahl an Zeilen (Count(*)) wird ermittelt
                    height = 0;
                    while(rs.next()) height ++;
                    System.out.println(height);

                    // Tabelle wird mit Daten gefüllt
                    data = new String[height][width];
                    columnNames = new String[width];

                    // Spaltennamen aus dem ResultSetMetaData abrufen
                    for (int i = 0; i < width; i++) {
                        columnNames[i] = md.getColumnName(i + 1); // Spaltennamen beginnen bei 1
                    }

                    // Daten aus dem ResultSet abrufen
                    rs.beforeFirst();
                    int rowIndex = 0;
                    while (rs.next()) {
                        for (int i = 0; i < width; i++) {
                            data[rowIndex][i] = rs.getString(i + 1);
                            System.out.println(rs.getString(i + 1));
                        }
                        rowIndex++;
                    }

                    // Tabelle nach der Abfrage aktualisieren
                    DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return true; // Alle Zellen sind bearbeitbar
                        }
                    };

                    table = new JTable(tableModel);
                    updateDatabase(tableModel, columnNames, md);

                    //Neue Tabelle dem ScrollPane hinzufügen
                    if (scrollPane != null) {
                        panel.remove(scrollPane);
                    }
                    scrollPane = new JScrollPane(table);
                    panel.add(scrollPane, BorderLayout.CENTER);

                    // Aktualisiere das Panel
                    update();
                    panel.revalidate();
                    panel.repaint();
                } catch (SQLException s) {
                    s.printStackTrace();
                }
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    public void updateDatabase(DefaultTableModel tableModel, String[] columnNames, ResultSetMetaData md) {
        tableModel.addTableModelListener(e -> {
            int row = e.getFirstRow(); // Die geänderte Zeile
            int column = e.getColumn(); // Die geänderte Spalte

            // Hole den neuen Wert aus dem Modell
            String newValue = (String) tableModel.getValueAt(row, column);

            // Optional: Aktualisiere ein Attribut oder schreibe den Wert in die Datenbank
            System.out.println("Neue Eingabe: " + newValue + " in Zeile " + row + ", Spalte " + column);

            //Der Attributname und die Tabelle, in der dieses Attribut gespeichert wird, wird herausgesucht
            String attributeName = columnNames[column];
            try {
                String tableName = md.getTableName(column);

                try {
                    stmt = clientDB.createStatement();
                    stmt.executeQuery("UPDATE " + tableName + " SET " + attributeName + "=" + newValue + "WHERE " + "id = " + data[row][0]);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            assert clientDB != null;


        });
    }

    public void update() {
        frame.repaint();
    }

    public Connection getConnection() {
        String url = "jdbc:mariadb://db.ontubs.de:3306/gambling";
        String user = "carl";
        String password = "geilo123!";

        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("Verbindung zur Datenbank erfolgreich!");
            return connection;
        } catch (SQLException e) {
            System.err.println("Datenbankverbindung fehlgeschlagen!");
            e.printStackTrace();
            return null;
        }
    }
}
