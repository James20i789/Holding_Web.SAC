package Dao.IMPL;

import Interface.IResena;
import Modelos.Resena;
import Util.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResenaImpl implements IResena {

    @Override
    public boolean registrar(Resena resena) {
        // Evitar doble reseña del mismo usuario al mismo producto
        String sqlCheck = "SELECT COUNT(*) FROM resena WHERE id_usuario = ? AND id_producto = ?";
        String sqlInsert = "INSERT INTO resena (id_usuario, id_producto, calificacion, comentario) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.getInstancia().getConexion()) {
            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                psCheck.setInt(1, resena.getIdUsuario());
                psCheck.setInt(2, resena.getIdProducto());
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return false; // ya existe
                }
            }
            try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                ps.setInt(1, resena.getIdUsuario());
                ps.setInt(2, resena.getIdProducto());
                ps.setInt(3, resena.getCalificacion());
                ps.setString(4, resena.getComentario());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al registrar reseña: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Resena> listarPorProducto(int idProducto) {
        List<Resena> lista = new ArrayList<>();
        // JOIN con usuario para mostrar el nombre del autor
        String sql = "SELECT r.*, u.nombre AS nombre_usuario "
                + "FROM resena r "
                + "JOIN usuario u ON r.id_usuario = u.id_usuario "
                + "WHERE r.id_producto = ? "
                + "ORDER BY r.fecha DESC";
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearResena(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar reseñas: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public double obtenerPromedioCalificacion(int idProducto) {
        // AVG + GROUP BY
        String sql = "SELECT AVG(calificacion) AS promedio FROM resena WHERE id_producto = ?";
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("promedio");
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular promedio: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public List<Resena> listarUltimas(int limite) {
        List<Resena> lista = new ArrayList<>();
        // JOIN con usuario y producto, ORDER BY fecha DESC, LIMIT
        String sql = "SELECT r.*, u.nombre AS nombre_usuario, p.nombre AS nombre_producto "
                + "FROM resena r "
                + "JOIN usuario u ON r.id_usuario = u.id_usuario "
                + "JOIN producto p ON r.id_producto = p.id_producto "
                + "ORDER BY r.fecha DESC "
                + "LIMIT ?";
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Resena r = mapearResena(rs);
                    r.setNombreProducto(rs.getString("nombre_producto"));
                    lista.add(r);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar últimas reseñas: " + e.getMessage());
        }
        return lista;
    }

    private Resena mapearResena(ResultSet rs) throws SQLException {
        Resena r = new Resena();
        r.setIdResena(rs.getInt("id_resena"));
        r.setIdUsuario(rs.getInt("id_usuario"));
        r.setIdProducto(rs.getInt("id_producto"));
        r.setCalificacion(rs.getInt("calificacion"));
        r.setComentario(rs.getString("comentario"));
        r.setFecha(rs.getTimestamp("fecha"));
        try { r.setNombreUsuario(rs.getString("nombre_usuario")); } catch (SQLException ignored) {}
        return r;
    }
}