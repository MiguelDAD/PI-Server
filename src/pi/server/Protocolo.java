/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pi.server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Miguel
 */
public class Protocolo {

    //ROLES
    private final int ADMINISTRADOR = 1;
    private final int ORGANIZADOR = 2;
    private final int USUARIO = 3;

    //ESTADOS
    private final int LOGIN_REGISTER = 1; //ESPERANDO QUE EL USUARIO SE REGISTRE O HAGA EL LOGIN
    private final int LOGUEADO = 2;       //ESPERANDO QUE EL USUARIO REALIZE ALGUNA ACCION
    private final int SALIR = 3;          //EL USUARIO SE HA DESCONECTADO

    //CARACTERISTICAS USUARIO
    private int idUsuario;
    private String nombre;
    private int rol;
    private int estado;

    private JDBCManager jdbc;

    public Protocolo() {
        jdbc = new JDBCManager();

        estado = LOGIN_REGISTER;
        idUsuario = 0;
        nombre = "";
        rol = 0;
    }

    public String responderPeticion(String inputLine) {

        String[] partesMensaje = null;
        String tipoDeMensaje = "";
        String respuesta = "";
        if (inputLine == null) {
            estado = SALIR;
        } else {
            hacerLog(inputLine);
            partesMensaje = inputLine.split(";");
            tipoDeMensaje = partesMensaje[0];
            respuesta = "";
        }

        try {
            switch (estado) {
                case LOGIN_REGISTER:
                    switch (tipoDeMensaje) {

                        case Mensajes.LOGIN:
                            String rolUsuario = partesMensaje[3];
                            switch (rolUsuario) {
                                case "ORGANIZADOR":
                                    idUsuario = jdbc.iniciarSesionOrganizador(partesMensaje[1], Encriptador.generate512(partesMensaje[2]));
                                    if (idUsuario > 0) {
                                        nombre = partesMensaje[1];
                                        estado = LOGUEADO;

                                        if (jdbc.esAdministrador(idUsuario)) {
                                            respuesta = (Mensajes.LOGIN_ACCEPT_ADMIN + ";" + idUsuario + ";" + nombre);
                                            rol = ADMINISTRADOR;
                                        } else {
                                            respuesta = (Mensajes.LOGIN_ACCEPT_ORGANIZER + ";" + idUsuario + ";" + nombre);
                                            rol = ORGANIZADOR;
                                        }
                                    } else {
                                        respuesta = (Mensajes.LOGIN_ERROR_ORGANIZER);
                                    }

                                    break;

                                case "USUARIO":
                                    idUsuario = jdbc.iniciarSesionUsuario(partesMensaje[1], Encriptador.generate512(partesMensaje[2]));
                                    if (idUsuario > 0) {
                                        nombre = partesMensaje[1];
                                        respuesta = (Mensajes.LOGIN_ACCEPT_USER + ";" + idUsuario + ";" + nombre);
                                        estado = LOGUEADO;
                                        rol = USUARIO;

                                    } else {
                                        respuesta = (Mensajes.LOGIN_ERROR_USER);
                                    }
                                    break;
                                default:
                                    respuesta = (Mensajes.ERROR);
                                    break;
                            }
                            break;
                        case Mensajes.REGISTER:
                            rolUsuario = partesMensaje[4];
                            switch (rolUsuario) {
                                case "ORGANIZADOR":
                                    if (jdbc.registrarUsuarioOrganizador(partesMensaje[1], partesMensaje[2], Encriptador.generate512(partesMensaje[3]))) {
                                        idUsuario = jdbc.iniciarSesionOrganizador(partesMensaje[1], Encriptador.generate512(partesMensaje[3]));
                                        nombre = partesMensaje[1];
                                        respuesta = (Mensajes.REGISTER_ACCEPT_ORGANIZER + ";" + idUsuario + ";" + nombre);
                                        estado = LOGUEADO;
                                        rol = ORGANIZADOR;

                                    } else {
                                        respuesta = (Mensajes.REGISTER_ERROR_ORGANIZER);
                                    }
                                    break;

                                case "USUARIO":
                                    if (jdbc.registrarUsuarioUsuario(partesMensaje[1], partesMensaje[2], Encriptador.generate512(partesMensaje[3]))) {
                                        idUsuario = jdbc.iniciarSesionUsuario(partesMensaje[1], Encriptador.generate512(partesMensaje[3]));
                                        nombre = partesMensaje[1];
                                        respuesta = (Mensajes.REGISTER_ACCEPT_USER + ";" + idUsuario + ";" + nombre);
                                        estado = LOGUEADO;
                                        rol = USUARIO;

                                    } else {
                                        respuesta = (Mensajes.REGISTER_ERROR_USER);
                                    }
                                    break;
                                default:
                                    respuesta = (Mensajes.ERROR);
                                    break;
                            }
                            break;
                        case Mensajes.EXIT:
                            respuesta = "SE HA DESCONECTADO";
                            estado = SALIR;
                            break;
                        default:
                            respuesta = (Mensajes.ERROR);
                            break;
                    }
                    break;
                case LOGUEADO:
                    switch (tipoDeMensaje) {

                        case Mensajes.LOGOUT:
                            respuesta = Mensajes.LOGOUT_SEND;
                            estado = LOGIN_REGISTER;
                            idUsuario = 0;
                            nombre = "";
                            rol = 0;
                            break;

                        case Mensajes.TOURNAMENTS_ORGANIZER:

                            String torneos = jdbc.torneosDeUnOrganizador(idUsuario);

                            if (torneos != null) {
                                respuesta = (Mensajes.TOURNAMENTS_SEND_ORGANIZER + ";" + torneos);
                            } else {
                                respuesta = (Mensajes.TOURNAMENTS_ERROR_SEND_ORGANIZER);
                            }
                            break;
                        case Mensajes.GET_SPORT:
                            String deportes = jdbc.deportesDisponibles();

                            if (deportes != null) {
                                respuesta = (Mensajes.SEND_SPORT + ";" + deportes);
                            } else {
                                respuesta = (Mensajes.ERROR_SEND_SPORT);
                            }

                            break;

                        case Mensajes.TOURNAMENT_CREATE:
                            String datosTorneo = partesMensaje[1];

                            if (jdbc.insertarTorneo(datosTorneo, idUsuario)) {
                                respuesta = (Mensajes.TOURNAMENT_OK_CREATE);
                            } else {
                                respuesta = (Mensajes.TOURNAMENT_ERROR_CREATE);
                            }

                            break;

                        case Mensajes.TOURNAMENTS_END:

                            if (jdbc.finalizarTorneo(Integer.parseInt(partesMensaje[1]))) {
                                respuesta = (Mensajes.TOURNAMENTS_END_OK);
                            } else {
                                respuesta = (Mensajes.TOURNAMENTS_END_ERROR);
                            }

                            break;

                        case Mensajes.TOURNAMENTS_CANCEL:

                            if (jdbc.cancelarTorneo(Integer.parseInt(partesMensaje[1]))) {
                                respuesta = (Mensajes.TOURNAMENTS_CANCEL_OK);
                            } else {
                                respuesta = (Mensajes.TOURNAMENTS_CANCEL_ERROR);
                            }

                            break;

                        case Mensajes.TOURNAMENT_TEAMS:
                            String idTorneo = partesMensaje[1];
                            String equipos = null;
                            if (rol == USUARIO) {
                                equipos = jdbc.equiposDeUnTorneoParaUsuario(Integer.parseInt(idTorneo));
                            } else {
                                equipos = jdbc.equiposDeUnTorneo(Integer.parseInt(idTorneo));
                            }

                            if (equipos != null) {
                                respuesta = (Mensajes.TOURNAMENT_TEAMS_OK + ";" + equipos);
                            } else {
                                respuesta = (Mensajes.TOURNAMENT_TEAMS_ERROR);
                            }

                            break;

                        case Mensajes.LIGUES_ORGANIZER:
                            String ligas = jdbc.ligasDeUnOrganizador(idUsuario);

                            if (ligas != null) {
                                respuesta = (Mensajes.LIGUES_SEND_ORGANIZER + ";" + ligas);
                            } else {
                                respuesta = (Mensajes.LIGUES_ERROR_SEND_ORGANIZER);
                            }

                            break;

                        case Mensajes.LIGUES_CREATE:
                            String datosLiga = partesMensaje[1];

                            if (jdbc.insertarLiga(datosLiga, idUsuario)) {
                                respuesta = (Mensajes.LIGUES_OK_CREATE);
                            } else {
                                respuesta = (Mensajes.LIGUES_ERROR_CREATE);
                            }

                            break;

                        case Mensajes.LIGUES_TEAMS:
                            String idLiga = partesMensaje[1];
                            String equiposLiga = null;
                            if (rol == USUARIO) {
                                equiposLiga = jdbc.equiposDeUnaLigaParaUsuario(Integer.parseInt(idLiga));
                            } else {
                                equiposLiga = jdbc.equiposDeUnaLiga(Integer.parseInt(idLiga));
                            }

                            if (equiposLiga != null) {
                                respuesta = (Mensajes.LIGUES_TEAMS_OK + ";" + equiposLiga);
                            } else {
                                respuesta = (Mensajes.LIGUES_TEAMS_ERROR);
                            }

                            break;
                        case Mensajes.USERS_MYTEAMS:
                            String equiposDelUsuario = jdbc.equiposDeUnUsuario(idUsuario);

                            if (equiposDelUsuario != null) {
                                respuesta = Mensajes.USERS_MYTEAMS_OK + ";" + equiposDelUsuario;
                            } else {
                                respuesta = Mensajes.USERS_MYTEAMS_ERROR;
                            }
                            break;

                        case Mensajes.TEAM_INVITE_USER:
                            String nombreEquipo = partesMensaje[1];
                            String nombreUsuario = partesMensaje[2];
                            int idE = jdbc.obtenerIdEquipoSegunNombre(nombreEquipo);
                            int idU = jdbc.obtenerIdUsuarioSegunNombre(nombreUsuario);

                            if (jdbc.invitarUsuarioAEquipo(idE, idU)) {
                                respuesta = Mensajes.TEAM_INVITE_USER_OK + ";" + nombreUsuario;
                            } else {
                                respuesta = Mensajes.TEAM_INVITE_USER_ERROR;
                            }
                            break;

                        case Mensajes.TEAM_CREATE:

                            String nombreEquipoNuevo = partesMensaje[1];
                            String deporte = partesMensaje[2];
                            int idDeporte = jdbc.obtenerIdDeporte(deporte);
                            String ubicacion = partesMensaje[3];
                            int privacidad = jdbc.obtenerIdPrivacidad(partesMensaje[4]);

                            if (jdbc.crearEquipo(nombreEquipoNuevo, idDeporte, idUsuario, ubicacion, privacidad)) {
                                respuesta = Mensajes.TEAM_CREATE_OK;
                            } else {
                                respuesta = Mensajes.TEAM_CREATE_ERROR;
                            }

                            break;

                        case Mensajes.TOURNAMENTS_USER:
                            String torneosUso = jdbc.torneosIncristosUsuario(idUsuario);
                            if (torneosUso != null) {
                                respuesta = Mensajes.TOURNAMENTS_USER_OK + ";" + torneosUso;
                            } else {
                                respuesta = Mensajes.TOURNAMENTS_USER_ERROR;
                            }

                            break;
                        case Mensajes.TOURNAMENTS_AVAILABLE:
                            String torneosDispo;
                            if (!partesMensaje[1].equalsIgnoreCase("")) {
                                torneosDispo = jdbc.torneosDisponibles(partesMensaje[1], Float.parseFloat(partesMensaje[2]));
                                if (torneosDispo != null) {
                                    respuesta = Mensajes.TOURNAMENTS_AVAILABLE_OK + ";" + torneosDispo;
                                } else {
                                    respuesta = Mensajes.TOURNAMENTS_AVAILABLE_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.TOURNAMENTS_AVAILABLE_ERROR;
                            }
                            break;

                        case Mensajes.TOURNAMENTS_STATE_USER:
                            String torneoEstad;
                            if (!partesMensaje[1].equalsIgnoreCase("")) {
                                torneoEstad = jdbc.torneoEstadoUsuario(idUsuario, Integer.parseInt(partesMensaje[1]));
                                if (torneoEstad != null) {
                                    respuesta = Mensajes.TOURNAMENTS_STATE_USER_OK + ";" + torneoEstad;
                                } else {
                                    respuesta = Mensajes.TOURNAMENTS_STATE_USER_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.TOURNAMENTS_STATE_USER_ERROR;
                            }
                            break;

                        case Mensajes.TOURNAMENTS_TEAMS_INSCRIBE_USER:

                            String equiposInscribibles;
                            if (!partesMensaje[1].equalsIgnoreCase("")) {
                                equiposInscribibles = jdbc.tusEquiposInscribiblesTorneo(idUsuario, Integer.parseInt(partesMensaje[1]));
                                if (equiposInscribibles != null) {
                                    respuesta = Mensajes.TOURNAMENTS_TEAMS_INSCRIBE_USER_OK + ";" + equiposInscribibles;
                                } else {
                                    respuesta = Mensajes.TOURNAMENTS_TEAMS_INSCRIBE_USER_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.TOURNAMENTS_TEAMS_INSCRIBE_USER_ERROR;
                            }

                            break;

                        case Mensajes.TOURNAMENTS_INSCRIBE_TEAM_USER:

                            if (!partesMensaje[1].equalsIgnoreCase("")) {
                                if (jdbc.insertarEquipoEnTorneo(Integer.parseInt(partesMensaje[1]), partesMensaje[2])) {
                                    respuesta = Mensajes.TOURNAMENTS_INSCRIBE_TEAM_USER_OK;
                                } else {
                                    respuesta = Mensajes.TOURNAMENTS_INSCRIBE_TEAM_USER_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.TOURNAMENTS_INSCRIBE_TEAM_USER_ERROR;
                            }
                            break;

                        case Mensajes.TOURNAMENTS_ROUNDS:
                            String rondas;
                            if (!partesMensaje[1].equalsIgnoreCase("")) {
                                rondas = jdbc.rondasTorneo(Integer.parseInt(partesMensaje[1]));
                                if (rondas != null) {
                                    respuesta = Mensajes.TOURNAMENTS_ROUNDS_OK + ";" + rondas;
                                } else {
                                    respuesta = Mensajes.TOURNAMENTS_ROUNDS_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.TOURNAMENTS_ROUNDS_ERROR;
                            }
                            break;
                        case Mensajes.SAVE_ROUNDS:
                            //IdRonda;eL;pL;eV;pV
                            if (jdbc.guardarRonda(Integer.parseInt(partesMensaje[1]), partesMensaje[2],
                                    Integer.parseInt(partesMensaje[3]), partesMensaje[4], Integer.parseInt(partesMensaje[5]))) {
                                respuesta = Mensajes.SAVE_ROUNDS_OK;
                            } else {
                                respuesta = Mensajes.SAVE_ROUNDS_ERROR;
                            }

                            break;

                        case Mensajes.GAME_CREATE:
                            //ubicacion;fInicio;hInicio;fLimite;hLimite;coste;deporte;creador
                            int idPartido = jdbc.insertarPartido(partesMensaje[1], partesMensaje[2], partesMensaje[3], partesMensaje[4],
                                    partesMensaje[5], partesMensaje[6], partesMensaje[7], idUsuario);

                            if (idPartido != -1) {
                                respuesta = Mensajes.GAME_CREATE_OK + ";" + idPartido;
                            } else {
                                respuesta = Mensajes.GAME_CREATE_ERROR;
                            }

                            break;

                        case Mensajes.GAMES_USER:
                            String partidosUsu = jdbc.partidosIncristosUsuario(idUsuario);
                            if (partidosUsu != null) {
                                respuesta = Mensajes.GAMES_USER_OK + ";" + partidosUsu;
                            } else {
                                respuesta = Mensajes.GAMES_USER_ERROR;
                            }

                            break;

                        case Mensajes.GAMES_STATE_USER:
                            String partidoEstad;
                            if (!partesMensaje[1].equalsIgnoreCase("")) {
                                partidoEstad = jdbc.partidoEstadoUsuario(idUsuario, Integer.parseInt(partesMensaje[1]));
                                if (partidoEstad != null) {
                                    respuesta = Mensajes.GAMES_STATE_USER_OK + ";" + partidoEstad;
                                } else {
                                    respuesta = Mensajes.GAMES_STATE_USER_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.GAMES_STATE_USER_ERROR;
                            }
                            break;

                        case Mensajes.GAMES_INSCRIBE_USER:
                            if (!partesMensaje[1].equalsIgnoreCase("")) {

                                if (jdbc.inscribirUsuarioPartido(idUsuario, Integer.parseInt(partesMensaje[1]))) {
                                    respuesta = Mensajes.GAMES_INSCRIBE_USER_OK;
                                } else {
                                    respuesta = Mensajes.GAMES_INSCRIBE_USER_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.GAMES_INSCRIBE_USER_ERROR;
                            }
                            break;

                        case Mensajes.GAMES_AVAILABLE:
                            String partidosDispo;
                            if (!partesMensaje[1].equalsIgnoreCase("")) {
                                partidosDispo = jdbc.partidosDisponibles(partesMensaje[1], Float.parseFloat(partesMensaje[2]));
                                if (partidosDispo != null) {
                                    respuesta = Mensajes.GAMES_AVAILABLE_OK + ";" + partidosDispo;
                                } else {
                                    respuesta = Mensajes.GAMES_AVAILABLE_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.GAMES_AVAILABLE_ERROR;
                            }
                            break;

                        case Mensajes.LIGUES_NUMBER_ROUNDS:
                            String cantidad;
                            if (!partesMensaje[1].equalsIgnoreCase("")) {
                                cantidad = jdbc.cantidadDeJornadasLiga(Integer.parseInt(partesMensaje[1]));
                                if (cantidad != null) {
                                    respuesta = Mensajes.LIGUES_NUMBER_ROUNDS_OK + ";" + cantidad;
                                } else {
                                    respuesta = Mensajes.LIGUES_NUMBER_ROUNDS_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.LIGUES_NUMBER_ROUNDS_ERROR;
                            }
                            break;

                        case Mensajes.LIGUES_ROUND_DATE:
                            String rondasDato;
                            if (!partesMensaje[1].equalsIgnoreCase("")) {
                                rondasDato = jdbc.jornadaLiga(Integer.parseInt(partesMensaje[1]), Integer.parseInt(partesMensaje[2]));
                                if (rondasDato != null) {
                                    respuesta = Mensajes.LIGUES_ROUND_DATE_OK + ";" + rondasDato;
                                } else {
                                    respuesta = Mensajes.LIGUES_ROUND_DATE_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.LIGUES_ROUND_DATE_ERROR;
                            }
                            break;
                        case Mensajes.LIGUES_JORNATE_SAVE:
                            //IdRonda;eL;pL;eV;pV
                            if (jdbc.guardarJornada(Integer.parseInt(partesMensaje[1]), jdbc.obtenerIdEquipoSegunNombre(partesMensaje[2]),
                                    Integer.parseInt(partesMensaje[3]), jdbc.obtenerIdEquipoSegunNombre(partesMensaje[4]), Integer.parseInt(partesMensaje[5]))) {
                                respuesta = Mensajes.LIGUES_JORNATE_SAVE_OK;
                            } else {
                                respuesta = Mensajes.LIGUES_JORNATE_SAVE_ERROR;
                            }

                            break;

                        case Mensajes.LIGUES_SCORE:
                            String clasificacion;
                            if (!partesMensaje[1].equalsIgnoreCase("")) {
                                clasificacion = jdbc.clasificacionDeLiga(Integer.parseInt(partesMensaje[1]));
                                if (clasificacion != null) {
                                    respuesta = Mensajes.LIGUES_SCORE_OK + ";" + clasificacion;
                                } else {
                                    respuesta = Mensajes.LIGUES_SCORE_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.LIGUES_SCORE_ERROR;
                            }
                            break;

                        case Mensajes.LIGUES_END:

                            if (jdbc.finalizarLiga(Integer.parseInt(partesMensaje[1]))) {
                                respuesta = (Mensajes.LIGUES_END_OK);
                            } else {
                                respuesta = (Mensajes.LIGUES_END_ERROR);
                            }

                            break;

                        case Mensajes.LIGUES_CANCEL:

                            if (jdbc.cancelarLiga(Integer.parseInt(partesMensaje[1]))) {
                                respuesta = (Mensajes.LIGUES_CANCEL_OK);
                            } else {
                                respuesta = (Mensajes.LIGUES_CANCEL_ERROR);
                            }

                            break;

                        case Mensajes.LIGUES_USER:
                            String ligasUso = jdbc.ligasIncristosUsuario(idUsuario);
                            if (ligasUso != null) {
                                respuesta = Mensajes.LIGUES_USER_OK + ";" + ligasUso;
                            } else {
                                respuesta = Mensajes.LIGUES_USER_ERROR;
                            }

                            break;

                        case Mensajes.LIGUES_STATE_USER:
                            String ligaEstad;
                            if (!partesMensaje[1].equalsIgnoreCase("")) {
                                ligaEstad = jdbc.ligaEstadoUsuario(idUsuario, Integer.parseInt(partesMensaje[1]));
                                if (ligaEstad != null) {
                                    respuesta = Mensajes.LIGUES_STATE_USER_OK + ";" + ligaEstad;
                                } else {
                                    respuesta = Mensajes.LIGUES_STATE_USER_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.LIGUES_STATE_USER_ERROR;
                            }
                            break;

                        case Mensajes.LIGUES_INSCRIBE_TEAM_USER:

                            if (!partesMensaje[1].equalsIgnoreCase("")) {
                                if (jdbc.insertarEquipoEnLiga(Integer.parseInt(partesMensaje[1]), partesMensaje[2])) {
                                    respuesta = Mensajes.LIGUES_INSCRIBE_TEAM_USER_OK;
                                } else {
                                    respuesta = Mensajes.LIGUES_INSCRIBE_TEAM_USER_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.LIGUES_INSCRIBE_TEAM_USER_ERROR;
                            }
                            break;

                        case Mensajes.LIGUES_TEAMS_INSCRIBE_USER:

                            String equiposInscribiblesLiga;
                            if (!partesMensaje[1].equalsIgnoreCase("")) {
                                equiposInscribiblesLiga = jdbc.tusEquiposInscribiblesLiga(idUsuario, Integer.parseInt(partesMensaje[1]));
                                if (equiposInscribiblesLiga != null) {
                                    respuesta = Mensajes.LIGUES_TEAMS_INSCRIBE_USER_OK + ";" + equiposInscribiblesLiga;
                                } else {
                                    respuesta = Mensajes.LIGUES_TEAMS_INSCRIBE_USER_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.LIGUES_TEAMS_INSCRIBE_USER_ERROR;
                            }

                            break;

                        case Mensajes.LIGUES_AVAILABLE:
                            String ligasDispo;
                            if (!partesMensaje[1].equalsIgnoreCase("")) {
                                ligasDispo = jdbc.ligasDisponibles(partesMensaje[1], Float.parseFloat(partesMensaje[2]));
                                if (ligasDispo != null) {
                                    respuesta = Mensajes.LIGUES_AVAILABLE_OK + ";" + ligasDispo;
                                } else {
                                    respuesta = Mensajes.LIGUES_AVAILABLE_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.LIGUES_AVAILABLE_ERROR;
                            }
                            break;

                        case Mensajes.TEAMS_PRIVACITY:
                            String privacidades = jdbc.privacidadDisponibles();

                            if (privacidades != null) {
                                respuesta = (Mensajes.TEAMS_PRIVACITY_OK + ";" + privacidades);
                            } else {
                                respuesta = (Mensajes.TEAMS_PRIVACITY_ERROR);
                            }

                            break;

                        case Mensajes.USER_INVITES:
                            String invitacionesUsuario = jdbc.invitacionesAEquipo(idUsuario);

                            if (invitacionesUsuario != null) {
                                respuesta = (Mensajes.USER_INVITES_OK + ";" + invitacionesUsuario);
                            } else {
                                respuesta = (Mensajes.USER_INVITES_ERROR);
                            }

                            break;

                        case Mensajes.USER_ANSWER_INVITE:
                            int idInvitacion = Integer.parseInt(partesMensaje[1]);
                            String respuestaInvitacion = partesMensaje[2];

                            if (jdbc.respuestaInvitacionEquipo(idInvitacion, respuestaInvitacion, idUsuario)) {
                                respuesta = (Mensajes.USER_ANSWER_INVITE_OK);
                            } else {
                                respuesta = (Mensajes.USER_ANSWER_INVITE_ERROR);
                            }

                            break;

                        case Mensajes.SEARCH_TEAMS:
                            String equiposDispo;
                            if (!partesMensaje[1].equalsIgnoreCase("")) {
                                equiposDispo = jdbc.equiposDisponibles(partesMensaje[1], Float.parseFloat(partesMensaje[2]));
                                if (equiposDispo != null) {
                                    respuesta = Mensajes.SEARCH_TEAMS_OK + ";" + equiposDispo;
                                } else {
                                    respuesta = Mensajes.SEARCH_TEAMS_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.SEARCH_TEAMS_ERROR;
                            }
                            break;

                        case Mensajes.JOIN_TEAM:
                            String nombreEquip = partesMensaje[1];
                            String nombreUsuari = partesMensaje[2];
                            int idEq = jdbc.obtenerIdEquipoSegunNombre(nombreEquip);
                            int idUs = jdbc.obtenerIdUsuarioSegunNombre(nombreUsuari);

                            if (jdbc.registrarUsuarioEnEquipo(idEq, idUs)) {
                                respuesta = Mensajes.JOIN_TEAM_OK + ";" + nombreUsuari;
                            } else {
                                respuesta = Mensajes.JOIN_TEAM_ERROR;
                            }
                            break;

                        case Mensajes.SOLICTE_JOIN_TEAM:
                            String nombreEqui = partesMensaje[1];
                            String nombreUsuar = partesMensaje[2];
                            int idEqu = jdbc.obtenerIdEquipoSegunNombre(nombreEqui);
                            int idUsu = jdbc.obtenerIdUsuarioSegunNombre(nombreUsuar);

                            if (jdbc.solicitudUnionEquipo(idEqu, idUsu)) {
                                respuesta = Mensajes.SOLICTE_JOIN_TEAM_OK;
                            } else {
                                respuesta = Mensajes.SOLICTE_JOIN_TEAM_ERROR;
                            }
                            break;

                        case Mensajes.USER_IS_INVITE:
                            String nombreEqu = partesMensaje[1];
                            String nombreUsua = partesMensaje[2];
                            int idEqui = jdbc.obtenerIdEquipoSegunNombre(nombreEqu);
                            int idUsua = jdbc.obtenerIdUsuarioSegunNombre(nombreUsua);

                            if (jdbc.usuarioSolicitoInscripccion(idEqui, idUsua)) {
                                respuesta = Mensajes.USER_IS_INVITE_OK;
                            } else {
                                respuesta = Mensajes.USER_IS_INVITE_ERROR;
                            }
                            break;

                        case Mensajes.TEAM_INVITES:

                            int eq = jdbc.obtenerIdEquipoSegunNombre(partesMensaje[1]);

                            String invitacionesEquipo = jdbc.invitacionesAlEquipo(eq);

                            if (invitacionesEquipo != null) {
                                respuesta = (Mensajes.TEAM_INVITES_OK + ";" + invitacionesEquipo);
                            } else {
                                respuesta = (Mensajes.TEAM_INVITES_ERROR);
                            }

                            break;

                        case Mensajes.TEAM_ANSWER_INVITE:
                            int idInvitac = Integer.parseInt(partesMensaje[1]);
                            String respuestaInvitaci = partesMensaje[2];

                            if (jdbc.respuestaInvitacionUsuario(idInvitac, respuestaInvitaci)) {
                                respuesta = (Mensajes.TEAM_ANSWER_INVITE_OK);
                            } else {
                                respuesta = (Mensajes.TEAM_ANSWER_INVITE_ERROR);
                            }

                            break;

                        case Mensajes.TEAM_LEAVE:
                            int equ = jdbc.obtenerIdEquipoSegunNombre(partesMensaje[1]);

                            if (jdbc.abandonarEquipo(equ, idUsuario)) {
                                respuesta = (Mensajes.TEAM_LEAVE_OK);
                            } else {
                                respuesta = (Mensajes.TEAM_LEAVE_ERROR);
                            }

                            break;

                        case Mensajes.TEAM_DELETE:
                            int equip = jdbc.obtenerIdEquipoSegunNombre(partesMensaje[1]);

                            if (jdbc.borrarEquipo(equip)) {
                                respuesta = (Mensajes.TEAM_DELETE_OK);
                            } else {
                                respuesta = (Mensajes.TEAM_DELETE_ERROR);
                            }

                            break;

                        case Mensajes.TEAM_MODIFY:

                            int nombreE = jdbc.obtenerIdEquipoSegunNombre(partesMensaje[1]);
                            String nUbica = partesMensaje[2];
                            int nPriv = jdbc.obtenerIdPrivacidad(partesMensaje[3]);

                            if (jdbc.modificarEquipo(nombreE, nUbica, nPriv)) {
                                respuesta = Mensajes.TEAM_MODIFY_OK;
                            } else {
                                respuesta = Mensajes.TEAM_MODIFY_ERROR;
                            }

                            break;

                        case Mensajes.INSERT_PAY:
                            double precio = Double.parseDouble(partesMensaje[1]);

                            String pago = jdbc.insertarPago(idUsuario, precio);
                            if (pago != null) {
                                respuesta = (Mensajes.INSERT_PAY_OK + ";" + pago);
                            } else {
                                respuesta = (Mensajes.INSERT_PAY_ERROR);
                            }
                            break;

                        case Mensajes.GAMES_INSCRIBE_USER_PAY:
                            if (!partesMensaje[1].equalsIgnoreCase("")) {

                                if (jdbc.inscribirUsuarioPartidoDePago(idUsuario, Integer.parseInt(partesMensaje[1]), Integer.parseInt(partesMensaje[2]))) {
                                    respuesta = Mensajes.GAMES_INSCRIBE_USER_PAY_OK;
                                } else {
                                    respuesta = Mensajes.GAMES_INSCRIBE_USER_PAY_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.GAMES_INSCRIBE_USER_PAY_ERROR;
                            }
                            break;

                        case Mensajes.LIGUES_INSCRIBE_USER_PAY:

                            if (!partesMensaje[1].equalsIgnoreCase("")) {
                                if (jdbc.insertarEquipoEnLigaDePago(Integer.parseInt(partesMensaje[1]), partesMensaje[2], Integer.parseInt(partesMensaje[3]))) {
                                    respuesta = Mensajes.LIGUES_INSCRIBE_USER_PAY_OK;
                                } else {
                                    respuesta = Mensajes.LIGUES_INSCRIBE_USER_PAY_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.LIGUES_INSCRIBE_USER_PAY_ERROR;
                            }
                            break;
                        case Mensajes.TOURNAMENTS_INSCRIBE_USER_PAY:

                            if (!partesMensaje[1].equalsIgnoreCase("")) {
                                if (jdbc.insertarEquipoEnTorneoDePago(Integer.parseInt(partesMensaje[1]), partesMensaje[2], Integer.parseInt(partesMensaje[3]))) {
                                    respuesta = Mensajes.TOURNAMENTS_INSCRIBE_USER_PAY_OK;
                                } else {
                                    respuesta = Mensajes.TOURNAMENTS_INSCRIBE_USER_PAY_ERROR;
                                }
                            } else {
                                respuesta = Mensajes.TOURNAMENTS_INSCRIBE_USER_PAY_ERROR;
                            }
                            break;

                        case Mensajes.USER_EMAIL:
                            String emailUsuario = jdbc.emailUsuario(idUsuario);
                            if (emailUsuario != null) {
                                respuesta = (Mensajes.USER_EMAIL_OK + ";" + emailUsuario);
                            } else {
                                respuesta = (Mensajes.USER_EMAIL_ERROR);
                            }
                            break;

                        case Mensajes.CHANGE_DATA:
                            String nuevoEmail = partesMensaje[1];
                            String pass = " ";
                            if (partesMensaje.length > 2) {
                                pass = Encriptador.generate512(partesMensaje[2]);
                            }

                            if (jdbc.modificarDatosUsuario(nuevoEmail, pass, idUsuario)) {
                                respuesta = Mensajes.CHANGE_DATA_OK;
                            } else {
                                respuesta = Mensajes.CHANGE_DATA_ERROR;
                            }

                            break;
                        case Mensajes.GET_SPORT_ADMIN:
                            String deportesAdmin = jdbc.deportesDisponiblesAdmin();

                            if (deportesAdmin != null) {
                                respuesta = (Mensajes.GET_SPORT_ADMIN_OK + ";" + deportesAdmin);
                            } else {
                                respuesta = (Mensajes.GET_SPORT_ADMIN_ERROR);
                            }
                            break;
                        case Mensajes.ADD_SPORT_ADMIN:
                            String deporteNuevo = partesMensaje[1];
                            int cantidadTo = Integer.parseInt(partesMensaje[2]);
                            int cantidadEq = Integer.parseInt(partesMensaje[3]);
                            if (jdbc.insertarDeporte(deporteNuevo, cantidadTo, cantidadEq)) {
                                respuesta = Mensajes.ADD_SPORT_ADMIN_OK;
                            } else {
                                respuesta = Mensajes.ADD_SPORT_ADMIN_ERROR;
                            }
                            break;
                        case Mensajes.SHOW_TEAM_ADMIN:
                            String todosEquipos = jdbc.equiposAdmin();
                            if (todosEquipos != null) {
                                respuesta = (Mensajes.SHOW_TEAM_ADMIN_OK + ";" + todosEquipos);
                            } else {
                                respuesta = (Mensajes.SHOW_TEAM_ADMIN_ERROR);
                            }
                            break;
                        case Mensajes.SHOW_USER_ADMIN:
                            String todosUsuarios = jdbc.usuariosAdmin();
                            if (todosUsuarios != null) {
                                respuesta = (Mensajes.SHOW_USER_ADMIN_OK + ";" + todosUsuarios);
                            } else {
                                respuesta = (Mensajes.SHOW_USER_ADMIN_ERROR);
                            }
                            break;
                        case Mensajes.ADD_USERADMIN_ADMIN:
                            int idUsuarioEscogido = Integer.parseInt(partesMensaje[1]);
                            if (jdbc.asignarRolAdministrador(idUsuarioEscogido)) {
                                respuesta = (Mensajes.ADD_USERADMIN_ADMIN_OK);
                            } else {
                                respuesta = (Mensajes.ADD_USERADMIN_ADMIN_ERROR);
                            }
                            break;
                        case Mensajes.SHOW_GAME_ADMIN:
                            String todoPartidos = jdbc.partidosAdmin();
                            if (todoPartidos != null) {
                                respuesta = (Mensajes.SHOW_GAME_ADMIN_OK + ";" + todoPartidos);
                            } else {
                                respuesta = (Mensajes.SHOW_GAME_ADMIN_ERROR);
                            }
                            break;
                        case Mensajes.DELETE_GAME_ADMIN:
                            if (jdbc.eliminarPartido(Integer.parseInt(partesMensaje[1]))) {
                                respuesta = (Mensajes.DELETE_GAME_ADMIN_OK);
                            } else {
                                respuesta = (Mensajes.DELETE_GAME_ADMIN_ERROR);
                            }
                            break;
                        case Mensajes.SHOW_TOURNAMENT_ADMIN:
                            String todosTorneo = jdbc.torneosAdmin();
                            if (todosTorneo != null) {
                                respuesta = (Mensajes.SHOW_TOURNAMENT_ADMIN_OK + ";" + todosTorneo);
                            } else {
                                respuesta = (Mensajes.SHOW_TOURNAMENT_ADMIN_ERROR);
                            }
                            break;
                        case Mensajes.DELETE_TOURNAMENT_ADMIN:
                            if (jdbc.eliminarTorneo(Integer.parseInt(partesMensaje[1]))) {
                                respuesta = (Mensajes.DELETE_TOURNAMENT_ADMIN_OK);
                            } else {
                                respuesta = (Mensajes.DELETE_TOURNAMENT_ADMIN_ERROR);
                            }
                            break;
                        case Mensajes.SHOW_LIGUE_ADMIN:
                            String todasLigas = jdbc.ligasAdmin();
                            if (todasLigas != null) {
                                respuesta = (Mensajes.SHOW_LIGUE_ADMIN_OK + ";" + todasLigas);
                            } else {
                                respuesta = (Mensajes.SHOW_LIGUE_ADMIN_ERROR);
                            }
                            break;
                        case Mensajes.DELETE_LIGUE_ADMIN:
                            if (jdbc.eliminarLiga(Integer.parseInt(partesMensaje[1]))) {
                                respuesta = (Mensajes.DELETE_LIGUE_ADMIN_OK);
                            } else {
                                respuesta = (Mensajes.DELETE_LIGUE_ADMIN_ERROR);
                            }
                            break;
                        case Mensajes.REQUEST_SPORT:
                            String nombreDto = partesMensaje[1];
                            int ctdadT = Integer.parseInt(partesMensaje[2]);
                            int ctdadE = Integer.parseInt(partesMensaje[3]);
                            String desc = partesMensaje[4];
                            if (jdbc.insertarSolicitudDeporte(nombreDto, ctdadT, ctdadE, desc, idUsuario)) {
                                respuesta = (Mensajes.REQUEST_SPORT_OK);
                            } else {
                                respuesta = (Mensajes.REQUEST_SPORT_ERROR);
                            }
                            break;
                        case Mensajes.PETITION_SPORT:
                            String todasPeticiones = jdbc.peticiones();
                            if (todasPeticiones != null) {
                                respuesta = (Mensajes.PETITION_SPORT_OK + ";" + todasPeticiones);
                            } else {
                                respuesta = (Mensajes.PETITION_SPORT_ERROR);
                            }
                            break;

                        case Mensajes.PETITION_ANSWER:
                            int estado = -1;
                            if(partesMensaje[2].equalsIgnoreCase("SI")){
                                estado = 6;
                            }else{
                                estado = 7;
                            }

                            if (jdbc.cambiarEstadoPeticion(Integer.parseInt(partesMensaje[1]), estado)) {
                                respuesta = Mensajes.PETITION_ANSWER_OK;
                            } else {
                                respuesta = Mensajes.PETITION_ANSWER_ERROR;
                            }
                            break;

                        case Mensajes.EXIT:
                            respuesta = "SE HA DESCONECTADO";
                            estado = SALIR;
                            break;
                        default:
                            respuesta = (Mensajes.ERROR);
                            break;

                    }
                    break;
                case SALIR:
                    respuesta = "SE HA DESCONECTADO";
                    estado = SALIR;
                    break;

                default:
                    respuesta = (Mensajes.ERROR);

                    break;
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            hacerLog("¡FALTAN DATOS EN LA PETICION!");
            respuesta = (Mensajes.ERROR);
        } catch (Exception ex) {
            hacerLog("ERROR: " + ex.getMessage());
            respuesta = (Mensajes.ERROR);
        }
        hacerLog(respuesta);
        return respuesta;
    }

    private void hacerLog(String infor) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String ahora = dtf.format(LocalDateTime.now());

        String log = "[" + ahora + "] " + idUsuario + "-" + nombre + ":" + infor;

        System.out.println(log);
        LogServer.guardarLog(log);
    }

}
