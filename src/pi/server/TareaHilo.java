/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pi.server;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.PriorityQueue;

/**
 *
 * @author Miguel
 */
public class TareaHilo extends Thread {

    private PriorityQueue<Tarea> colaHoras;
    private JDBCManager jdbc;

    public TareaHilo() {
        colaHoras = new PriorityQueue<>();
        jdbc = new JDBCManager();
    }

    public TareaHilo(PriorityQueue<Tarea> colaHoras) {
        this.colaHoras = colaHoras;
        jdbc = new JDBCManager();

    }

    public void run() {
        while (true) {
            try {
                LocalDateTime horaActual = LocalDateTime.now();
                //LocalTime horaProxima = colaHoras.peek();
                LocalDateTime horaProxima = null;

                synchronized (colaHoras) {
                    Tarea tareaSiguiente = colaHoras.peek();

                    if (tareaSiguiente != null) {
                        horaProxima = tareaSiguiente.getEjecucion();
                    } else {
                        horaProxima = null;
                    }

                }

                String log = "";

                //System.out.println(colaHoras);
                if (horaProxima != null) {

                    // Si la hora de la cola es posterior a la actual, calculamos el tiempo que falta para la próxima hora
                    long segundosHastaProximaHora = LocalDateTime.now().until(horaProxima, java.time.temporal.ChronoUnit.SECONDS);
                    if (segundosHastaProximaHora > 0) {
                        log = "Esperando " + segundosHastaProximaHora + " segundos hasta la próxima tarea en: " + horaProxima;
                        System.out.println(log);
                        LogServer.guardarLog(log);

                        // Dormimos el hilo por el tiempo necesario
                        synchronized (colaHoras) {
                            colaHoras.wait(segundosHastaProximaHora * 1000);
                        }
                    } else {
                        // Si la hora es 0 o inferior a la actual se ejecuta la tarea

                        synchronized (colaHoras) {
                            Tarea ejecutar = colaHoras.poll();

                            ejecutar.ejecutarTarea(jdbc);

                            System.out.println(log);
                            LogServer.guardarLog(log);
                        }
                    }
                } else {
                    // Si no hay ninguna hora en la cola, dormimos el hilo hasta que se agregue una hora nueva
                    log = "No hay tareas en la cola. Esperando inserccion...";
                    System.out.println(log);
                    LogServer.guardarLog(log);

                    synchronized (colaHoras) {
                        colaHoras.wait();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void agregarTarea(Tarea tarea) {
        //colaHoras.offer(hora);
        // Despertamos el hilo para que revise la nueva hora agregada
        synchronized (colaHoras) {
            colaHoras.offer(tarea);
            System.out.println("Añadida la tarea: " + tarea.getAccion());
            LogServer.guardarLog("Añadida la tarea: " + tarea.getAccion());
            colaHoras.notify();
        }
    }

    public synchronized void eliminarTarea(String accion) {

        synchronized (colaHoras) {
            for (Tarea t : colaHoras) {
                if (t.getAccion().equalsIgnoreCase(accion)) {
                    colaHoras.remove(t);
                    System.out.println("Eliminada la tarea: " + t.getAccion());
                    LogServer.guardarLog("Eliminada la tarea: " + t.getAccion());
                    colaHoras.notify();
                    return;
                }
            }
        }
    }
}
