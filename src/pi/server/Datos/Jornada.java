/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pi.server.Datos;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Miguel
 */
public class Jornada {

    private int numero;
    private List<Partido> partidos;

    public Jornada(int numero) {
        this.numero = numero;
        this.partidos = new ArrayList<>();
    }

    public int getNumero() {
        return numero;
    }

    public List<Partido> getPartidos() {
        return partidos;
    }

    public void agregarPartido(Partido partido) {
        partidos.add(partido);
    }
}
