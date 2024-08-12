import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import Autentication.*;

public class SqlManagementStudio {

    private static DBManager DB;

    public static void main(String[] args) throws IOException, SqlException, SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username: ");
        String username = scanner.nextLine();
        System.out.println("Enter password: ");
        String password = scanner.nextLine();

        User currentUser = User.getUser(username, password);
        if (currentUser != null) {
            System.out.println("Authentication successful!");

            if (currentUser instanceof DatabaseAdmin) {
                String Title = ((DatabaseAdmin) currentUser).getTitle();
                // DatabaseAdmin menu
                while (true) {
                    System.out.println("Menu:");
                    System.out.println("1. Create Database");
                    System.out.println("2. List Databases");
                    System.out.println("3. Use Database");
                    System.out.println("4. Add New User");
                    System.out.println("5. Errors occurred so far ");
                    System.out.println("6. Exit");

                    int choice = Integer.parseInt(scanner.nextLine());

                    switch (choice) {
                        case 1:
                            System.out.println("Enter database name: ");
                            String dbName = scanner.nextLine();
                            Database db = new Database(dbName);
                            db.create();
                            System.out.println("Database created.");
                            break;
                        case 2:
                            List<String> databases = Database.listDatabases();
                            System.out.println("Databases: " + databases);
                            break;
                        case 3:
                            System.out.println("Enter database name: ");
                            String useDbName = scanner.nextLine();
                            if (Database.listDatabases().contains(useDbName)) {
                                System.out.println("Using database: " + useDbName);
                                useDatabase(useDbName, scanner, Title);
                            } else {
                                System.out.println("Database not found.");
                            }
                            break;
                        case 4:
                            System.out.println("Enter new username: ");
                            String newUsername = scanner.nextLine();
                            System.out.println("Enter password for new user: ");
                            String newPassword = scanner.nextLine();
                            System.out.println("Enter Title for the user");
                            String title = scanner.nextLine();
                            if (title.equalsIgnoreCase("Admin")) {
                                DatabaseAdmin DA = new DatabaseAdmin(newUsername, newPassword, title);
                                DA.addUser(newUsername, newPassword);
                            } else if (title.equalsIgnoreCase("User")) {
                                NormalUser Nu = new NormalUser(newUsername, newPassword, title);
                                Nu.addUser(newUsername, newPassword);
                            }
                            System.out.println("New user added: " + newUsername);
                            break;

                        case 5: {
                            DB = new DBManager();
                            ResultSet select = DB.select("Select * from ErrorLogs");

                            System.out.println("Errors:");
                            while (select.next()) {
                                int id = select.getInt("id");
                                String query = select.getString("query");
                                String errorMessage = select.getString("error_message");
                                String timestamp = select.getString("timestamp");

                                System.out.printf("ID: %d, Query: %s, Error Message: %s, Timestamp: %s%n", id, query, errorMessage, timestamp);
                            }
                            DB.close();
                        }
                        break;
                        case 6:
                            return;

                        default:
                            System.out.println("Invalid choice.");
                            break;
                    }
                }

            }else if(currentUser instanceof NormalUser) {
                String Title = ((NormalUser) currentUser).getTitle();
                while (true) {
                    System.out.println("Menu:");

                    System.out.println("1. List Databases");
                    System.out.println("2. Use Database");
                    System.out.println("3. Errors occurred so far ");
                    System.out.println("4. Exit");

                    int choice = Integer.parseInt(scanner.nextLine());

                    switch (choice) {
                        case 1:
                            List<String> databases = Database.listDatabases();
                            System.out.println("Databases: " + databases);
                            break;
                        case 2:
                            System.out.println("Enter database name: ");
                            String useDbName = scanner.nextLine();
                            if (Database.listDatabases().contains(useDbName)) {
                                System.out.println("Using database: " + useDbName);
                                useDatabase(useDbName, scanner, Title);
                            } else {
                                System.out.println("Database not found.");
                            }
                            break;
                            case 3: {
                            DB = new DBManager();
                            ResultSet select = DB.select("Select * from ErrorLogs");
                            System.out.println("Errors:");
                            while (select.next()) {
                                int id = select.getInt("id");
                                String query = select.getString("query");
                                String errorMessage = select.getString("error_message");
                                String timestamp = select.getString("timestamp");

                                System.out.printf("ID: %d, Query: %s, Error Message: %s, Timestamp: %s%n", id, query, errorMessage, timestamp);
                            }
                            DB.close();
                        }
                        break;
                        case 4:
                            return;

                        default:
                            System.out.println("Invalid choice.");
                            break;
                    }
                }

            }
        } else System.out.println("Authentication failed.");
    }

    public static void useDatabase(String dbName, Scanner scanner, String title) throws IOException, SqlException, SQLException {
        while (true) {
            System.out.println("Menu:");
            System.out.println("1. Create Table");
            System.out.println("2. List Tables");
            System.out.println("3. Select");
            System.out.println("4. Insert");
            System.out.println("5. Update");
            System.out.println("6. Delete");
            System.out.println("7. Custom Query");
            System.out.println("8. Exit");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    if (title.equalsIgnoreCase("Admin")) {
                        System.out.println("Enter table name: ");
                        String tableName = scanner.nextLine();
                        System.out.println("Enter columns (comma separated): ");
                        String columnsLine = scanner.nextLine();
                        List<String> columns = Arrays.asList(columnsLine.split(","));
                        Table table = new Table(dbName, tableName, columns);
                        table.create();
                        System.out.println("Table created.");
                        break;
                    } else {
                        System.out.println("Permission denied: Only DatabaseAdmin can create tables.");
                    }
                    break;
                case 2:
                    List<String> tables = Table.listTables(dbName);
                    System.out.println("Tables: " + tables);
                    break;
                case 3:
                    System.out.println("Enter table name: ");
                    String selectTableName = scanner.nextLine();
                    System.out.println("Enter columns to select (comma separated) or * for all: ");
                    String selectColumnsLine = scanner.nextLine();
                    String[] selectColumns = selectColumnsLine.equals("*") ? new String[0] : selectColumnsLine.split(",");
                    System.out.println("Enter condition (e.g., column=value) or leave blank: ");
                    String condition = scanner.nextLine();
                    Query selectQuery = new Query(dbName);
                    selectQuery.tableName = selectTableName;
                    List<String[]> selectResults = selectQuery.select(selectColumns, condition);
                    for (String[] row : selectResults) {
                        System.out.println(Arrays.toString(row));
                    }
                    break;
                case 4:
                    System.out.println("Enter table name: ");
                    String insertTableName = scanner.nextLine();
                    System.out.println("Enter row data (comma separated): ");
                    String rowData = scanner.nextLine();
                    String[] insertValues = rowData.split(",");
                    Query insertQuery = new Query(dbName);
                    insertQuery.tableName = insertTableName;
                    insertQuery.insert(insertValues);
                    System.out.println("Row inserted.");
                    break;
                case 5:
                    System.out.println("Enter table name: ");
                    String updateTableName = scanner.nextLine();
                    System.out.println("Enter SET values (comma separated): ");
                    String setValuesLine = scanner.nextLine();
                    String[] setValues = setValuesLine.split(",");
                    System.out.println("Enter condition (e.g., column=value) or leave blank: ");
                    String updateCondition = scanner.nextLine();
                    Query updateQuery = new Query(dbName);
                    updateQuery.tableName = updateTableName;
                    updateQuery.update(updateCondition, setValues);
                    System.out.println("Rows updated.");
                    break;
                case 6:
                    System.out.println("Enter table name: ");
                    String deleteTableName = scanner.nextLine();
                    System.out.println("Enter condition (e.g., column=value) or leave blank: ");
                    String deleteCondition = scanner.nextLine();
                    Query deleteQuery = new Query(dbName);
                    deleteQuery.tableName = deleteTableName;
                    deleteQuery.delete(deleteCondition);
                    System.out.println("Rows deleted.");
                    break;
                case 7:
                    System.out.println("Enter query: ");
                    String customQuery = scanner.nextLine();
                    Query queryProcessor = new Query(dbName);
                    queryProcessor.parseAndExecute(customQuery);
                    break;
                case 8:
                    return;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }
}


