/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pi.server;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Miguel
 */
public class Encriptador {
    
    private static String bytesToHex(byte[] bytes){
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) 
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            return sb.toString();
            
    }
    
    public static String generate512(String passwordToHash){
        String generatedPassword = null;
        try {
            
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            byte[] byteOfTextToHash = passwordToHash.getBytes(StandardCharsets.UTF_8);
            byte[] hashedByteArray  = md.digest(byteOfTextToHash);
            
            generatedPassword = bytesToHex(hashedByteArray);
            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

}
