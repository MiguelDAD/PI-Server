/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package pi.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Miguel
 */
public class PIServer {

    ServerSocket serverSocket;
    int portService;

    static TareaHilo tareasPendientes;

    public PIServer() {

        portService = 7777;
        serverSocket = null;
    }

    public void iniciarServidor() {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String ahora = dtf.format(LocalDateTime.now());

        try {
            serverSocket = new ServerSocket(portService);
            hacerLog("SERVIDOR INICIADO");
        } catch (IOException e) {
            System.err.println("No se puede escuchar por el puerto: " + portService);
            System.exit(1);
        }

        Socket clientSocket;

        try {
            while (true) {
                clientSocket = serverSocket.accept();
                new ClienteHilo(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Fallo al conectarse el cliente.");
            System.exit(1);
        }
        try {
            serverSocket.close();
        } catch (IOException ex) {
            System.err.println("Fallo al cerrar el servidor.");
            System.exit(1);
        }

    }

    public void cargarTareas() {

        JDBCManager jdbc = new JDBCManager();

        PriorityQueue<Tarea> tareas = new PriorityQueue<>();

        hacerLog("Obteniendo todos los torneos pendientes a generar");
        PriorityQueue<Tarea> torneosPendientes = jdbc.torneosPendientesGenerar();

        hacerLog("Obteniendo todas las ligas pendientes a generar");
        PriorityQueue<Tarea> ligasPendientes = jdbc.ligasPendientesGenerar();

        hacerLog("Obteniendo los partidos por jugarse");
        PriorityQueue<Tarea> partidosPendientes = jdbc.partidosPorJugar();

        tareas.addAll(torneosPendientes);
        tareas.addAll(ligasPendientes);
        tareas.addAll(partidosPendientes);

        tareasPendientes = new TareaHilo(tareas);
        hacerLog("Creado el hilo encargado de las tareas");
        tareasPendientes.start();
        hacerLog("Iniciado el hilo encargado de las tareas");

    }

    private void hacerLog(String infor) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String ahora = dtf.format(LocalDateTime.now());

        String log = "---- [" + ahora + "] " + infor + " ----";

        System.out.println(log);
        LogServer.guardarLog(log);
    }

    public static void main(String[] args) throws IOException {

        PIServer servidor = new PIServer();
        servidor.iniciarXampp();
        servidor.cargarTareas();
        servidor.iniciarServidor();

    }

    public void iniciarXampp() {
        String os = System.getProperty("os.name").toLowerCase();

        //INICIO XAMPP SI ESTOY EN WINDOWS
        if (os.contains("win")) {
            try {
                // Ruta del ejecutable de XAMPP
                String rutaXAMPP = "C:/xampp/xampp_start.exe";
                String comando = rutaXAMPP;
                ProcessBuilder pb = new ProcessBuilder(comando);
                pb.start();
                System.out.println("XAMPP iniciado correctamente.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
