/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dao.IMPL;

import Interface.IProducto;
import Modelos.Producto;
import Util.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author HP
 */
public class ProductoImpl implements IProducto {
 
    @Override
    public boolean insertar(Producto producto) {
        String sql = "INSERT INTO producto (nombre, descripcion, precio, stock, imagen, id_categoria) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
 
            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getDescripcion());
            ps.setDouble(3, producto.getPrecio());
            ps.setInt(4, producto.getStock());
            ps.setString(5, producto.getImagen());
            ps.setInt(6, producto.getIdCategoria());
 
            int filas = ps.executeUpdate();
            return filas > 0;
 
        } catch (SQLException e) {
            System.err.println("Error al insertar producto: " + e.getMessage());
            return false;
        }
    }
 
    @Override
    public boolean actualizar(Producto producto) {
        String sql = "UPDATE producto SET nombre = ?, descripcion = ?, precio = ?, "
                + "stock = ?, imagen = ?, id_categoria = ? WHERE id_producto = ?";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getDescripcion());
            ps.setDouble(3, producto.getPrecio());
            ps.setInt(4, producto.getStock());
            ps.setString(5, producto.getImagen());
            ps.setInt(6, producto.getIdCategoria());
            ps.setInt(7, producto.getIdProducto());
 
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }
 
    @Override
    public boolean eliminar(int idProducto) {
        String sql = "DELETE FROM producto WHERE id_producto = ?";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }
 
    @Override
    public List<Producto> listarTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre AS nombre_categoria, c.color AS color_categoria "
                + "FROM producto p "
                + "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "WHERE p.estado = 1 "
                + "ORDER BY p.nombre ASC";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                lista.add(mapearProducto(rs));
            }
 
        } catch (SQLException e) {
            System.err.println("Error al listar productos: " + e.getMessage());
        }
        return lista;
    }
 
    @Override
    public List<Producto> listarPorCategoria(int idCategoria) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre AS nombre_categoria, c.color AS color_categoria "
                + "FROM producto p "
                + "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "WHERE p.id_categoria = ? AND p.estado = 1 "
                + "ORDER BY p.nombre ASC";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, idCategoria);
 
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
 
        } catch (SQLException e) {
            System.err.println("Error al listar productos por categoría: " + e.getMessage());
        }
        return lista;
    }
 
    @Override
    public List<Producto> buscarPorNombre(String texto) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre AS nombre_categoria, c.color AS color_categoria "
                + "FROM producto p "
                + "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "WHERE p.nombre LIKE ? AND p.estado = 1 "
                + "ORDER BY p.nombre ASC";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, "%" + texto + "%");
 
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
 
        } catch (SQLException e) {
            System.err.println("Error al buscar productos: " + e.getMessage());
        }
        return lista;
    }
 
    @Override
    public Producto buscarPorId(int idProducto) {
        String sql = "SELECT p.*, c.nombre AS nombre_categoria, c.color AS color_categoria "
                + "FROM producto p "
                + "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "WHERE p.id_producto = ?";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, idProducto);
 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }
            return null;
 
        } catch (SQLException e) {
            System.err.println("Error al buscar producto: " + e.getMessage());
            return null;
        }
    }
 
    @Override
    public boolean actualizarStock(int idProducto, int cantidadVendida) {
        String sql = "UPDATE producto SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, cantidadVendida);
            ps.setInt(2, idProducto);
            ps.setInt(3, cantidadVendida);
 
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            System.err.println("Error al actualizar stock: " + e.getMessage());
            return false;
        }
    }
 
    /**
     * NUEVA FUNCIONALIDAD: ranking de productos más vendidos.
     * Usa JOIN + GROUP BY + SUM + ORDER BY + LIMIT.
     */
    @Override
    public List<Producto> listarMasVendidos(int limite) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre AS nombre_categoria, c.color AS color_categoria, "
                + "SUM(dv.cantidad) AS total_vendido "
                + "FROM detalle_venta dv "
                + "JOIN producto p ON dv.id_producto = p.id_producto "
                + "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "GROUP BY p.id_producto "
                + "ORDER BY total_vendido DESC "
                + "LIMIT ?";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, limite);
 
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
 
        } catch (SQLException e) {
            System.err.println("Error al listar más vendidos: " + e.getMessage());
        }
        return lista;
    }
 
    /**
     * NUEVA FUNCIONALIDAD: alerta de stock bajo (para el panel admin).
     */
    @Override
    public List<Producto> listarStockBajo(int umbral) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre AS nombre_categoria, c.color AS color_categoria "
                + "FROM producto p "
                + "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "WHERE p.stock <= ? AND p.estado = 1 "
                + "ORDER BY p.stock ASC";
 
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, umbral);
 
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
 
        } catch (SQLException e) {
            System.err.println("Error al listar stock bajo: " + e.getMessage());
        }
        return lista;
    }
 
    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("id_producto"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecio(rs.getDouble("precio"));
        p.setStock(rs.getInt("stock"));
        p.setImagen(rs.getString("imagen"));
        p.setIdCategoria(rs.getInt("id_categoria"));
        p.setNombreCategoria(rs.getString("nombre_categoria"));
        p.setColorCategoria(rs.getString("color_categoria"));
        p.setEstado(rs.getBoolean("estado"));
        return p;
    }
}
