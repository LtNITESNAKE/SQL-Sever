package Autentication;

import java.io.*;

final public class NormalUser extends User {
    String Title;

    public NormalUser(String username, String password, String title) {
        super(username, password);
        Title = title;
    }

    public NormalUser(String username, String password) {
        super(username, password);
    }
    public  boolean authenticate(String username, String password) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(CREDENTIALS_FILE));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts[0].equals(username) && parts[1].equals(password)&& parts[2].equals("user")) {
                reader.close();
                return true;
            }
        }
        reader.close();
        return false;
    }
    public  void addUser(String username, String password) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(CREDENTIALS_FILE, true));
        writer.write(username + "," + password);
        writer.newLine();
        writer.close();
    }

    public String getTitle() {
        return Title;
    }
}
