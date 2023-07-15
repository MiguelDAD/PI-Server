package pi.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Miguel
 */
public class LogServer {
    
    //STATIC
    public synchronized static void guardarLog(String texto) {
        FileWriter fw = null;
        
        try {
            // Obtener la fecha actual
            LocalDate fechaActual = LocalDate.now();
            DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            String fechaComoTexto = fechaActual.format(formatoFecha);
            
            // Crear el archivo y la ruta relativa
            String rutaRelativa = "log" + File.separator + fechaComoTexto + ".log";
            File archivo = new File(rutaRelativa);
            
            // Si el archivo no existe, crearlo
            if (!archivo.exists()) {
                archivo.getParentFile().mkdirs();
                archivo.createNewFile();
            }   

            fw = new FileWriter(archivo, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(texto);
            bw.newLine();
            
            bw.close();
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(PIServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(PIServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    
}
