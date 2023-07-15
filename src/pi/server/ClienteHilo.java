/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pi.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Miguel
 */
public class ClienteHilo extends Thread {

    private Socket clientSocket;
    private int idUsuario;
    private String nombre;
    private Protocolo protocolo;
    
    
    public ClienteHilo(Socket clientsocket) {
        this.clientSocket = clientsocket;
        protocolo = new Protocolo();
        
    }

    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;
        boolean usuarioConectado = true;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String inputLine = null;

        try {
            hacerLog("Se ha conectado un cliente");
            String respuesta;
            while (usuarioConectado) {
                inputLine = in.readLine();
                if (inputLine == null || inputLine.equals(Mensajes.EXIT)) {
                    usuarioConectado = false;
                    protocolo.responderPeticion(inputLine);
                } else {
                    respuesta = protocolo.responderPeticion(inputLine);
                    out.println(respuesta);
                }

            }

        } catch (SocketException ex) {
            //CODIGO DE ERROR CUANDO UN USUARIO SE DESCONECTA DE MANERA REPENTINA
            if ("Connection reset".equals(ex.getMessage())) {
                hacerLog("Un usuario se ha desconectado de forma abrupta");
            } else {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void hacerLog(String infor) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String ahora = dtf.format(LocalDateTime.now());

        String log = "[" + ahora + "] " + infor;
        
        System.out.println(log);
        LogServer.guardarLog(log);
    }
}
