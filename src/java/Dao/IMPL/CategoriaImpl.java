/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dao.IMPL;
import java.sql.*;
import Interface.ICategoria;
import Modelos.Categoria;
import Util.ConexionBD;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author HP
 */
public class CategoriaImpl implements ICategoria {
 
    @Override
    public boolean insertar(Categoria categoria) {
        String sql = "INSERT INTO categoria (nombre, color) VALUES (?, ?)";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getColor());
 
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            System.err.println("Error al insertar categoría: " + e.getMessage());
            return false;
        }
    }
 
    @Override
    public boolean actualizar(Categoria categoria) {
        String sql = "UPDATE categoria SET nombre = ?, color = ? WHERE id_categoria = ?";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getColor());
            ps.setInt(3, categoria.getIdCategoria());
 
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            System.err.println("Error al actualizar categoría: " + e.getMessage());
            return false;
        }
    }
 
    @Override
    public boolean eliminar(int idCategoria) {
        // Los productos vinculados quedan con id_categoria = NULL (ON DELETE SET NULL)
        String sql = "DELETE FROM categoria WHERE id_categoria = ?";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, idCategoria);
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            System.err.println("Error al eliminar categoría: " + e.getMessage());
            return false;
        }
    }
 
    @Override
    public List<Categoria> listarTodas() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM categoria WHERE estado = 1 ORDER BY nombre ASC";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                lista.add(mapearCategoria(rs));
            }
 
        } catch (SQLException e) {
            System.err.println("Error al listar categorías: " + e.getMessage());
        }
        return lista;
    }
 
    @Override
    public Categoria buscarPorId(int idCategoria) {
        String sql = "SELECT * FROM categoria WHERE id_categoria = ?";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, idCategoria);
 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCategoria(rs);
                }
            }
            return null;
 
        } catch (SQLException e) {
            System.err.println("Error al buscar categoría: " + e.getMessage());
            return null;
        }
    }
 
    private Categoria mapearCategoria(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setIdCategoria(rs.getInt("id_categoria"));
        c.setNombre(rs.getString("nombre"));
        c.setColor(rs.getString("color"));
        c.setEstado(rs.getBoolean("estado"));
        return c;
    }
}
