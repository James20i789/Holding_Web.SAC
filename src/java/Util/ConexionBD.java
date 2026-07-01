package Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author HP
 */
public class ConexionBD {
 
    private static ConexionBD instancia;
        private Connection conexion;
 
    // ===== CONFIGURACIÓN DE LA BASE DE DATOS =====
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/el_batan"
            + "?useSSL=false"
            + "&serverTimezone=America/Lima"
            + "&allowPublicKeyRetrieval=true"
            + "&characterEncoding=UTF-8";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "james"; // <-- Contraseña actualizada
 
    // Constructor privado (patrón Singleton)
    private ConexionBD() {
        try {
            Class.forName(DRIVER);
            conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("No se encontró el Driver de MySQL: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
        }
    }
 
    /**
     * Devuelve la única instancia de ConexionBD (patrón Singleton).
     */
    public static synchronized ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }
 
    /**
     * Devuelve la conexión activa. Si está cerrada o es nula,
     * crea una nueva automáticamente.
     */
    public Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                Class.forName(DRIVER);
                conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error al reconectar con la base de datos: " + e.getMessage());
        }
        return conexion;
    }
 
    /**
     * Cierra la conexión manualmente.
     */
    public void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}