/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dao.IMPL;

import Interface.IUsuario;
import Modelos.Usuario;
import Util.ConexionBD;
import Util.PasswordUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author HP
 */
public class UsuarioImpl implements IUsuario {
 
    @Override
    public boolean registrar(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre, correo, password, direccion, id_rol) "
                + "VALUES (?, ?, ?, ?, ?)";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getCorreo().toLowerCase());
            ps.setString(3, PasswordUtil.hashSHA256(usuario.getPassword()));
            ps.setString(4, usuario.getDireccion());
            ps.setInt(5, usuario.getIdRol());
 
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }
 
    @Override
    public Usuario login(String identificador, String passwordPlano) {
        // Permite ingresar por correo, por nombre de rol (admin, mozo, etc.) o por id (para usuarios de sistema)
        String sql = "SELECT u.*, r.nombre_rol FROM usuario u "
                + "JOIN rol r ON u.id_rol = r.id_rol "
                + "WHERE LOWER(u.correo) = LOWER(?) "
                + "OR (u.es_sistema = 1 AND LOWER(r.nombre_rol) = LOWER(?)) "
                + "OR (u.es_sistema = 1 AND u.id_usuario = ?) "
                + "LIMIT 1";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, identificador);
            ps.setString(2, identificador);
 
            int idIntento = -1;
            try {
                idIntento = Integer.parseInt(identificador.trim());
            } catch (NumberFormatException ignored) { }
            ps.setInt(3, idIntento);
 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashGuardado = rs.getString("password");
                    if (PasswordUtil.verificar(passwordPlano, hashGuardado)) {
                        return mapearUsuario(rs);
                    }
                }
            }
            return null;
 
        } catch (SQLException e) {
            System.err.println("Error en login: " + e.getMessage());
            return null;
        }
    }
 
    @Override
    public boolean correoExiste(String correo) {
        String sql = "SELECT COUNT(*) AS total FROM usuario WHERE LOWER(correo) = LOWER(?)";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, correo);
 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }
            return false;
 
        } catch (SQLException e) {
            System.err.println("Error al verificar correo: " + e.getMessage());
            return false;
        }
    }
 
    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.*, r.nombre_rol FROM usuario u "
                + "JOIN rol r ON u.id_rol = r.id_rol "
                + "ORDER BY u.fecha_registro DESC";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                lista.add(mapearUsuario(rs));
            }
 
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return lista;
    }
 
    @Override
    public Usuario buscarPorId(int idUsuario) {
        String sql = "SELECT u.*, r.nombre_rol FROM usuario u "
                + "JOIN rol r ON u.id_rol = r.id_rol "
                + "WHERE u.id_usuario = ?";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, idUsuario);
 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
            return null;
 
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario: " + e.getMessage());
            return null;
        }
    }
 
    @Override
    public boolean actualizar(Usuario usuario) {
        String sql = "UPDATE usuario SET nombre = ?, direccion = ? WHERE id_usuario = ?";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getDireccion());
            ps.setInt(3, usuario.getIdUsuario());
 
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }
 
    @Override
    public boolean eliminar(int idUsuario) {
        String sql = "DELETE FROM usuario WHERE id_usuario = ? AND es_sistema = 0";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }
 
    @Override
    public List<Usuario> listarPorRol(String nombreRol) {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.*, r.nombre_rol FROM usuario u "
                + "JOIN rol r ON u.id_rol = r.id_rol "
                + "WHERE LOWER(r.nombre_rol) = LOWER(?) "
                + "ORDER BY u.nombre ASC";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, nombreRol);
 
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearUsuario(rs));
                }
            }
 
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios por rol: " + e.getMessage());
        }
        return lista;
    }
 
    // ===== Mapper interno =====
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setNombre(rs.getString("nombre"));
        u.setCorreo(rs.getString("correo"));
        u.setPassword(rs.getString("password"));
        u.setDireccion(rs.getString("direccion"));
        u.setIdRol(rs.getInt("id_rol"));
        u.setNombreRol(rs.getString("nombre_rol"));
        u.setEsSistema(rs.getBoolean("es_sistema"));
        u.setEstado(rs.getBoolean("estado"));
        u.setFechaRegistro(rs.getTimestamp("fecha_registro"));
        return u;
    }
}
