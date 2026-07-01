package Dao.IMPL;

import Interface.ICupon;
import Modelos.Cupon;
import Util.ConexionBD;
import java.sql.*;

public class CuponImpl implements ICupon {

    @Override
    public Cupon validarCupon(String codigo) {
        // Busca cupón activo, vigente y con usos disponibles
        String sql = "SELECT * FROM cupon "
                + "WHERE codigo = ? AND estado = 1 "
                + "AND CURDATE() BETWEEN fecha_inicio AND fecha_fin "
                + "AND usos_actuales < usos_maximos";
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo.toUpperCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearCupon(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al validar cupón: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean registrarUso(String codigo) {
        String sql = "UPDATE cupon SET usos_actuales = usos_actuales + 1 "
                + "WHERE codigo = ? AND usos_actuales < usos_maximos";
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo.toUpperCase().trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar uso de cupón: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean insertar(Cupon cupon) {
        String sql = "INSERT INTO cupon (codigo, descripcion, porcentaje_descuento, "
                + "fecha_inicio, fecha_fin, usos_maximos) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cupon.getCodigo().toUpperCase());
            ps.setString(2, cupon.getDescripcion());
            ps.setDouble(3, cupon.getPorcentajeDescuento());
            ps.setDate(4, new java.sql.Date(cupon.getFechaInicio().getTime()));
            ps.setDate(5, new java.sql.Date(cupon.getFechaFin().getTime()));
            ps.setInt(6, cupon.getUsosMaximos());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar cupón: " + e.getMessage());
            return false;
        }
    }

    private Cupon mapearCupon(ResultSet rs) throws SQLException {
        Cupon c = new Cupon();
        c.setIdCupon(rs.getInt("id_cupon"));
        c.setCodigo(rs.getString("codigo"));
        c.setDescripcion(rs.getString("descripcion"));
        c.setPorcentajeDescuento(rs.getDouble("porcentaje_descuento"));
        c.setFechaInicio(rs.getDate("fecha_inicio"));
        c.setFechaFin(rs.getDate("fecha_fin"));
        c.setEstado(rs.getBoolean("estado"));
        c.setUsosMaximos(rs.getInt("usos_maximos"));
        c.setUsosActuales(rs.getInt("usos_actuales"));
        return c;
    }
}