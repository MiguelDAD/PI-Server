/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pi.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Miguel
 */
public class LectorINI {

    private String URL;
    private String USER;
    private String PASS;

    public LectorINI() {
        try (InputStream is = new FileInputStream("conector.ini")) {
            Properties pop = new Properties();
            pop.load(is);
            URL = pop.getProperty("URL");
            USER = pop.getProperty("USER");
            PASS = pop.getProperty("PASS");

        } catch (IOException ex) {
            Logger.getLogger(LectorINI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String getURL() {
        return URL;
    }

    public String getUSER() {
        return USER;
    }

    public String getPASS() {
        return PASS;
    }
}
