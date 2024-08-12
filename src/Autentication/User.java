package Autentication;

import java.io.*;
public abstract class User {
    protected String username;
    protected String password;
   protected static final String CREDENTIALS_FILE = "credentials.txt";

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public abstract boolean authenticate(String username, String password) throws IOException;

    public abstract void addUser(String username, String password) throws IOException ;

    public static User getUser(String username, String password) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(CREDENTIALS_FILE));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts[0].equals(username) && parts[1].equals(password)) {
                if (parts.length == 3 && parts[2].equalsIgnoreCase("admin")) {
                    reader.close();
                    return new DatabaseAdmin(username, password, "admin");
                } else if (parts.length == 3 && parts[2].equalsIgnoreCase("user")) {
                    reader.close();
                    return new NormalUser(username, password,"user");
                }
            }
        }
        reader.close();
        return null;
    }
}
