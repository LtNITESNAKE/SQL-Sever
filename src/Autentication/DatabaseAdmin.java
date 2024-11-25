package Autentication;

import java.io.*;

final public class DatabaseAdmin extends User {
   private String Title;

    public DatabaseAdmin(String username, String password, String title) {
        super(username, password);
        Title = title;
    }
    public  boolean authenticate(String username, String password) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(User.CREDENTIALS_FILE));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts[0].equals(username) && parts[1].equals(password)&& parts[2].equals("admin")) {
                reader.close();
                return true;
            }
        }
        reader.close();
        return false;
    }
    public  void addUser(String username, String password) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(User.CREDENTIALS_FILE, true));
        writer.write(username + "," + password+","+Title);
        writer.newLine();
        writer.close();
    }

    public String getTitle() {
        return Title;
    }
}
