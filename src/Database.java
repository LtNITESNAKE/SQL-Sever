import java.io.*;
import java.util.*;

public class Database {
    private final String name;
    private static final String LOCAL_STORAGE_FILE = "local_storage.txt";

    public Database(String name) {
        this.name = name;
    }

    public void create() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(LOCAL_STORAGE_FILE, true));
        writer.write(name);
        writer.newLine();
        writer.close();
    }

    public static List<String> listDatabases() throws IOException {
        List<String> databases = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(LOCAL_STORAGE_FILE));
        String line;
        while ((line = reader.readLine()) != null) {
            databases.add(line);
        }
        reader.close();


        return databases;
    }

}
