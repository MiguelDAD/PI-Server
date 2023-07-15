/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pi.server;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import pi.server.Datos.Jornada;
import pi.server.Datos.Partido;

//PARA QUE SE PUEDAN ORDENAR
public class Tarea implements Comparable<Tarea> {

    private LocalDateTime ejecucion;

    //NOMBRETAREA;IDDELCORRESPONDIENTE
    private String accion;

    public Tarea(LocalDateTime ejecucion, String accion) {
        this.ejecucion = ejecucion;
        this.accion = accion;
    }

    public LocalDateTime getEjecucion() {
        return ejecucion;
    }

    public void setEjecucion(LocalDateTime ejecucion) {
        this.ejecucion = ejecucion;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public void ejecutarTarea(JDBCManager jdbc) {

        LogServer.guardarLog("Iniciando la tarea: " + accion);

        String[] partesAccion = accion.split(";");

        String tarea = partesAccion[0];
        int idEvento = Integer.parseInt(partesAccion[1]);
        if (tarea.equalsIgnoreCase(TiposDeEnfrentamientos.GENERATE_FIGHT_TOURNAMENTS)) {
            generarEnfrentamientoTorneos(jdbc, idEvento);
        } else if (tarea.equalsIgnoreCase(TiposDeEnfrentamientos.GENERATE_FIGHT_LEAGUES)) {
            generarEnfrentamientosLiga(jdbc, idEvento);
        } else if (tarea.equalsIgnoreCase(TiposDeEnfrentamientos.CHECK_MATCH)) {
            comprobarPartido(jdbc, idEvento);
        } else if (tarea.equalsIgnoreCase(TiposDeEnfrentamientos.END_MATCH)) {
            finalizarPartido(jdbc, idEvento);
        }

    }

    private void comprobarPartido(JDBCManager jdbc, int idPartido) {
        hacerLog("COMPROBANDO INTEGRANTES DEL PARTIDO");
        if (jdbc.ctdaUsuariosInscritosPartidos(idPartido) >= jdbc.ctdadNecesariaParaPartido(idPartido)) {
            hacerLog("EL PARTIDO HA OBTENIDO LAS INSCRIPCCIONES SUFICIENTES");
            //PASO EL ESTADO A JUGANDOSE
            jdbc.cambiarEstadoPartido(idPartido, 1);

            hacerLog("CREANDO TAREA PARA FINALIZAR PARTIDO");
            LocalDateTime ejecutarse = LocalDateTime.now();
            ejecutarse = ejecutarse.plusHours(2);
            Tarea t = new Tarea(ejecucion, TiposDeEnfrentamientos.END_MATCH + ";" + idPartido);

            PIServer.tareasPendientes.agregarTarea(t);

        } else {
            hacerLog("EL PARTIDO NO HA OBTENIDO LAS INSCRIPCCIONES SUFICIENTES");
            //PASO EL ESTADO A CANCELADO
            jdbc.cambiarEstadoPartido(idPartido, 3);
        }

    }

    private void finalizarPartido(JDBCManager jdbc, int idPartido) {
        hacerLog("EL PARTIDO HA FINALIZADO");
        //PASO EL ESTADO A FINALIZADO
        jdbc.cambiarEstadoPartido(idPartido, 4);
    }

    private void generarEnfrentamientoTorneos(JDBCManager jdbc, int idTorneo) {

        hacerLog("GENERANDO ENFRENTAMIENTO DEL TORNEO");

        //COMPROBAR QUE NO SE HA GENERADO LOS ENFRENTAMIENTOS
        if (jdbc.enfrentamientoGeneradoTorneo(idTorneo)) {

            //COMPROBAR QUE EL ESTADO ES ESPERANDO
            if (jdbc.comprobarEstadoTorneo(idTorneo, 2)) {
                hacerLog("EL ESTADO DEL TORNEO ES ESPERANDO");
                hacerLog("COMPROBANDO QUE CUMPLE LOS MINIMOS INSCRITOS");

                //COMPRUEBO SI HA CUMPLIDO EL MINIMO DE USUAIROS
                if (jdbc.ctdaEquiposInscritosTorneo(idTorneo) >= jdbc.cantidadMinimaEquiposTorneo(idTorneo)) {

                    hacerLog("OBTENIENDO LOS EQUIPOS INSCRITOS");

                    Stack<Integer> equipos = jdbc.stackEquiposInscritosTorneo(idTorneo);

                    List<String> rondas = generarRondas(equipos);

                    hacerLog("ALMACENANDO LAS RONDAS EN LA BASE DE DATOS");

                    //S = ronda + ";" + e1 + ";" + e2
                    for (String s : rondas) {
                        String[] partes = s.split(";");

                        int ronda = Integer.parseInt(partes[0]);

                        int e1, e2;

                        try {
                            e1 = Integer.parseInt(partes[1]);
                        } catch (Exception e) {
                            e1 = -1;
                        }

                        try {
                            e2 = Integer.parseInt(partes[2]);
                        } catch (Exception e) {
                            e2 = -1;
                        }

                        if (jdbc.insertarRonda(idTorneo, ronda, e1, e2)) {
                            hacerLog("INSERTADO LA RONDA: " + ronda + "! " + e1 + "-" + e2);
                        } else {
                            hacerLog("NO SE HA PODIDO INSERTAR LA RONDA: " + ronda + "! " + e1 + "-" + e2);
                        }

                    }

                    jdbc.generadoEnfrentamientoTorneo(idTorneo);
                    jdbc.cambiarEstadoTorneo(idTorneo, 1);
                    hacerLog("ESTADO DEL TORNEO ESTABLECIDO EN JUGANDOSE");

                } else {
                    hacerLog("EL TORNEO NO HA OBTENIDO LAS INSCRIPCCIONES SUFICIENTES");
                    //PASO EL ESTADO A CANCELADO
                    jdbc.cambiarEstadoTorneo(idTorneo, 3);

                }

            } else {
                hacerLog("LOS ENFRENTAMIENTOS YA ESTABAN GENERADOS");
            }
        } else {
            hacerLog("LOS ENFRENTAMIENTOS YA ESTABAN GENERADOS -");
            jdbc.cambiarEstadoTorneo(idTorneo, 1);
        }
        hacerLog("FINALIZADA LA GENRACION DE ENFRENTAMIENTOS");

    }

    private List<String> generarRondas(Stack<Integer> idEquipos) {

        hacerLog("GENERANDO LAS RONDAS");

        List<String> rondasGeneras = new ArrayList<>();

        int ctdInscritos = idEquipos.size();

        int rondas = ctdInscritos > 4 ? 3 : 2;

        //VOY COLOCANDO POR RONDA
        String e1, e2;

        //VECES QUE SE TIENE QUE EJECUTAR
        boolean sonImpares = false;
        String sobrante = "";
        if (rondas == 2) {
            for (int i = 1; i <= rondas; i++) {

                for (int j = rondas - i; j >= 0; j--) {
                    e1 = "";
                    e2 = "";

                    if (!idEquipos.empty()) {
                        e1 = "" + idEquipos.pop();
                    }
                    if (!idEquipos.empty()) {
                        e2 = "" + idEquipos.pop();
                    }

                    if (!e1.equals("") && e2.equals("")) {
                        sonImpares = true;
                        sobrante = e1;
                    }

                    if (sonImpares && j == 0) {
                        e1 = sobrante;
                        sonImpares = false;
                    }

                    hacerLog("Ronda " + i + ":" + e1 + "-" + e2);
                    rondasGeneras.add(i + ";" + e1 + ";" + e2);
                }

            }
        } else if (rondas == 3) {
            for (int i = 1; i <= rondas; i++) {
                int partidosPorRonda = 8 / (int) Math.pow(2, i);

                for (int j = 0; j < partidosPorRonda; j++) {
                    e1 = "";
                    e2 = "";

                    if (!idEquipos.empty()) {
                        e1 = "" + idEquipos.pop();
                    }
                    if (!idEquipos.empty()) {
                        e2 = "" + idEquipos.pop();
                    }

                    if (!e1.equals("") && e2.equals("")) {
                        sonImpares = true;
                        sobrante = e1;
                    }

                    if (sonImpares && j == 0) {
                        e1 = sobrante;
                        sonImpares = false;
                    }

                    hacerLog("Ronda " + i + ":" + e1 + "-" + e2);
                    rondasGeneras.add(i + ";" + e1 + ";" + e2);
                }
                sonImpares = false;

            }
        }

        return rondasGeneras;
    }

    private void generarEnfrentamientosLiga(JDBCManager jdbc, int idLiga) {

        //OBTENER EQUIPOS DE LA LIGA        
        hacerLog("GENERANDO ENFRENTAMIENTO DE LA LIGA");

        //COMPROBAR QUE NO SE HA GENERADO LOS ENFRENTAMIENTOS
        if (jdbc.enfrentamientoGeneradoLiga(idLiga)) {
            //COMPROBAR QUE EL ESTADO ES ESPERANDO
            if (jdbc.comprobarEstadoLiga(idLiga, 2)) {
                hacerLog("EL ESTADO DE LA LIGA ES ESPERANDO");
                hacerLog("COMPROBANDO QUE CUMPLE LOS MINIMOS INSCRITOS");

                //COMPRUEBO SI HA CUMPLIDO EL MINIMO DE USUAIROS
                if (jdbc.ctdaEquiposInscritosLiga(idLiga) >= jdbc.cantidadMinimaEquiposLiga(idLiga)) {

                    hacerLog("OBTENIENDO LOS EQUIPOS INSCRITOS");

                    List<String> equipos = jdbc.listEquiposInscritosLiga(idLiga);

                    List<Jornada> jornada = generarJornadas(equipos);
                    establecerFecha(jornada, jdbc, idLiga);

                    hacerLog("ALMACENANDO LAS JORNADAS EN LA BASE DE DATOS");
                    //GUARDAR JORNADAS

                    for (Jornada j : jornada) {
                        for (Partido p : j.getPartidos()) {
                            DateTimeFormatter formaFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            String fechaString = p.getFechaHora().format(formaFecha);
                            DateTimeFormatter fomaHora = DateTimeFormatter.ofPattern("HH:mm");
                            String horaString = p.getFechaHora().format(fomaHora);
                            if (jdbc.insertarJornada(idLiga, j.getNumero(), Integer.parseInt(p.getLocal()), Integer.parseInt(p.getVisitante()),
                                    fechaString, horaString)) {
                                hacerLog("INSERTADO LA JORNADA: " + j.getNumero() + "! " + p.getLocal() + "-" + p.getVisitante());
                            } else {
                                hacerLog("NO SE HA PODIDO INSERTAR LA RONDA: " + j.getNumero() + "! " + p.getLocal() + "-" + p.getVisitante());
                            }
                        }

                    }

                    jdbc.generadoEnfrentamientoLiga(idLiga);
                    jdbc.cambiarEstadoLiga(idLiga, 1);
                    hacerLog("ESTADO DE LA LIGA ESTABLECIDO EN JUGANDOSE");

                } else {
                    hacerLog("LA LIGA NO HA OBTENIDO LAS INSCRIPCCIONES SUFICIENTES");
                    //PASO EL ESTADO A CANCELADO
                    jdbc.cambiarEstadoLiga(idLiga, 3);

                }

            } else {
                hacerLog("LOS ENFRENTAMIENTOS YA ESTABAN GENERADOS");
            }
        } else {
            hacerLog("LOS ENFRENTAMIENTOS YA ESTABAN GENERADOS -");
            jdbc.cambiarEstadoLiga(idLiga, 1);
        }
        hacerLog("FINALIZADA LA GENRACION DE ENFRENTAMIENTOS");

    }

    private List<Jornada> generarJornadas(List<String> equipos) {
        List<Jornada> jornadas = new ArrayList<>();
        int totalEquipos = equipos.size();
        int totalJornadas = totalEquipos - 1;
        int partidosPorJornada = totalEquipos / 2;

        for (int i = 0; i < totalJornadas * 2; i++) {
            Jornada jornada = new Jornada(i + 1);
            jornadas.add(jornada);

            List<String> equiposLocal = new ArrayList<>();
            List<String> equiposVisitante = new ArrayList<>();

            // Generar partidos de ida
            for (int j = 0; j < partidosPorJornada; j++) {
                equiposLocal.add(equipos.get(j));
                equiposVisitante.add(equipos.get(totalEquipos - 1 - j));
            }

            // Generar partidos de vuelta
            for (int j = 0; j < partidosPorJornada; j++) {
                String local = equiposVisitante.get(j);
                String visitante = equiposLocal.get(j);
                Partido partido = new Partido(local, visitante);
                jornada.agregarPartido(partido);
            }

            // Rotar equipos para la siguiente jornada
            equipos.add(1, equipos.remove(equipos.size() - 1));
        }

        return jornadas;

    }

    private void establecerFecha(List<Jornada> jornadas, JDBCManager jdbc, int idLiga) {

        LocalDate fechaInicio = LocalDate.parse(jdbc.fInicioLiga(idLiga)); // Fecha de inicio de la liga
        int duracionPartido = jdbc.duracionPartidoLiga(idLiga); // Duración de un partido en horas
        LocalTime horaInicio = LocalTime.parse(jdbc.horaInicioPartidosLiga(idLiga)); // Hora de inicio de los partidos
        LocalTime horaFin = LocalTime.parse(jdbc.horaFinPartidosLiga(idLiga)); // Hora de fin de los partidos
        int frecuenciaJornadas = jdbc.frecuenciaJornadaLiga(idLiga); // Frecuencia de las jornadas en días

        // Asignar fechas y horarios a los partidos de cada jornada
        LocalDateTime fechaHoraActual;
        LocalDateTime fechaJornada = LocalDateTime.of(fechaInicio, horaInicio);
        for (Jornada jornada : jornadas) {
            fechaHoraActual = fechaJornada;
            for (Partido partido : jornada.getPartidos()) {
                //AÑADE LA HORA AL PARTIDO
                partido.setFechaHora(fechaHoraActual);

                //SUMA A LA FECHA LAS HORAS QUE DURA EL PARTIDO
                fechaHoraActual = fechaHoraActual.plusMinutes(duracionPartido);
                //SI LA FECHA SE PASA DEL LIMTIE DE HORARIO SE PASA AL DIA SIGUIENTE
                if (fechaHoraActual.toLocalTime().isAfter(horaFin)) {
                    fechaHoraActual = LocalDateTime.of(fechaHoraActual.toLocalDate().plusDays(1), horaInicio);
                }
            }

            //REINICIO LOS DATOS DE LA JORANDA
            fechaJornada = fechaJornada.withHour(horaInicio.getHour());
            fechaJornada = fechaJornada.withMinute(horaInicio.getMinute());
            fechaJornada = LocalDateTime.of(fechaJornada.toLocalDate().plusDays(frecuenciaJornadas), horaInicio);
        }
    }

    //PARA QUE SE ORDENEN POR TIEMPO
    @Override
    public int compareTo(Tarea o) {
        return ejecucion.compareTo(o.getEjecucion());
    }

    private void hacerLog(String mensaje) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String ahora = dtf.format(LocalDateTime.now());

        String men = "[" + ahora + "] " + accion + ": " + mensaje;

        System.out.println(men);
        LogServer.guardarLog(men);
    }

}
