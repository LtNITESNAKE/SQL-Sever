import java.io.*;
import java.sql.*;
import java.util.*;

public class Query {

    String dbName;
    String tableName;
    private DBManager DB;

    {
        try {
            DB = new DBManager();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Query(String dbName) {
        this.dbName = dbName;
    }

    public List<String[]> select(String[] selectedColumns, String condition) {
        try (BufferedReader reader = new BufferedReader(new FileReader(dbName + "_" + tableName + ".txt"))) {
            String headerLine = reader.readLine();
            String[] headers = headerLine.split(",");
            List<String[]> results = new ArrayList<>();
            String line;

            List<String> columns = Table.getTableColumns(dbName, tableName);
            for (String selectedColumn : selectedColumns) {
                if (!columns.contains(selectedColumn)) {
                    throw new SqlException("Invalid columns");
                }
            }
            List<Integer> selectedIndices = new ArrayList<>();
            if (selectedColumns.length == 0) {
                for (int i = 0; i < headers.length; i++) {
                    selectedIndices.add(i);
                }
            } else {
                for (String column : selectedColumns) {
                    for (int i = 0; i < headers.length; i++) {
                        if (headers[i].equals(column)) {
                            selectedIndices.add(i);
                            break;
                        }
                    }
                }
            }

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }
                String[] values = line.split(",");

                if (condition == null || condition.isEmpty() || conditionMet(headers, values, condition)) {
                    String[] selectedValues = new String[selectedIndices.size()];
                    for (int i = 0; i < selectedIndices.size(); i++) {
                        selectedValues[i] = values[selectedIndices.get(i)];
                    }
                    results.add(selectedValues);
                }
            }
            return results;
        } catch (IOException | SqlException e) {
            DB.logError("SELECT", e.getMessage());
            System.err.println("Error executing SELECT query: " + e.getMessage());
            return Collections.emptyList();
        }
    }


    private boolean conditionMet(String[] headers, String[] values, String condition) {
        System.out.println("Evaluating condition: " + condition);
        if (headers.length != values.length) {
            System.out.println("Headers and values length mismatch");
            return false; // Ensure headers and values arrays have the same length
        }

        String[] parts = condition.split("=");
        if (parts.length != 2) {
            System.out.println("Condition split length is not 2");
            return false;
        }

        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals(parts[0].trim()) && values.length > i && values[i].equals(parts[1].trim())) {
                return true;
            }
        }
        return false;
    }

    public void insert(String[] row) {
        try {
            List<String> columns = Table.getTableColumns(dbName, tableName);
            if (row.length != columns.size()) {
                throw new SqlException("Row length does not match the number of columns");
            }

            // Assuming that the first three columns should be digits
            for (int i = 0; i < 3; i++) {
                if (!row[i].matches("\\d+")) {
                    throw new SqlException("Invalid data format for column " + columns.get(i) + ": expected digits");
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(dbName + "_" + tableName + ".txt", true))) {
                writer.write(String.join(",", row));
                writer.newLine();
            }
        } catch (IOException | SqlException e) {
            DB.logError("INSERT", e.getMessage());
            System.err.println("Error executing INSERT query: " + e.getMessage());
        }
    }

    public void update(String condition, String[] newValues) {
        try {
            List<String> columns = Table.getTableColumns(dbName, tableName);
            if (newValues.length != columns.size()) {
                throw new SqlException("Row length does not match the number of columns");
            }

            // Assuming that the first three columns should be digits
            for (int i = 0; i < 3; i++) {
                if (!newValues[i].matches("\\d+")) {
                    throw new SqlException("Invalid data format for column " + columns.get(i) + ": expected digits");
                }
            }

            File tempFile = new File(dbName + "_" + tableName + "_temp.txt");
            try (BufferedReader reader = new BufferedReader(new FileReader(dbName + "_" + tableName + ".txt"));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

                String headerLine = reader.readLine();
                writer.write(headerLine);
                writer.newLine();

                String[] headers = headerLine.split(",");
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    if (condition == null || condition.isEmpty() || conditionMet(headers, values, condition)) {
                        writer.write(String.join(",", newValues));
                    } else {
                        writer.write(line);
                    }
                    writer.newLine();
                }

                File originalFile = new File(dbName + "_" + tableName + ".txt");
                originalFile.delete();
                tempFile.renameTo(originalFile);
            } catch (IOException e) {
                DB.logError("UPDATE", e.getMessage());
                System.err.println("Error executing UPDATE query: " + e.getMessage());
            }
        } catch (IOException | SqlException e) {
            DB.logError("UPDATE", e.getMessage());
            System.err.println("Error executing UPDATE query: " + e.getMessage());
        }
    }

    public void delete(String condition) throws IOException, SqlException {
        try {
            List<String> columns = Table.getTableColumns(dbName, tableName);
            if (!isValidConditionColumn(condition, columns)) {
                throw new SqlException("Invalid condition: Column specified in condition does not exist in table");
            }

            File tempFile = new File(dbName + "_" + tableName + "_temp.txt");
            try (BufferedReader reader = new BufferedReader(new FileReader(dbName + "_" + tableName + ".txt"));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

                String headerLine = reader.readLine();
                writer.write(headerLine);
                writer.newLine();

                String[] headers = headerLine.split(",");
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    if (!(condition == null || condition.isEmpty() || conditionMet(headers, values, condition))) {
                        writer.write(line);
                        writer.newLine();
                    }
                }

                File originalFile = new File(dbName + "_" + tableName + ".txt");
                originalFile.delete();
                tempFile.renameTo(originalFile);
            } catch (IOException e) {
                DB.logError("DELETE", e.getMessage());
                throw e;
            }
        } catch (IOException | SqlException e) {
            DB.logError("DELETE", e.getMessage());
            System.out.println("Error::"+e.getMessage());

        }
    }

    private boolean isValidConditionColumn(String condition, List<String> columns) {
        if (condition == null || condition.isEmpty()) {
            return true; // No condition specified
        }

        String[] parts = condition.split("=");
        if (parts.length != 2) {
            return false; // Invalid condition format
        }

        String column = parts[0].trim();
        return columns.contains(column);
    }

    public void parseAndExecute(String query) {
        try {
            query = query.trim();
            String[] tokens = query.split("\\s+");
            if (tokens.length < 4) throw new SqlException("Invalid query");

            String command = tokens[0].toUpperCase();

            switch (command) {
                case "SELECT":
                    if (!tokens[2].equalsIgnoreCase("FROM")) {
                        throw new SqlException("Syntax error in SELECT query");
                    }
                    String columnsPart = tokens[1];
                    this.tableName = tokens[3];
                    String[] selectedColumns = columnsPart.equals("*") ? new String[0] : columnsPart.split(",");
                    String condition = null;
                    if (query.toUpperCase().contains("WHERE")) {
                        condition = query.substring(query.toUpperCase().indexOf("WHERE") + 6).trim();
                    }
                    List<String[]> results = select(selectedColumns, condition);
                    for (String[] row : results) {
                        System.out.println(Arrays.toString(row));
                    }
                    break;
                case "INSERT":
                    if (!tokens[1].equalsIgnoreCase("INTO") || !tokens[2].equalsIgnoreCase(tableName)) {
                        throw new SqlException("Syntax error in INSERT query");
                    }
                    String[] values = query.substring(query.indexOf("VALUES") + 7).replaceAll("[()]", "").split(",");
                    insert(values);
                    System.out.println("Row inserted.");
                    break;
                case "UPDATE":
                    if (!tokens[1].equalsIgnoreCase(tableName) || !tokens[2].equalsIgnoreCase("SET")) {
                        throw new SqlException("Syntax error in UPDATE query");
                    }
                    String[] setValues = query.substring(query.indexOf("SET") + 4, query.toUpperCase().indexOf("WHERE")).split(",");
                    String updateCondition = null;
                    if (query.toUpperCase().contains("WHERE")) {
                        updateCondition = query.substring(query.toUpperCase().indexOf("WHERE") + 6).trim();
                    }
                    update(updateCondition, setValues);
                    System.out.println("Rows updated.");
                    break;
                case "DELETE":
                    if (!tokens[1].equalsIgnoreCase("FROM") || !tokens[2].equalsIgnoreCase(tableName)) {
                        throw new SqlException("Syntax error in DELETE query");
                    }
                    String deleteCondition = null;
                    if (query.toUpperCase().contains("WHERE")) {
                        deleteCondition = query.substring(query.toUpperCase().indexOf("WHERE") + 6).trim();
                    }
                    delete(deleteCondition);
                    System.out.println("Rows deleted.");
                    break;
                default:
                    throw new SqlException("Unsupported command: " + command);
            }
        } catch (SqlException | IOException e) {
            DB.logError(query, e.getMessage());
            System.err.println("Error executing query: " + e.getMessage());
        }
    }
}
