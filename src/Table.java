import java.io.*;
import java.util.*;

public class Table {
    private String dbName;
    private String tableName;
    private List<String> columns;

    public Table(String dbName, String tableName, List<String> columns) {
        this.dbName = dbName;
        this.tableName = tableName;
        this.columns = columns;
    }

    public void create() throws IOException {
        // Create the table data file
        BufferedWriter writer = new BufferedWriter(new FileWriter(dbName + "_" + tableName + ".txt"));
        writer.write(String.join(",", columns));
        writer.newLine();
        writer.close();

        // Add the table metadata to the tables file
        updateTableList();
    }

    private void updateTableList() throws IOException {
        // Update tables list file with table name and columns
        BufferedWriter tablesWriter = new BufferedWriter(new FileWriter(dbName + "_tables.txt", true));
        tablesWriter.write(tableName + ":" + String.join(",", columns));
        tablesWriter.newLine();
        tablesWriter.close();

        // Update database file with table list
        BufferedWriter dbWriter = new BufferedWriter(new FileWriter(dbName + ".db", true));
        dbWriter.write(tableName);
        dbWriter.newLine();
        dbWriter.close();
    }

    public static List<String> listTables(String dbName) throws IOException {
        List<String> tables = new ArrayList<>();
        // Read from tables list file
        File tablesFile = new File(dbName + "_tables.txt");
        if (tablesFile.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(tablesFile));
            String line;
            while ((line = reader.readLine()) != null) {
                tables.add(line.split(":")[0]); // Extract only the table name
            }
            reader.close();
        }
        return tables;
    }

    public static List<String> getTableColumns(String dbName, String tableName) throws IOException {
        List<String> columns = new ArrayList<>();
        // Read from tables list file
        File tablesFile = new File(dbName + "_tables.txt");
        if (tablesFile.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(tablesFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 2 && parts[0].equals(tableName)) {
                    columns = Arrays.asList(parts[1].split(","));
                    break;
                }
            }
            reader.close();
        }
        return columns;
    }
}
