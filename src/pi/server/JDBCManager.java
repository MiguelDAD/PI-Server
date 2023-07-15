/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pi.server;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

/**
 *
 * @author Miguel
 */
public class JDBCManager {

    private String URL;
    private String USER;
    private String PASS;

    public JDBCManager() {
        iniciarParametros();
    }

    private void iniciarParametros() {
        LectorINI lINI = new LectorINI();

        URL = lINI.getURL();
        USER = lINI.getUSER();
        PASS = lINI.getPASS();
    }

    public int iniciarSesionOrganizador(String usuario, String passwd) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM usuario WHERE nombre = ? AND passwd = ?");
            preparedStatement.setString(1, usuario);
            preparedStatement.setString(2, passwd);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                //SI ENTRA ES PORQ EXISTE EL USUARIO
                int idUsuario = rset.getInt(1);

                //COMPRUEBA SI ES USUARIO PARA DARLE EL ROL USUARIO SI NO
                if (esOrganizador(idUsuario)) {
                    return idUsuario;
                } else {
                    asignarRolOrganizador(idUsuario);
                    return idUsuario;
                }

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public boolean esOrganizador(int id) {
        //ROL ORGANIZADOR = 2
        int organizador = 2;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) FROM rolasociado WHERE usuario = ? AND rol = ?");
            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, organizador);
            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                if (rset.getInt(1) == 1) {
                    return true;
                }
                return false;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;

    }

    public boolean esAdministrador(int id) {
        //ROL ADMINISTRADOR = 1
        int administrador = 1;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) FROM rolasociado WHERE usuario = ? AND rol = ?");
            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, administrador);
            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                if (rset.getInt(1) == 1) {
                    return true;
                }
                return false;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;

    }

    public int iniciarSesionUsuario(String usuario, String passwd) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM usuario WHERE nombre = ? AND passwd = ?");
            preparedStatement.setString(1, usuario);
            preparedStatement.setString(2, passwd);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                //SI ENTRA ES PORQ EXISTE EL USUARIO
                int idUsuario = rset.getInt(1);

                //COMPRUEBA SI ES USUARIO PARA DARLE EL ROL USUARIO SI NO
                if (esUsuario(idUsuario)) {
                    return idUsuario;
                } else {
                    asignarRolUsuario(idUsuario);
                    return idUsuario;
                }

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public boolean registrarUsuarioUsuario(String usuario, String email, String passwd) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO usuario(nombre,email,passwd) VALUES(?,?,?)");
            preparedStatement.setString(1, usuario);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, passwd);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                //DEVUELVE TRUE SI LE ASIGNA EL ROL USUARIO SIN PROBLEMA
                return asignarRolUsuario(obtenerIdUsuarioSegunNombre(usuario));
            } else {
                return false;
            }
        } catch (SQLException ex) {
            return false;
        }
    }

    public boolean esUsuario(int id) {
        //ROL usuario = 3
        int usuario = 3;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) FROM rolasociado WHERE usuario = ? AND rol = ?");
            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, usuario);
            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                if (rset.getInt(1) == 1) {
                    return true;
                }
                return false;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;

    }

    public boolean asignarRolUsuario(int id) {
        //ROL USUARIO = 3
        int usuario = 3;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO rolasociado(rol,usuario) VALUES(?,?)");
            preparedStatement.setInt(1, usuario);
            preparedStatement.setInt(2, id);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;

    }

    public int obtenerIdUsuarioSegunNombre(String nombre) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM usuario WHERE nombre = ?");
            preparedStatement.setString(1, nombre);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                return rset.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public boolean registrarUsuarioOrganizador(String usuario, String email, String passwd) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO usuario(nombre,email,passwd) VALUES(?,?,?)");
            preparedStatement.setString(1, usuario);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, passwd);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                //DEVUELVE TRUE SI LE ASIGNA EL ROL ORGANIZADOR SIN PROBLEMA
                return asignarRolOrganizador(obtenerIdUsuarioSegunNombre(usuario));
            } else {
                return false;
            }
        } catch (SQLException ex) {
            return false;
        }
    }

    public boolean asignarRolOrganizador(int id) {
        //ROL ORGANIZADOR = 2
        int organizador = 2;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO rolasociado(rol,usuario) VALUES(?,?)");
            preparedStatement.setInt(1, organizador);
            preparedStatement.setInt(2, id);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;

    }

    public String ligasDeUnOrganizador(int id) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            //id:nombre:ubicacion:coste:maxEquipos:minEquipos:horaInicio:fechaInicio:horaLimite:fechaLimite:deporte:frecuenciaJornada:duracionestado:equiposInscritos
            String ligaInfo = "";
            int idL = 0;

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT l.id,l.nombre,l.ubicacion,l.coste,l.maxEquipos,l.minEquipos,l.hInicio,l.fInicio,l.hLimite,l.fLimite,d.nombreDto,l.frecuenciaJornada,l.duracionPartido,l.horaInicioPartidos,l.horaFinPartidos,e.nombre "
                    + "FROM liga l JOIN deporte d ON l.deporte = d.id JOIN estado e ON l.estado = e.id WHERE l.organizador = ? ORDER BY estado ASC");
            preparedStatement.setInt(1, id);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                idL = rset.getInt(1);
                for (int i = 1; i <= 16; i++) {
                    ligaInfo = ligaInfo + rset.getString(i) + "!";
                }
                ligaInfo = ligaInfo + ctdaEquiposInscritosLiga(idL) + ";";
            }

            return ligaInfo;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public int ctdaEquiposInscritosLiga(int idLiga) {

        //SELECT COUNT(*) FROM inscrippciontorneo WHERE torneo = 1
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) FROM inscrippcionliga WHERE liga = ?");
            preparedStatement.setInt(1, idLiga);
            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                return rset.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;

    }

    public int ctdaEquiposInscritosTorneo(int idTorneo) {

        //SELECT COUNT(*) FROM inscrippciontorneo WHERE torneo = 1
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) FROM inscrippciontorneo WHERE torneo = ?");
            preparedStatement.setInt(1, idTorneo);
            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                return rset.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;

    }

    public String deportesDisponibles() {

        String deportes = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT nombreDto, ctdadJugadores FROM deporte ORDER BY nombreDto");

            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                deportes = deportes + rset.getString(1) + ":" + rset.getString(2) + ";";
            }

            return deportes;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;

    }

    public String deportesDisponiblesAdmin() {

        String deportes = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT id, nombreDto, ctdadJugadores, ctdadXequipo FROM deporte ORDER BY id");

            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                deportes = deportes + rset.getInt(1) + ":" + rset.getString(2) + ":" + rset.getInt(3) + ":" + rset.getInt(4) + ";";
            }

            return deportes;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;

    }

    public boolean insertarTorneo(String torneo, int idOrganizador) {

        //nombre!ubicacion!coste!maxEquipos!minEquipos!horaInicio!fechaInicio!horaLimite!fechaLimite!deporte
        String[] partesTorneo = torneo.split("!");
        int idDeporte = obtenerIdDeporte(partesTorneo[9]);

        if (idDeporte == 0) {
            return false;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO torneo(nombre,ubicacion,coste,maxEquipos,minEquipos,hInicio,fInicio,hLimite,fLimite,deporte,organizador,enfrentamientosGenerados,estado) VALUES(?,?,?,?,?,?,?,?,?,?,?,False,2)",
                    Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, partesTorneo[0]);
            preparedStatement.setString(2, partesTorneo[1].replaceAll(" ", ""));
            preparedStatement.setDouble(3, Double.parseDouble(partesTorneo[2]));
            preparedStatement.setInt(4, Integer.parseInt(partesTorneo[3]));
            preparedStatement.setInt(5, Integer.parseInt(partesTorneo[4]));
            preparedStatement.setString(6, partesTorneo[5]);
            preparedStatement.setString(7, partesTorneo[6]);
            preparedStatement.setString(8, partesTorneo[7]);
            preparedStatement.setString(9, partesTorneo[8]);
            preparedStatement.setInt(10, idDeporte);
            preparedStatement.setInt(11, idOrganizador);
            //System.out.println(preparedStatement);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {

                ResultSet rs = preparedStatement.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm yyyy-MM-dd");
                    LocalDateTime dateTime = LocalDateTime.parse(partesTorneo[7] + " " + partesTorneo[8], formatter);

                    PIServer.tareasPendientes.agregarTarea(new Tarea(dateTime, TiposDeEnfrentamientos.GENERATE_FIGHT_TOURNAMENTS + ";" + id));
                }

                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean cancelarTorneo(int idTorneo) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("UPDATE torneo SET estado = 3 WHERE id = ?");
            preparedStatement.setInt(1, idTorneo);

            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {

                PIServer.tareasPendientes.eliminarTarea(TiposDeEnfrentamientos.GENERATE_FIGHT_TOURNAMENTS + ";" + idTorneo);
                return true;

            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean finalizarTorneo(int idTorneo) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("UPDATE torneo SET estado = 4 WHERE id = ?");
            preparedStatement.setInt(1, idTorneo);

            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {

                return true;

            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public int obtenerIdPrivacidad(String privacidad) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT id FROM privacidad WHERE nombre LIKE ?");
            preparedStatement.setString(1, privacidad);

            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                return rset.getInt(1);
            }

            return 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public int obtenerIdDeporte(String deporte) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT id FROM deporte WHERE nombreDto LIKE ?");
            preparedStatement.setString(1, deporte);

            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                return rset.getInt(1);
            }

            return 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public String equiposDeUnTorneo(int id) {

        String equipos = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT e.id, e.nombre, u.nombre FROM inscrippciontorneo i JOIN equipo e ON (i.equipo = e.id) JOIN usuario u ON (e.lider = u.id) WHERE i.torneo = ?");
            preparedStatement.setInt(1, id);

            ResultSet rset = preparedStatement.executeQuery();

            //nombreEquipo:lider:integrantes1!integrante2;
            while (rset.next()) {
                equipos = equipos + rset.getString(2) + ":" + rset.getString(3) + ":" + integrantesDeUnEquipo(rset.getInt(1)) + ";";

            }

            return equipos;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String equiposDeUnTorneoParaUsuario(int id) {

        String equipos = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT e.id, e.nombre, u.nombre, d.nombreDto FROM inscrippciontorneo i JOIN equipo e ON (i.equipo = e.id) JOIN usuario u ON (e.lider = u.id) JOIN deporte d ON (e.deporte = d.id) WHERE i.torneo = ?");
            preparedStatement.setInt(1, id);

            ResultSet rset = preparedStatement.executeQuery();

            //nombreEquipo:lider:deporte;
            while (rset.next()) {
                equipos = equipos + rset.getString(2) + ":" + rset.getString(3) + ":" + rset.getString(4) + ";";

            }

            return equipos;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String integrantesDeUnEquipoSinLider(int id, String lider) {
        String integrantes = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT u.nombre FROM componenteequipo e JOIN usuario u ON (e.usuario = u.id) WHERE u.nombre NOT LIKE ? AND e.equipo = ?");
            preparedStatement.setString(1, lider);
            preparedStatement.setInt(2, id);

            ResultSet rset = preparedStatement.executeQuery();

            //nombreEquipo:lider:integrante1!integrante2;
            while (rset.next()) {
                integrantes = integrantes + rset.getString(1) + "!";

            }

            return integrantes;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String integrantesDeUnEquipo(int id) {
        String integrantes = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT u.nombre FROM componenteequipo e JOIN usuario u ON (e.usuario = u.id) WHERE e.equipo = ?");

            preparedStatement.setInt(1, id);

            ResultSet rset = preparedStatement.executeQuery();

            //nombreEquipo:lider:integrante1!integrante2;
            while (rset.next()) {
                integrantes = integrantes + rset.getString(1) + "!";

            }

            return integrantes;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String torneosDeUnOrganizador(int id) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            //id!nombre!ubicacion!coste!maxEquipos!minEquipos!horaInicio!fechaInicio!horaLimite!fechaLimite
            //!deporte!frecuenciaJornada!duracionPartido!horaInicioPartidos!horaFinPartidos!estado!equiposInscritos
            String torneoInfo = "";
            int idT = 0;

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT t.id,t.nombre,t.ubicacion,t.coste,t.maxEquipos,t.minEquipos,t.hInicio,t.fInicio,t.hLimite,t.fLimite,d.nombreDto,e.nombre FROM torneo t JOIN deporte d ON t.deporte = d.id JOIN estado e ON t.estado=e.id  WHERE t.organizador = ? ORDER BY t.estado ASC");
            preparedStatement.setInt(1, id);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                idT = rset.getInt(1);
                for (int i = 2; i <= 12; i++) {
                    torneoInfo = torneoInfo + rset.getString(i) + "!";
                }
                torneoInfo = torneoInfo + ctdaEquiposInscritosTorneo(idT) + "!" + idT + ";";
            }

            return torneoInfo;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public boolean insertarLiga(String liga, int idOrganizador) {

        //nombre!ubicacion!coste!maxEquipos!minEquipos!horaInicio!fechaInicio!horaLimite!fechaLimite
        //!deporte!frecuenciaJornada!duracionPartido!horaInicioPartidos!horaFinPartidos
        String[] partesLiga = liga.split("!");
        int idDeporte = obtenerIdDeporte(partesLiga[9]);

        if (idDeporte == 0) {
            return false;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO liga(nombre,ubicacion,coste,maxEquipos,minEquipos,hInicio,fInicio,hLimite,fLimite,deporte,organizador,frecuenciaJornada,duracionPartido,horaInicioPartidos,horaFinPartidos,enfrentamientosGenerados,estado) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,False,2)",
                    Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, partesLiga[0]);
            preparedStatement.setString(2, partesLiga[1]);
            preparedStatement.setDouble(3, Double.parseDouble(partesLiga[2]));
            preparedStatement.setInt(4, Integer.parseInt(partesLiga[3]));
            preparedStatement.setInt(5, Integer.parseInt(partesLiga[4]));
            preparedStatement.setString(6, partesLiga[5]);
            preparedStatement.setString(7, partesLiga[6]);
            preparedStatement.setString(8, partesLiga[7]);
            preparedStatement.setString(9, partesLiga[8]);
            preparedStatement.setInt(10, idDeporte);
            preparedStatement.setInt(11, idOrganizador);
            preparedStatement.setInt(12, Integer.parseInt(partesLiga[10]));
            preparedStatement.setInt(13, Integer.parseInt(partesLiga[11]));
            preparedStatement.setString(14, partesLiga[12]);
            preparedStatement.setString(15, partesLiga[13]);
            //System.out.println(preparedStatement);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                ResultSet rs = preparedStatement.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm yyyy-MM-dd");
                    LocalDateTime dateTime = LocalDateTime.parse(partesLiga[7] + " " + partesLiga[8], formatter);

                    PIServer.tareasPendientes.agregarTarea(new Tarea(dateTime, TiposDeEnfrentamientos.GENERATE_FIGHT_LEAGUES + ";" + id));
                }
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            return false;
        }
    }

    public String equiposDeUnaLiga(int id) {

        String equipos = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT e.id, e.nombre, u.nombre FROM InscrippcionLiga i JOIN equipo e ON (i.equipo = e.id) JOIN usuario u ON (e.lider = u.id) WHERE i.liga = ?");
            preparedStatement.setInt(1, id);

            ResultSet rset = preparedStatement.executeQuery();

            //nombreEquipo:lider:integrantes1!integrante2;
            while (rset.next()) {
                equipos = equipos + rset.getString(2) + ":" + rset.getString(3) + ":" + integrantesDeUnEquipo(rset.getInt(1)) + ";";

            }

            return equipos;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String equiposDeUnaLigaParaUsuario(int id) {

        String equipos = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT e.id, e.nombre, u.nombre, d.nombreDto FROM InscrippcionLiga i JOIN equipo e ON (i.equipo = e.id) JOIN usuario u ON (e.lider = u.id) JOIN deporte d ON (e.deporte = d.id) WHERE i.liga = ?");
            preparedStatement.setInt(1, id);

            ResultSet rset = preparedStatement.executeQuery();

            //nombreEquipo:lider:deporte;
            while (rset.next()) {
                equipos = equipos + rset.getString(2) + ":" + rset.getString(3) + ":" + rset.getString(4) + ";";

            }

            return equipos;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String rondasTorneo(int idTorneo) {
        String rondas = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            //nºronda;id;eLocal;ptosLocal;eVisitante;ptosVisiante!otroE:otraRonda;;;;!
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT id, ronda, equipoLocal, ptosLocal, equipoVisitante, ptosVisitante, torneo FROM resultadostorneo WHERE torneo=?");
            preparedStatement.setInt(1, idTorneo);

            ResultSet rset = preparedStatement.executeQuery();

            int rondaActual = 1;

            while (rset.next()) {

                if (rondaActual != rset.getInt(2)) {
                    rondas += ":";
                    rondaActual = rset.getInt(2);

                }

                String ptosLocal;
                String ptosVisitante;

                if (rset.getObject(4) == null) {
                    ptosLocal = "?";
                } else {
                    ptosLocal = "" + rset.getInt(4);
                }

                if (rset.getObject(6) == null) {
                    ptosVisitante = "?";

                } else {
                    ptosVisitante = "" + rset.getInt(6);
                }

                //getObjetc para tener los nulos
                rondas += rset.getInt(1) + "/" + rset.getInt(2) + "/" + nombreEquipo(rset.getInt(3))
                        + "/" + ptosLocal + "/" + nombreEquipo(rset.getInt(5)) + "/" + ptosVisitante + "/" + rset.getInt(7) + "!";
            }

            return rondas;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;

    }

    public String equiposDeUnUsuario(int id) {
        String equipos = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            //ME DA TODAS LAS ID DONDE ESTA ESE USUARIO
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT DISTINCT(c.equipo) FROM componenteequipo c WHERE c.usuario=?");
            preparedStatement.setInt(1, id);

            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {

                equipos = equipos + datosDeUnEquipo(rset.getInt(1));
                //System.out.println(equipos);
            }
            //--------------EQUIPO------------------
            //nombreEquipo:deporte:lider:integrante1!integrante2;  EQUIPO;EQUIPO;EQUIPO
            return equipos;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String datosDeUnEquipo(int id) {
        String equipo = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            //ME DA TODAS LAS ID DONDE ESTA ESE USUARIO
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT e.nombre,d.nombreDto,u.nombre,e.ubicacion,p.nombre FROM equipo e JOIN deporte d ON (e.deporte=d.id) JOIN usuario u ON (e.lider=u.id) JOIN privacidad p ON (e.privacidad = p.id) WHERE e.id=?");
            preparedStatement.setInt(1, id);

            ResultSet rset = preparedStatement.executeQuery();
            //nombreEquipo:deporte:lider:integrante1!integrante2;
            while (rset.next()) {

                return equipo = rset.getString(1) + ":" + rset.getString(2) + ":" + rset.getString(3) + ":" + integrantesDeUnEquipo(id) + ":" + rset.getString(4) + ":" + rset.getString(5) + ";";

            }

            return equipo;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public int obtenerIdEquipoSegunNombre(String nombre) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM equipo WHERE nombre LIKE ?");
            preparedStatement.setString(1, nombre);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                return rset.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public boolean registrarUsuarioEnEquipo(int equipo, int id) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO componenteequipo(equipo,usuario) VALUES(?,?)");
            preparedStatement.setInt(1, equipo);
            preparedStatement.setInt(2, id);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean usuarioEnEquipo(int equipo, int usuario) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM componenteequipo e WHERE equipo = ? AND usuario = ?")) {
            stmt.setInt(1, equipo);
            stmt.setInt(2, usuario);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean usuarioSolicitoInscripccion(int equipo, int usuario) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM invitacionusuarioequipo WHERE equipo = ? AND usuario = ? AND estado = 5")) {
            stmt.setInt(1, equipo);
            stmt.setInt(2, usuario);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean invitarUsuarioAEquipo(int equipo, int id) {

        if (usuarioEnEquipo(equipo, id)) {
            return false;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO invitacionEquipoUsuario(equipo,usuario,estado) VALUES(?,?,5)");
            preparedStatement.setInt(1, equipo);
            preparedStatement.setInt(2, id);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            //ex.printStackTrace();
            return false;
        }
    }

    public boolean solicitudUnionEquipo(int equipo, int id) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO invitacionUsuarioEquipo(equipo,usuario,estado) VALUES(?,?,5)");
            preparedStatement.setInt(1, equipo);
            preparedStatement.setInt(2, id);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean nombreExistente(String nombre) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM equipo WHERE nombre = ?")) {
            stmt.setString(1, nombre);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean crearEquipo(String nombreEquipo, int idDeporte, int idUsuario, String ubicacion, int privacidad) {

        if (nombreExistente(nombreEquipo)) {
            return false;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO equipo(nombre,deporte,lider,ubicacion,privacidad) VALUES(?,?,?,?,?)");
            preparedStatement.setString(1, nombreEquipo);
            preparedStatement.setInt(2, idDeporte);
            preparedStatement.setInt(3, idUsuario);
            preparedStatement.setString(4, ubicacion);
            preparedStatement.setInt(5, privacidad);

            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {

                //INSERTA EL LIDER EN EL EQUIPO
                int idE = obtenerIdEquipoSegunNombre(nombreEquipo);

                return registrarUsuarioEnEquipo(idE, idUsuario);
            } else {
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String torneoInformacion(int id) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            String torneoInfo = "";
            int idT = 0;

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT t.id,t.nombre,t.ubicacion,t.coste,t.maxEquipos,t.minEquipos,t.hInicio,t.fInicio,t.hLimite,t.fLimite,d.nombreDto,e.nombre FROM torneo t JOIN deporte d ON t.deporte = d.id JOIN estado e ON t.estado=e.id  WHERE t.id = ?");
            preparedStatement.setInt(1, id);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                idT = rset.getInt(1);
                for (int i = 2; i <= 12; i++) {
                    torneoInfo = torneoInfo + rset.getString(i) + "!";
                }
                torneoInfo = torneoInfo + ctdaEquiposInscritosTorneo(idT) + "!" + idT + ";";
            }

            return torneoInfo;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String torneosDisponibles(String ubicacionUsuario, float radio) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String torneoInfo = "";
            String idTorneos = "";

            CallableStatement cs = conn.prepareCall("{? = call buscarTorneosCercanos(?, ?)}");
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setString(2, ubicacionUsuario);
            if (radio == -1) {
                cs.setFloat(3, 6371); // RADIO DE LA TIERRA
            } else {
                cs.setFloat(3, radio);
            }
            cs.execute();
            idTorneos = cs.getString(1);
            if (idTorneos == null) {
                return torneoInfo;
            }
            String[] ids = idTorneos.split(",");

            for (String id : ids) {
                torneoInfo = torneoInfo + torneoInformacion(Integer.parseInt(id));
            }

            return torneoInfo;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String torneosJugables() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            String torneoInfo = "";
            int idT = 0;

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT t.id,t.nombre,t.ubicacion,t.coste,t.maxEquipos,t.minEquipos,t.hInicio,t.fInicio,t.hLimite,t.fLimite,d.nombreDto,e.nombre FROM torneo t JOIN deporte d ON t.deporte = d.id JOIN estado e ON t.estado=e.id  WHERE t.estado = 2");
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                idT = rset.getInt(1);
                for (int i = 2; i <= 12; i++) {
                    torneoInfo = torneoInfo + rset.getString(i) + "!";
                }
                torneoInfo = torneoInfo + ctdaEquiposInscritosTorneo(idT) + "!" + idT + ";";
            }

            return torneoInfo;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String torneosIncristosUsuario(int idUsuario) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            //nombre:ubicacion:coste:maxEquipos:minEquipos:horaInicio:fechaInicio:horaLimite:fechaLimite:deporte:estado:equiposInscritos:id
            String torneoInfo = "";
            int idT = 0;

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT t.id,t.nombre,t.ubicacion,t.coste,t.maxEquipos,t.minEquipos,t.hInicio,t.fInicio,t.hLimite,t.fLimite,d.nombreDto,e.nombre FROM torneo t JOIN deporte d ON t.deporte = d.id JOIN estado e ON t.estado=e.id JOIN inscrippciontorneo it ON it.torneo = t.id JOIN equipo eq ON eq.id = it.equipo JOIN componenteequipo ce ON (ce.equipo = eq.id) WHERE (t.estado = 2 OR t.estado = 1)AND ce.usuario = ?");
            preparedStatement.setInt(1, idUsuario);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                idT = rset.getInt(1);
                for (int i = 2; i <= 12; i++) {
                    torneoInfo = torneoInfo + rset.getString(i) + "!";
                }
                torneoInfo = torneoInfo + ctdaEquiposInscritosTorneo(idT) + "!" + idT + ";";
            }

            return torneoInfo;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String torneoEstadoUsuario(int idUsuario, int idTorneo) {

        String inscrito = "INSCRITO";
        String inscribirse = "INSCRIBIRSE";
        String cerrado = "CERRADO";

        //COMPROBAR SI ESTOY INSCRITO
        if (usuarioInscritoTorneo(idUsuario, idTorneo)) {
            return inscrito;
        }

        //COMPROBAR SI ESTA LLENO
        int max = cantidadMaximaEquiposTorneo(idTorneo);
        int inscritos = ctdaEquiposInscritosTorneo(idTorneo);

        if (inscritos >= max) {
            return cerrado;
        }

        return inscribirse;
    }

    public boolean usuarioInscritoTorneo(int idUsuario, int idTorneo) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement(
                "SELECT ce.usuario FROM inscrippciontorneo it JOIN equipo e ON (it.equipo = e.id) JOIN componenteequipo ce ON (e.id = ce.equipo) WHERE ce.usuario = ? AND it.torneo = ?")) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idTorneo);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String tusEquiposInscribiblesTorneo(int idUsuario, int idTorneo) {

        int deporte = deporteTorneo(idTorneo);

        String equipos = "";

        //SELECT e.id, e.nombre, d.ctdadXequipo FROM equipo e, componenteequipo ce, deporte d 
        //WHERE e.id = ce.equipo AND e.deporte = d.id AND e.lider = 17 AND d.id = 5
        //GROUP BY e.id, e.nombre 
        //HAVING COUNT(*) >= d.ctdadXequipo
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement stmt = conn.prepareStatement("SELECT e.id, e.nombre, d.ctdadXequipo FROM equipo e, componenteequipo ce, deporte d "
                    + "WHERE e.id = ce.equipo AND e.deporte = d.id AND e.lider = ? AND d.id = ? "
                    + "GROUP BY e.id, e.nombre "
                    + "HAVING COUNT(*) >= d.ctdadXequipo");
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, deporte);

            ResultSet rset = stmt.executeQuery();
            if (rset.next()) {
                equipos = equipos + rset.getString(2) + ":";
            }

            return equipos;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;

    }

    public int deporteTorneo(int idTorneo) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement stmt = conn.prepareStatement("SELECT deporte FROM torneo WHERE id = ?");
            stmt.setInt(1, idTorneo);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public boolean insertarEquipoEnTorneo(int idTorneo, String nombreEquipo) {

        int idEquipo = equipoId(nombreEquipo);

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO InscrippcionTorneo(torneo,equipo) VALUES(?,?)");
            preparedStatement.setInt(1, idTorneo);
            preparedStatement.setInt(2, idEquipo);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            return false;
        }
    }

    public int equipoId(String nombreEquipo) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement stmt = conn.prepareStatement("SELECT id FROM equipo WHERE nombre = ?");
            stmt.setString(1, nombreEquipo);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public String nombreEquipo(int id) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            int cantidad = 0;

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT nombre FROM equipo WHERE id = ?");
            preparedStatement.setInt(1, id);

            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                return rset.getString(1);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return "?";

    }

    //GENERAR ENFRENTAMIENTOS
    public boolean comprobarEstadoTorneo(int idTorneo, int estado) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement("SELECT estado FROM torneo WHERE id = ?")) {
            stmt.setInt(1, idTorneo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return (rs.getInt(1) == estado);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;

    }

    public int cantidadMinimaEquiposTorneo(int idTorneo) {
        int cantidadMinima = 0;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement(
                "SELECT minEquipos FROM torneo WHERE id = ?")) {
            stmt.setInt(1, idTorneo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                cantidadMinima = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cantidadMinima;
    }

    public int cantidadMaximaEquiposTorneo(int idTorneo) {
        int cantidadMinima = 0;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement(
                "SELECT maxEquipos FROM torneo WHERE id = ?")) {
            stmt.setInt(1, idTorneo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                cantidadMinima = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cantidadMinima;
    }

    public void cambiarEstadoTorneo(int idTorneo, int nuevoEstado) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement pstmt = conn.prepareStatement("UPDATE torneo SET estado = ? WHERE id = ?");

            pstmt.setInt(1, nuevoEstado);
            pstmt.setInt(2, idTorneo);
            pstmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println("Error al cambiar el estado del torneo: " + ex.getMessage());
        }
    }

    public void cambiarEstadoPartido(int idPartido, int nuevoEstado) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement pstmt = conn.prepareStatement("UPDATE partido SET estado = ? WHERE id = ?");

            pstmt.setInt(1, nuevoEstado);
            pstmt.setInt(2, idPartido);
            pstmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println("Error al cambiar el estado del partido: " + ex.getMessage());
        }
    }

    public Stack<Integer> stackEquiposInscritosTorneo(int idTorneo) {
        Stack<Integer> equipos = new Stack<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement("SELECT equipo FROM InscrippcionTorneo WHERE torneo = ?")) {
            stmt.setInt(1, idTorneo);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                equipos.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipos;
    }

    public boolean insertarRonda(int idTorneo, int ronda, int equipo1, int equipo2) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO ResultadosTorneo(torneo,ronda,equipoLocal,equipoVisitante) VALUES(?,?,?,?)");
            preparedStatement.setInt(1, idTorneo);
            preparedStatement.setInt(2, ronda);
            if (equipo1 != -1) {
                preparedStatement.setInt(3, equipo1);
            } else {
                preparedStatement.setNull(3, Types.NULL);
            }
            if (equipo2 != -1) {
                preparedStatement.setInt(4, equipo2);
            } else {
                preparedStatement.setNull(4, Types.NULL);
            }

            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            return false;
        }
    }

    public PriorityQueue<Tarea> torneosPendientesGenerar() {

        PriorityQueue<Tarea> torneosPendientes = new PriorityQueue<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT t.id, t.hLimite, t.fLimite FROM torneo t WHERE estado = 2");
            ResultSet rset = preparedStatement.executeQuery();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm yyyy-MM-dd");
            LocalDateTime dateTime;

            while (rset.next()) {

                dateTime = LocalDateTime.parse(rset.getString(2) + " " + rset.getString(3), formatter);

                torneosPendientes.add(new Tarea(dateTime, TiposDeEnfrentamientos.GENERATE_FIGHT_TOURNAMENTS + ";" + rset.getInt(1)));

            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return torneosPendientes;

    }

    public PriorityQueue<Tarea> ligasPendientesGenerar() {

        PriorityQueue<Tarea> ligasPendientesGenerar = new PriorityQueue<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT t.id, t.hLimite, t.fLimite FROM liga t WHERE estado = 2");
            ResultSet rset = preparedStatement.executeQuery();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm yyyy-MM-dd");
            LocalDateTime dateTime;

            while (rset.next()) {

                dateTime = LocalDateTime.parse(rset.getString(2) + " " + rset.getString(3), formatter);

                ligasPendientesGenerar.add(new Tarea(dateTime, TiposDeEnfrentamientos.GENERATE_FIGHT_LEAGUES + ";" + rset.getInt(1)));

            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ligasPendientesGenerar;

    }

    public PriorityQueue<Tarea> partidosPorJugar() {

        PriorityQueue<Tarea> ligasPendientesGenerar = new PriorityQueue<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT t.id, t.hLimite, t.fLimite FROM partido t WHERE estado = 2");
            ResultSet rset = preparedStatement.executeQuery();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm yyyy-MM-dd");
            LocalDateTime dateTime;

            while (rset.next()) {

                dateTime = LocalDateTime.parse(rset.getString(2) + " " + rset.getString(3), formatter);

                ligasPendientesGenerar.add(new Tarea(dateTime, TiposDeEnfrentamientos.CHECK_MATCH + ";" + rset.getInt(1)));

            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ligasPendientesGenerar;

    }

    public boolean guardarRonda(int id, String idL, int pL, String idV, int pV) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement pstmt = conn.prepareStatement("UPDATE ResultadosTorneo SET equipoLocal = ?, ptosLocal = ?, equipoVisitante = ?, ptosVisitante = ? WHERE id = ?");

            if (idL.equalsIgnoreCase("?")) {
                pstmt.setNull(1, Types.INTEGER);
            } else {
                pstmt.setInt(1, obtenerIdEquipoSegunNombre(idL));
            }
            pstmt.setInt(2, pL);
            if (idV.equalsIgnoreCase("?")) {
                pstmt.setNull(3, Types.INTEGER);
            } else {
                pstmt.setInt(3, obtenerIdEquipoSegunNombre(idV));
            }
            pstmt.setInt(4, pV);
            pstmt.setInt(5, id);
            pstmt.executeUpdate();

            int insertado = pstmt.executeUpdate();
            if (insertado > 0) {

                return true;

            } else {
                return false;
            }

        } catch (SQLException ex) {
            System.out.println("Error al cambiar el estado del torneo: " + ex.getMessage());
        }
        return false;
    }

    public int insertarPartido(String ubicacion, String fInicio, String hInicio, String fLimite, String hLimite, String coste, String deporte, int idUsuario) {

        //ubicacion;fInicio;hInicio;fLimite;hLimite;coste;deporte;creador
        int idDeporte = obtenerIdDeporte(deporte);

        if (idDeporte == 0) {
            return -1;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO partido(ubicacion,fInicio,hInicio,fLimite,hLimite,coste,deporte,creador,estado) VALUES(?,?,?,?,?,?,?,?,2)",
                    Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, ubicacion);
            preparedStatement.setString(2, fInicio);
            preparedStatement.setString(3, hInicio);
            preparedStatement.setString(4, fLimite);
            preparedStatement.setString(5, hLimite);
            preparedStatement.setDouble(6, Double.parseDouble(coste));
            preparedStatement.setInt(7, idDeporte);
            preparedStatement.setInt(8, idUsuario);

            //System.out.println(preparedStatement);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {

                ResultSet rs = preparedStatement.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    if (inscribirUsuarioPartido(idUsuario, id)) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm yyyy/MM/dd");
                        LocalDateTime dateTime = LocalDateTime.parse(hLimite + " " + fLimite, formatter);

                        PIServer.tareasPendientes.agregarTarea(new Tarea(dateTime, TiposDeEnfrentamientos.CHECK_MATCH + ";" + id));

                        return id;
                    } else {
                        return -1;
                    }
                }

                return -1;
            } else {
                return -1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public boolean inscribirUsuarioPartido(int idUsuario, int idPartido) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO participar(usuario,partido) VALUES(?,?)");
            preparedStatement.setInt(1, idUsuario);
            preparedStatement.setInt(2, idPartido);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            return false;
        }
    }

    public int ctdadNecesariaParaPartido(int idPartido) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT d.ctdadJugadores FROM partido p JOIN deporte d ON p.deporte = d.id WHERE p.id = ?");
            preparedStatement.setInt(1, idPartido);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                return rset.getInt(1);
            }

            return 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 1;
    }

    public String partidosIncristosUsuario(int idUsuario) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            //ubicacion!fInicio!hInicio!fLimite!hLimiite!coste!deporte!estado!maxInscritos!inscritos!id
            String partidos = "";
            int idP = 0;

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT p.id, p.ubicacion, p.fInicio, p.hInicio, p.fLimite, p.hLimite, p.coste, d.nombreDto, est.nombre, d.ctdadJugadores FROM partido p JOIN deporte d ON p.deporte = d.id JOIN participar par ON par.partido = p.id JOIN estado est ON est.id = p.estado WHERE (p.estado = 2 OR p.estado = 1)AND par.usuario = ?");
            preparedStatement.setInt(1, idUsuario);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                idP = rset.getInt(1);
                for (int i = 2; i <= 10; i++) {
                    partidos = partidos + rset.getString(i) + "!";
                }
                partidos = partidos + ctdaUsuariosInscritosPartidos(idP) + "!" + idP + ";";
            }

            return partidos;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public int ctdaUsuariosInscritosPartidos(int idPartido) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) FROM participar WHERE partido = ?");
            preparedStatement.setInt(1, idPartido);
            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                return rset.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;

    }

    public String partidoEstadoUsuario(int idUsuario, int idPartido) {

        String inscrito = "INSCRITO";
        String inscribirse = "INSCRIBIRSE";
        String cerrado = "CERRADO";

        //COMPROBAR SI ESTOY INSCRITO
        if (usuarioInscritoPartido(idUsuario, idPartido)) {
            return inscrito;
        }

        //COMPROBAR SI ESTA LLENO
        int max = cantidadMaxJugadoresPartido(idPartido);
        int inscritos = ctdaUsuariosInscritosPartidos(idPartido);

        if (inscritos >= max) {
            return cerrado;
        }

        return inscribirse;
    }

    public boolean usuarioInscritoPartido(int idUsuario, int idPartido) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement(
                "SELECT p.usuario FROM participar p WHERE p.usuario = ? AND p.partido = ?")) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idPartido);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public int cantidadMaxJugadoresPartido(int idPartido) {
        int cantidadMax = 0;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement(
                "SELECT d.ctdadJugadores FROM partido p JOIN deporte d ON (p.deporte = d.id) WHERE p.id = ?")) {
            stmt.setInt(1, idPartido);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                cantidadMax = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cantidadMax;
    }

    public String partidosDisponibles(String ubicacionUsuario, float radio) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String partidoInfo = "";
            String idPartidos = "";

            CallableStatement cs = conn.prepareCall("{? = call buscarPartidosCercanos(?, ?)}");
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setString(2, ubicacionUsuario);
            if (radio == -1) {
                cs.setFloat(3, 6371); // RADIO DE LA TIERRA
            } else {
                cs.setFloat(3, radio);
            }
            cs.execute();
            idPartidos = cs.getString(1);
            if (idPartidos == null) {
                return partidoInfo;
            }
            String[] ids = idPartidos.split(",");

            for (String id : ids) {
                partidoInfo = partidoInfo + partidoInformacion(Integer.parseInt(id));
            }

            return partidoInfo;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String partidoInformacion(int idPartido) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            //ubicacion!fInicio!hInicio!fLimite!hLimiite!coste!deporte!estado!maxInscritos!inscritos!id
            String partidos = "";
            int idP = 0;

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT p.id, p.ubicacion, p.fInicio, p.hInicio, p.fLimite, p.hLimite, p.coste, d.nombreDto, est.nombre, d.ctdadJugadores FROM partido p JOIN deporte d ON p.deporte = d.id JOIN estado est ON est.id = p.estado WHERE (p.estado = 2 OR p.estado = 1)AND p.id= ?");
            preparedStatement.setInt(1, idPartido);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                idP = rset.getInt(1);
                for (int i = 2; i <= 10; i++) {
                    partidos = partidos + rset.getString(i) + "!";
                }
                partidos = partidos + ctdaUsuariosInscritosPartidos(idP) + "!" + idP + ";";
            }

            return partidos;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public boolean comprobarEstadoLiga(int idLiga, int estado) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement("SELECT estado FROM liga WHERE id = ?")) {
            stmt.setInt(1, idLiga);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return (rs.getInt(1) == estado);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;

    }

    public String fInicioLiga(int idLiga) {
        String fecha = "";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement(
                "SELECT fInicio FROM liga WHERE id = ?")) {
            stmt.setInt(1, idLiga);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                fecha = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fecha;
    }

    public int duracionPartidoLiga(int idLiga) {
        int duracion = 0;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement(
                "SELECT duracionPartido FROM liga WHERE id = ?")) {
            stmt.setInt(1, idLiga);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                duracion = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return duracion;
    }

    public String horaInicioPartidosLiga(int idLiga) {
        String fecha = "";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement(
                "SELECT horaInicioPartidos FROM liga WHERE id = ?")) {
            stmt.setInt(1, idLiga);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                fecha = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fecha;
    }

    public String horaFinPartidosLiga(int idLiga) {
        String fecha = "";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement(
                "SELECT horaFinPartidos FROM liga WHERE id = ?")) {
            stmt.setInt(1, idLiga);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                fecha = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fecha;
    }

    public int frecuenciaJornadaLiga(int idLiga) {
        int duracion = 0;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement(
                "SELECT frecuenciaJornada FROM liga WHERE id = ?")) {
            stmt.setInt(1, idLiga);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                duracion = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return duracion;
    }

    public int cantidadMinimaEquiposLiga(int idLiga) {
        int cantidadMinima = 0;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement(
                "SELECT minEquipos FROM liga WHERE id = ?")) {
            stmt.setInt(1, idLiga);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                cantidadMinima = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cantidadMinima;
    }

    public int cantidadMaximaEquiposLiga(int idLiga) {
        int cantidadMinima = 0;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement(
                "SELECT maxEquipos FROM liga WHERE id = ?")) {
            stmt.setInt(1, idLiga);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                cantidadMinima = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cantidadMinima;
    }

    public void cambiarEstadoLiga(int idLiga, int nuevoEstado) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement pstmt = conn.prepareStatement("UPDATE liga SET estado = ? WHERE id = ?");

            pstmt.setInt(1, nuevoEstado);
            pstmt.setInt(2, idLiga);
            pstmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println("Error al cambiar el estado del torneo: " + ex.getMessage());
        }
    }

    public List<String> listEquiposInscritosLiga(int idLiga) {
        List<String> equipos = new LinkedList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement("SELECT equipo FROM InscrippcionLiga WHERE liga = ?")) {
            stmt.setInt(1, idLiga);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                equipos.add(rs.getInt(1) + "");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipos;
    }

    public boolean insertarJornada(int idLiga, int jornada, int equipo1, int equipo2, String fecha, String hora) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO ResultadosLiga(liga,ronda,equipoLocal,equipoVisitante,fJugada,hJugada) VALUES(?,?,?,?,?,?)");
            preparedStatement.setInt(1, idLiga);
            preparedStatement.setInt(2, jornada);
            if (equipo1 != -1) {
                preparedStatement.setInt(3, equipo1);
            } else {
                preparedStatement.setNull(3, Types.NULL);
            }
            if (equipo2 != -1) {
                preparedStatement.setInt(4, equipo2);
            } else {
                preparedStatement.setNull(4, Types.NULL);
            }

            preparedStatement.setString(5, fecha);
            preparedStatement.setString(6, hora);

            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            return false;
        }
    }

    public String cantidadDeJornadasLiga(int idLiga) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            String cantidad = "0";

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT MAX(ronda) FROM resultadosliga WHERE liga = ?");
            preparedStatement.setInt(1, idLiga);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                cantidad = rset.getString(1);
            }

            return cantidad;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String jornadaLiga(int idLiga, int jornada) {
        String rondas = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            //id;eLocal;ptosLocal;eVisitante;ptosVisiante;fecha;hora!
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT id, equipoLocal, ptosLocal, equipoVisitante, ptosVisitante, fJugada, hJugada FROM resultadosliga WHERE liga = ? AND ronda = ?");
            preparedStatement.setInt(1, idLiga);
            preparedStatement.setInt(2, jornada);

            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {

                String ptosLocal;
                String ptosVisitante;

                if (rset.getObject(3) == null) {
                    ptosLocal = "?";
                } else {
                    ptosLocal = "" + rset.getInt(3);
                }

                if (rset.getObject(5) == null) {
                    ptosVisitante = "?";

                } else {
                    ptosVisitante = "" + rset.getInt(5);
                }

                //getObjetc para tener los nulos
                rondas += rset.getInt(1) + "/" + nombreEquipo(rset.getInt(2))
                        + "/" + ptosLocal + "/" + nombreEquipo(rset.getInt(4)) + "/"
                        + ptosVisitante + "/" + rset.getString(6) + "/" + rset.getString(7) + "!";
            }

            return rondas;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;

    }

    public boolean guardarJornada(int id, int idL, int pL, int idV, int pV) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement pstmt = conn.prepareStatement("UPDATE ResultadosLiga SET equipoLocal = ?, ptosLocal = ?, equipoVisitante = ?, ptosVisitante = ? WHERE id = ?");

            pstmt.setInt(1, idL);
            pstmt.setInt(2, pL);
            pstmt.setInt(3, idV);
            pstmt.setInt(4, pV);
            pstmt.setInt(5, id);
            pstmt.executeUpdate();

            int insertado = pstmt.executeUpdate();
            if (insertado > 0) {

                return true;

            } else {
                return false;
            }

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return false;
    }

    public int partidosJugadosEquipoLiga(int liga, int equipo) {
        //SELECT COUNT(*) FROM resultadosliga WHERE liga = 2 AND ((equipoLocal = 5 AND ptosLocal IS NOT NULL) OR (equipoVisitante = 5 AND ptosVisitante IS NOT NULL))
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            int cantidad = 0;

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) FROM resultadosliga WHERE liga = ? AND ((equipoLocal = ? AND ptosLocal IS NOT NULL) OR (equipoVisitante = ? AND ptosVisitante IS NOT NULL))");
            preparedStatement.setInt(1, liga);
            preparedStatement.setInt(2, equipo);
            preparedStatement.setInt(3, equipo);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {

                if (rset.getObject(1) == null) {
                    cantidad = 0;
                } else {
                    cantidad = rset.getInt(1);
                }
            }

            return cantidad;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public int partidosGanadosEquipoLiga(int liga, int equipo) {
        //SELECT COUNT(*) FROM resultadosliga WHERE liga = ? AND ((equipoLocal = ? AND ptosLocal IS NOT NULL AND ptosLocal > ptosVisitante) OR (equipoVisitante = ? AND ptosVisitante IS NOT NULL AND ptosVisitante > ptosLocal))

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            int cantidad = 0;

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) FROM resultadosliga WHERE liga = ? AND ((equipoLocal = ? AND ptosLocal IS NOT NULL AND ptosLocal > ptosVisitante) OR (equipoVisitante = ? AND ptosVisitante IS NOT NULL AND ptosVisitante > ptosLocal))");
            preparedStatement.setInt(1, liga);
            preparedStatement.setInt(2, equipo);
            preparedStatement.setInt(3, equipo);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                if (rset.getObject(1) == null) {
                    cantidad = 0;
                } else {
                    cantidad = rset.getInt(1);
                }
            }

            return cantidad;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public int partidosEmpatadoEquipoLiga(int liga, int equipo) {
        //SELECT COUNT(*) FROM resultadosliga WHERE liga = ? AND ((equipoLocal = ? AND ptosLocal IS NOT NULL AND ptosLocal > ptosVisitante) OR (equipoVisitante = ? AND ptosVisitante IS NOT NULL AND ptosVisitante > ptosLocal))

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            int cantidad = 0;

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) FROM resultadosliga WHERE liga = ? AND ((equipoLocal = ? AND ptosLocal IS NOT NULL AND ptosLocal = ptosVisitante) OR (equipoVisitante = ? AND ptosVisitante IS NOT NULL AND ptosVisitante = ptosLocal))");
            preparedStatement.setInt(1, liga);
            preparedStatement.setInt(2, equipo);
            preparedStatement.setInt(3, equipo);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                if (rset.getObject(1) == null) {
                    cantidad = 0;
                } else {
                    cantidad = rset.getInt(1);
                }
            }

            return cantidad;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    //  SELECT SUM(total) AS suma_total FROM (
    //  SELECT SUM(ptosVisitante) AS total FROM resultadosliga WHERE liga = 2 AND equipoVisitante = 6
    //  UNION ALL
    //  SELECT SUM(ptosLocal) AS total FROM resultadosliga WHERE liga = 2 AND equipoLocal = 6
    //) AS sumaTotal;
    public int puntosGanadosLiga(int liga, int equipo) {
        //  SELECT SUM(total) AS suma_total FROM (SELECT SUM(ptosVisitante) AS total FROM resultadosliga WHERE liga = ? AND equipoVisitante = ? UNION ALL SELECT SUM(ptosLocal) AS total FROM resultadosliga WHERE liga = ? AND equipoLocal = ?) AS sumaTotal;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            int cantidad = 0;

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT SUM(total) AS suma_total FROM (SELECT SUM(ptosVisitante) AS total FROM resultadosliga WHERE liga = ? AND equipoVisitante = ? UNION ALL SELECT SUM(ptosLocal) AS total FROM resultadosliga WHERE liga = ? AND equipoLocal = ?) AS sumaTotal;");
            preparedStatement.setInt(1, liga);
            preparedStatement.setInt(2, equipo);
            preparedStatement.setInt(3, liga);
            preparedStatement.setInt(4, equipo);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                if (rset.getObject(1) == null) {
                    cantidad = 0;
                } else {
                    cantidad = rset.getInt(1);
                }
            }

            return cantidad;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public int puntosEnContraLiga(int liga, int equipo) {
        //  SELECT SUM(total) AS suma_total FROM (SELECT SUM(ptosLocal) AS total FROM resultadosliga WHERE liga = ? AND equipoVisitante = ? UNION ALL SELECT SUM(ptosVisitante) AS total FROM resultadosliga WHERE liga = ? AND equipoLocal = ?) AS sumaTotal;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            int cantidad = 0;

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT SUM(total) AS suma_total FROM (SELECT SUM(ptosLocal) AS total FROM resultadosliga WHERE liga = ? AND equipoVisitante = ? UNION ALL SELECT SUM(ptosVisitante) AS total FROM resultadosliga WHERE liga = ? AND equipoLocal = ?) AS sumaTotal;");
            preparedStatement.setInt(1, liga);
            preparedStatement.setInt(2, equipo);
            preparedStatement.setInt(3, liga);
            preparedStatement.setInt(4, equipo);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                if (rset.getObject(1) == null) {
                    cantidad = 0;
                } else {
                    cantidad = rset.getInt(1);
                }
            }

            return cantidad;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public String clasificacionDeLiga(int id) {

        String clasificacion = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT e.id, e.nombre FROM InscrippcionLiga i JOIN equipo e ON (i.equipo = e.id) JOIN usuario u ON (e.lider = u.id) WHERE i.liga = ?");
            preparedStatement.setInt(1, id);

            ResultSet rset = preparedStatement.executeQuery();

            //nombreEquipo:partidosJugados:puntos:puntosAFavor:puntosEnContra:Diferencia;
            while (rset.next()) {
                int idEquipo = rset.getInt(1);

                int partidosJugados = partidosJugadosEquipoLiga(id, idEquipo);
                int puntos = (partidosGanadosEquipoLiga(id, idEquipo) * 3) + partidosEmpatadoEquipoLiga(id, idEquipo);
                int puntosGanados = puntosGanadosLiga(id, idEquipo);
                int puntosPerdidos = puntosEnContraLiga(id, idEquipo);
                clasificacion += rset.getString(2) + ":" + partidosJugados + ":" + puntos + ":" + puntosGanados
                        + ":" + puntosPerdidos + ":" + (puntosGanados - puntosPerdidos) + ";";

            }

            return clasificacion;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public boolean cancelarLiga(int idLiga) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("UPDATE liga SET estado = 3 WHERE id = ?");
            preparedStatement.setInt(1, idLiga);

            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {

                PIServer.tareasPendientes.eliminarTarea(TiposDeEnfrentamientos.GENERATE_FIGHT_LEAGUES + ";" + idLiga);
                return true;

            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean finalizarLiga(int idLiga) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("UPDATE liga SET estado = 4 WHERE id = ?");
            preparedStatement.setInt(1, idLiga);

            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {

                return true;

            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String ligasIncristosUsuario(int idUsuario) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            //nombre:ubicacion:coste:maxEquipos:minEquipos:horaInicio:fechaInicio:horaLimite:fechaLimite:deporte:estado:equiposInscritos:id
            String ligaInfo = "";
            int idL = 0;

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT t.id,t.nombre,t.ubicacion,t.coste,t.maxEquipos,t.minEquipos,t.hInicio,t.fInicio,t.hLimite,t.fLimite,d.nombreDto,t.frecuenciaJornada,t.duracionPartido,t.horaInicioPartidos,t.horaFinPartidos,e.nombre FROM liga t JOIN deporte d ON t.deporte = d.id JOIN estado e ON t.estado=e.id JOIN inscrippcionliga it ON it.liga = t.id JOIN equipo eq ON eq.id = it.equipo JOIN componenteequipo ce ON (ce.equipo = eq.id) WHERE (t.estado = 2 OR t.estado = 1)AND ce.usuario = ?");
            preparedStatement.setInt(1, idUsuario);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                idL = rset.getInt(1);
                for (int i = 1; i <= 16; i++) {
                    ligaInfo = ligaInfo + rset.getString(i) + "!";
                }
                ligaInfo = ligaInfo + ctdaEquiposInscritosLiga(idL) + ";";
            }

            return ligaInfo;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String ligaEstadoUsuario(int idUsuario, int idLiga) {

        String inscrito = "INSCRITO";
        String inscribirse = "INSCRIBIRSE";
        String cerrado = "CERRADO";

        //COMPROBAR SI ESTOY INSCRITO
        if (usuarioInscritoLiga(idUsuario, idLiga)) {
            return inscrito;
        }

        //COMPROBAR SI ESTA LLENO
        int max = cantidadMaximaEquiposLiga(idLiga);
        int inscritos = ctdaEquiposInscritosLiga(idLiga);

        if (inscritos >= max) {
            return cerrado;
        }

        return inscribirse;
    }

    public boolean usuarioInscritoLiga(int idUsuario, int idLiga) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS); PreparedStatement stmt = conn.prepareStatement(
                "SELECT ce.usuario FROM inscrippcionliga it JOIN equipo e ON (it.equipo = e.id) JOIN componenteequipo ce ON (e.id = ce.equipo) WHERE ce.usuario = ? AND it.liga = ?")) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idLiga);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean insertarEquipoEnLiga(int idLiga, String nombreEquipo) {

        int idEquipo = equipoId(nombreEquipo);

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO InscrippcionLiga(liga,equipo) VALUES(?,?)");
            preparedStatement.setInt(1, idLiga);
            preparedStatement.setInt(2, idEquipo);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            return false;
        }
    }

    public int deporteLiga(int idLiga) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement stmt = conn.prepareStatement("SELECT deporte FROM liga WHERE id = ?");
            stmt.setInt(1, idLiga);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public String tusEquiposInscribiblesLiga(int idUsuario, int idLiga) {

        int deporte = deporteLiga(idLiga);

        String equipos = "";

        //SELECT e.id, e.nombre, d.ctdadXequipo FROM equipo e, componenteequipo ce, deporte d 
        //WHERE e.id = ce.equipo AND e.deporte = d.id AND e.lider = 17 AND d.id = 5
        //GROUP BY e.id, e.nombre 
        //HAVING COUNT(*) >= d.ctdadXequipo
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement stmt = conn.prepareStatement("SELECT e.id, e.nombre, d.ctdadXequipo FROM equipo e, componenteequipo ce, deporte d "
                    + "WHERE e.id = ce.equipo AND e.deporte = d.id AND e.lider = ? AND d.id = ? "
                    + "GROUP BY e.id, e.nombre "
                    + "HAVING COUNT(*) >= d.ctdadXequipo");
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, deporte);

            ResultSet rset = stmt.executeQuery();
            while (rset.next()) {
                equipos = equipos + rset.getString(2) + ":";
            }

            return equipos;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;

    }

    public String ligaInformacion(int id) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            String ligaInfo = "";
            int idL = 0;

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT t.id,t.nombre,t.ubicacion,t.coste,t.maxEquipos,t.minEquipos,t.hInicio,t.fInicio,t.hLimite,t.fLimite,d.nombreDto,t.frecuenciaJornada,t.duracionPartido,t.horaInicioPartidos,t.horaFinPartidos,e.nombre FROM liga t JOIN deporte d ON t.deporte = d.id JOIN estado e ON t.estado=e.id  WHERE t.id = ?");
            preparedStatement.setInt(1, id);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                idL = rset.getInt(1);
                for (int i = 1; i <= 16; i++) {
                    ligaInfo = ligaInfo + rset.getString(i) + "!";
                }
                ligaInfo = ligaInfo + ctdaEquiposInscritosLiga(idL) + ";";
            }

            return ligaInfo;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String ligasDisponibles(String ubicacionUsuario, float radio) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String ligaInfo = "";
            String idLigas = "";

            CallableStatement cs = conn.prepareCall("{? = call buscarLigasCercanos(?, ?)}");
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setString(2, ubicacionUsuario);
            if (radio == -1) {
                cs.setFloat(3, 6371); // RADIO DE LA TIERRA
            } else {
                cs.setFloat(3, radio);
            }
            cs.execute();
            idLigas = cs.getString(1);
            if (idLigas == null) {
                return ligaInfo;
            }
            String[] ids = idLigas.split(",");

            for (String id : ids) {
                ligaInfo = ligaInfo + ligaInformacion(Integer.parseInt(id));
            }

            return ligaInfo;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    //CAMBIA EL ESTADO
    public boolean generadoEnfrentamientoTorneo(int id) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("UPDATE torneo SET enfrentamientosGenerados = 1 WHERE id = ?");
            preparedStatement.setInt(1, id);

            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    //COMPRUEBA EL ESTADO
    public boolean enfrentamientoGeneradoTorneo(int id) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT enfrentamientosGenerados FROM torneo WHERE id = ?");
            preparedStatement.setInt(1, id);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {

                if (rset.getInt(1) == 0) {
                    return true;
                } else {
                    return false;
                }

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    //CAMBIA EL ESTADO
    public boolean generadoEnfrentamientoLiga(int id) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("UPDATE liga SET enfrentamientosGenerados = 1 WHERE id = ?");
            preparedStatement.setInt(1, id);

            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    //COMPRUEBA EL ESTADO
    public boolean enfrentamientoGeneradoLiga(int id) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT enfrentamientosGenerados FROM liga WHERE id = ?");
            preparedStatement.setInt(1, id);
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {
                if (rset.getInt(1) == 0) {
                    return true;
                } else {
                    return false;
                }

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public String privacidadDisponibles() {

        String privacidad = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT nombre FROM privacidad ORDER BY id ASC");

            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                privacidad = privacidad + rset.getString(1) + ";";
            }

            return privacidad;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;

    }

    public String invitacionesAEquipo(int idUsuario) {
        String invitaciones = "";

        //id:nombre;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT i.id, e.nombre FROM invitacionequipousuario i JOIN equipo e ON (e.id= i.equipo) WHERE usuario = ? AND estado = 5");
            preparedStatement.setInt(1, idUsuario);

            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                invitaciones += rset.getInt(1) + ":" + rset.getString(2) + ";";
            }

            return invitaciones;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    //CAMBIA EL ESTADO
    public boolean cambiarEstadoInvitacionUsuario(int idInvit, int estado) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("UPDATE invitacionEquipoUsuario SET estado = ? WHERE id = ?");
            preparedStatement.setInt(2, idInvit);
            preparedStatement.setInt(1, estado);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public int idRemitenteInvitacionUsuario(int idInvit) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT equipo FROM invitacionEquipoUsuario WHERE id = ?");
            preparedStatement.setInt(1, idInvit);

            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                return rset.getInt(1);
            }

            return 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public boolean respuestaInvitacionEquipo(int idInvitacion, String respuesta, int idUsuario) {

        if (respuesta.equalsIgnoreCase("NO")) {
            cambiarEstadoInvitacionUsuario(idInvitacion, 7);
            return true;
        } else if (respuesta.equalsIgnoreCase("SI")) {

            if (registrarUsuarioEnEquipo(idRemitenteInvitacionUsuario(idInvitacion), idUsuario)) {
                cambiarEstadoInvitacionUsuario(idInvitacion, 6);
                return true;
            } else {
                return false;

            }
        } else {
            return false;
        }
    }

    public String equiposDisponibles(String ubicacionUsuario, float radio) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String equiposInfo = "";
            String idEquipos = "";

            CallableStatement cs = conn.prepareCall("{? = call buscarEquiposCercanos(?, ?)}");
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setString(2, ubicacionUsuario);
            if (radio == -1) {
                cs.setFloat(3, 6371); // RADIO DE LA TIERRA
            } else {
                cs.setFloat(3, radio);
            }
            cs.execute();
            idEquipos = cs.getString(1);
            if (idEquipos == null) {
                return equiposInfo;
            }
            String[] ids = idEquipos.split(",");

            for (String id : ids) {
                equiposInfo = equiposInfo + datosDeUnEquipo(Integer.parseInt(id));
            }

            return equiposInfo;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String invitacionesAlEquipo(int idEquipo) {
        String invitaciones = "";

        //id:nombre;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT i.id, e.nombre FROM invitacionUsuarioEquipo i JOIN usuario e ON (e.id= i.usuario) WHERE equipo = ? AND estado = 5");
            preparedStatement.setInt(1, idEquipo);

            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                invitaciones = rset.getInt(1) + ":" + rset.getString(2) + ";";
            }

            return invitaciones;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public boolean respuestaInvitacionUsuario(int idInvitacion, String respuesta) {

        if (respuesta.equalsIgnoreCase("NO")) {
            cambiarEstadoInvitacionEquipo(idInvitacion, 7);
            return true;
        } else if (respuesta.equalsIgnoreCase("SI")) {

            int usuarioSolicitante = idUsuarioInvitacionEquipo(idInvitacion);

            if (registrarUsuarioEnEquipo(idEquipoInvitacionEquipo(idInvitacion), usuarioSolicitante)) {
                cambiarEstadoInvitacionEquipo(idInvitacion, 6);
                return true;
            } else {
                return false;

            }
        } else {
            return false;
        }
    }

    //CAMBIA EL ESTADO
    public boolean cambiarEstadoInvitacionEquipo(int idInvit, int estado) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("UPDATE invitacionUsuarioEquipo SET estado = ? WHERE id = ?");
            preparedStatement.setInt(2, idInvit);
            preparedStatement.setInt(1, estado);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public int idEquipoInvitacionEquipo(int idInvit) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT equipo FROM invitacionUsuarioEquipo WHERE id = ?");
            preparedStatement.setInt(1, idInvit);

            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                return rset.getInt(1);
            }

            return 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public int idUsuarioInvitacionEquipo(int idInvit) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT usuario FROM invitacionUsuarioEquipo WHERE id = ?");
            preparedStatement.setInt(1, idInvit);

            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                return rset.getInt(1);
            }

            return 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    //CAMBIA EL ESTADO
    public boolean abandonarEquipo(int idEquip, int idUsu) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM componenteequipo WHERE equipo = ? AND usuario = ?");
            preparedStatement.setInt(1, idEquip);
            preparedStatement.setInt(2, idUsu);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean borrarIntegrantesEquipo(int idEquip) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM componenteequipo WHERE equipo = ?");
            preparedStatement.setInt(1, idEquip);
            int insertado = preparedStatement.executeUpdate();
            if (insertado >= 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean quitarLider(int idEquipo) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("UPDATE equipo SET lider = null WHERE id = ?");
            preparedStatement.setInt(1, idEquipo);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean borrarEquipo(int idEquip) {

        if (!borrarIntegrantesEquipo(idEquip)) {
            return false;
        }

        /*if (!quitarLider(idEquip)) {
            return false;
        }*/
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("UPDATE equipo SET nombre = 'ELIMINADO' WHERE id = ?");
            preparedStatement.setInt(1, idEquip);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean modificarEquipo(int idEquip, String ubicacion, int privacidad) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("UPDATE equipo SET ubicacion=?, privacidad=? WHERE id = ?");
            preparedStatement.setString(1, ubicacion);
            preparedStatement.setInt(2, privacidad);
            preparedStatement.setInt(3, idEquip);

            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String insertarPago(int idUsuario, double coste) {

        String respuesta = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO pago(fecha,usuario,cantidad) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS);
            // Obtener la fecha actual
            LocalDate fechaActual = LocalDate.now();
            // Crear un formateador de fecha
            DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // Formatear la fecha actual usando el formateador
            String fechaFormateada = fechaActual.format(formateador);

            preparedStatement.setString(1, fechaFormateada);
            preparedStatement.setInt(2, idUsuario);
            preparedStatement.setDouble(3, coste);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                ResultSet rs = preparedStatement.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    respuesta = id + "";
                    return respuesta;
                }
            } else {
                return respuesta;
            }
        } catch (SQLException ex) {
            return null;
        }
        return null;
    }

    public boolean inscribirUsuarioPartidoDePago(int idUsuario, int idPartido, int idPago) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO participar(usuario,partido,pago) VALUES(?,?,?)");
            preparedStatement.setInt(1, idUsuario);
            preparedStatement.setInt(2, idPartido);
            preparedStatement.setInt(3, idPago);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            return false;
        }
    }

    public boolean insertarEquipoEnLigaDePago(int idLiga, String nombreEquipo, int idPago) {

        int idEquipo = equipoId(nombreEquipo);

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO InscrippcionLiga(liga,equipo,pago) VALUES(?,?,?)");
            preparedStatement.setInt(1, idLiga);
            preparedStatement.setInt(2, idEquipo);
            preparedStatement.setInt(3, idPago);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            return false;
        }
    }

    public boolean insertarEquipoEnTorneoDePago(int idTorneo, String nombreEquipo, int idPago) {

        int idEquipo = equipoId(nombreEquipo);

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO InscrippcionTorneo(torneo,equipo,pago) VALUES(?,?,?)");
            preparedStatement.setInt(1, idTorneo);
            preparedStatement.setInt(2, idEquipo);
            preparedStatement.setInt(3, idPago);

            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            return false;
        }
    }

    public String emailUsuario(int idUsuario) {

        String respuesta = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT email FROM usuario WHERE id = ?");
            preparedStatement.setInt(1, idUsuario);

            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                return rset.getString(1);
            }

            return respuesta;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public boolean modificarDatosUsuario(String email, String passwd, int idUsuario) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            if (passwd.equalsIgnoreCase(" ")) {
                PreparedStatement preparedStatement = conn.prepareStatement("UPDATE usuario SET email = ? WHERE id = ?");
                preparedStatement.setString(1, email);
                preparedStatement.setInt(2, idUsuario);

                int insertado = preparedStatement.executeUpdate();
                if (insertado > 0) {
                    return true;

                } else {
                    return false;
                }
            } else {
                PreparedStatement preparedStatement = conn.prepareStatement("UPDATE usuario SET email = ?, passwd = ? WHERE id = ?");
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, passwd);
                preparedStatement.setInt(3, idUsuario);

                int insertado = preparedStatement.executeUpdate();
                if (insertado > 0) {
                    return true;

                } else {
                    return false;
                }
            }

        } catch (Exception ex) {
            System.out.println("ERROR:" + ex.getMessage());
            return false;
        }
    }

    public boolean insertarDeporte(String deporte, int cantidad, int ctdadXEquipo) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO deporte(nombreDto,ctdadJugadores,ctdadXequipo) VALUES(?,?,?);");
            preparedStatement.setString(1, deporte);
            preparedStatement.setInt(2, cantidad);
            preparedStatement.setInt(3, ctdadXEquipo);

            //System.out.println(preparedStatement);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String equiposAdmin() {

        String respuesta = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT e.nombre, d.nombreDto, u.nombre, e.ubicacion, p.nombre FROM equipo e JOIN deporte d ON (e.deporte = d.id) JOIN usuario u ON (e.lider = u.id) JOIN privacidad p ON (p.id=e.privacidad) WHERE e.nombre NOT LIKE 'ELIMINADO'");

            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                respuesta += rset.getString(1) + ":" + rset.getString(2) + ":" + rset.getString(3) + ":" + rset.getString(4) + ":" + rset.getString(5) + ";";
            }

            return respuesta;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String usuariosAdmin() {

        String respuesta = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT id, nombre, email FROM usuario");

            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                respuesta += rset.getString(1) + ":" + rset.getString(2) + ":" + rset.getString(3) + ":" + rolesUsuario(rset.getInt(1)) + ";";
            }

            return respuesta;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String rolesUsuario(int idUsuario) {
        String respuesta = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT r.nombre FROM rolasociado ra JOIN usuario u ON(ra.usuario = u.id) JOIN rol r ON (r.id=ra.rol) WHERE u.id = ?");
            preparedStatement.setInt(1, idUsuario);

            ResultSet rset = preparedStatement.executeQuery();

            while (rset.next()) {
                respuesta += rset.getString(1) + ", ";
            }

            if (respuesta.endsWith(", ")) {
                respuesta = respuesta.substring(0, respuesta.length() - 2);
            }

            return respuesta;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public boolean asignarRolAdministrador(int id) {
        //ROL ADMINISTRADOR = 1;
        int administrador = 1;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO rolasociado(rol,usuario) VALUES(?,?)");
            preparedStatement.setInt(1, administrador);
            preparedStatement.setInt(2, id);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;

    }

    public String partidosAdmin() {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            //ubicacion!fInicio!hInicio!fLimite!hLimiite!coste!deporte!estado!maxInscritos!inscritos!id
            String partidos = "";
            int idP = 0;

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT p.id, d.nombreDto, u.nombre, p.ubicacion, e.nombre, p.fInicio FROM partido p JOIN usuario u ON (p.creador=u.id) JOIN estado e ON (e.id = p.estado)JOIN deporte d ON (d.id = p.deporte) ORDER BY p.estado ASC");
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {

                partidos += rset.getInt(1) + "!" + rset.getString(2) + "!" + rset.getString(3) + "!" + rset.getString(4) + "!" + rset.getString(5) + "!" + rset.getString(6) + ";";

            }

            return partidos;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;

    }

    public boolean eliminarPartido(int idPartido) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM partido WHERE id=?");
            preparedStatement.setInt(1, idPartido);

            int borrado = preparedStatement.executeUpdate();
            if (borrado > 0) {

                return true;

            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String torneosAdmin() {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            String torneos = "";

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT t.id, t.nombre, d.nombreDto, u.nombre, t.ubicacion, e.nombre FROM torneo t JOIN usuario u ON (t.organizador = u.id) JOIN estado e ON (t.estado = e.id) JOIN deporte d ON (t.deporte = d.id) ORDER BY e.id ASC");
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {

                torneos += rset.getInt(1) + "!" + rset.getString(2) + "!" + rset.getString(3) + "!" + rset.getString(4) + "!" + rset.getString(5) + "!" + rset.getString(6) + ";";

            }

            return torneos;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;

    }

    public boolean eliminarTorneo(int idTorneo) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM torneo WHERE id=?");
            preparedStatement.setInt(1, idTorneo);

            int borrado = preparedStatement.executeUpdate();
            if (borrado > 0) {

                return true;

            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String ligasAdmin() {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            String torneos = "";

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT t.id, t.nombre, d.nombreDto, u.nombre, t.ubicacion, e.nombre FROM liga t JOIN usuario u ON (t.organizador = u.id) JOIN estado e ON (t.estado = e.id) JOIN deporte d ON (t.deporte = d.id) ORDER BY e.id ASC");
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {

                torneos += rset.getInt(1) + "!" + rset.getString(2) + "!" + rset.getString(3) + "!" + rset.getString(4) + "!" + rset.getString(5) + "!" + rset.getString(6) + ";";

            }

            return torneos;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;

    }

    public boolean eliminarLiga(int idLiga) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM liga WHERE id=?");
            preparedStatement.setInt(1, idLiga);

            int borrado = preparedStatement.executeUpdate();
            if (borrado > 0) {

                return true;

            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean insertarSolicitudDeporte(String deporte, int ctdadT, int ctdadE, String informacion, int idUsu) {
       
        int estado = 5;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO peticion(nombreDto,ctdadJugadores,ctdadEquipos,informacionExtra,usuario,estado) VALUES(?,?,?,?,?,?)");
            preparedStatement.setString(1, deporte);
            preparedStatement.setInt(2, ctdadT);
            preparedStatement.setInt(3, ctdadE);
            preparedStatement.setString(4, informacion);
            preparedStatement.setInt(5, idUsu);
            preparedStatement.setInt(6, estado);
            int insertado = preparedStatement.executeUpdate();
            if (insertado > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;

    }
    
    public String peticiones() {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {

            String peticion = "";

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT p.id, p.nombreDto, p.ctdadJugadores, p.ctdadEquipos,p.informacionExtra, u.nombre FROM peticion p JOIN usuario u ON (p.usuario = u.id) WHERE estado = 5;");
            ResultSet rset = preparedStatement.executeQuery();
            while (rset.next()) {

                peticion += rset.getInt(1) + ":" + rset.getString(2) + ":" + rset.getInt(3) + ":" + rset.getInt(4) + ":" + rset.getString(5) + ":" + rset.getString(6) + ";";

            }

            return peticion;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;

    }
    
    public boolean cambiarEstadoPeticion(int idPet, int nuevoEstado) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);) {
            PreparedStatement pstmt = conn.prepareStatement("UPDATE peticion SET estado = ? WHERE id = ?");

            pstmt.setInt(1, nuevoEstado);
            pstmt.setInt(2, idPet);
            int insertado = pstmt.executeUpdate();
            if (insertado > 0) {

                return true;

            } else {
                return false;
            }

        } catch (SQLException ex) {
            System.out.println("Error al cambiar el estado de la peticion: " + ex.getMessage());
        }
        return false;
    }
}
