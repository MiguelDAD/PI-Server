/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pi.server.Datos;

import java.time.LocalDateTime;

/**
 *
 * @author Miguel
 */
public class Partido {

    private String local;
    private String visitante;
    private LocalDateTime fechaHora;

    public Partido(String local, String visitante) {
        this.local = local;
        this.visitante = visitante;
    }

    public String getLocal() {
        return local;
    }

    public String getVisitante() {
        return visitante;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
    
    
}
