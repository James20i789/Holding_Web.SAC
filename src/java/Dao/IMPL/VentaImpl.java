package Dao.IMPL;

import Interface.IVenta;
import Modelos.DetalleVenta;
import Modelos.Venta;
import Util.ConexionBD;
import java.sql.*;
import java.util.*;

public class VentaImpl implements IVenta {

    /* ================================================================
     *  REGISTRAR VENTA COMPLETA (cabecera + detalles en transacción)
     * ================================================================ */
    @Override
    public boolean registrarVenta(Venta venta) {
        String sqlVenta = "INSERT INTO venta (id_usuario, nombre_cliente, mesa, tipo_servicio, "
                + "metodo_pago, estado, notas, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, "
                + "precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        String sqlStock  = "UPDATE producto SET stock = stock - ? "
                + "WHERE id_producto = ? AND stock >= ?";

        Connection con = null;
        try {
            con = ConexionBD.getInstancia().getConexion();
            con.setAutoCommit(false); // TRANSACCIÓN

            // 1. Insertar cabecera
            int idVenta;
            try (PreparedStatement ps = con.prepareStatement(sqlVenta,
                    Statement.RETURN_GENERATED_KEYS)) {

                if (venta.getIdUsuario() > 0)
                    ps.setInt(1, venta.getIdUsuario());
                else
                    ps.setNull(1, Types.INTEGER);

                ps.setString(2, venta.getNombreCliente());
                ps.setString(3, venta.getMesa());
                ps.setString(4, venta.getTipoServicio());
                ps.setString(5, venta.getMetodoPago());
                ps.setString(6, venta.getEstado() != null ? venta.getEstado() : "nuevo");
                ps.setString(7, venta.getNotas());
                ps.setDouble(8, venta.getTotal());

                if (ps.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) idVenta = rs.getInt(1);
                    else { con.rollback(); return false; }
                }
            }

            // 2. Insertar detalles y descontar stock
            if (venta.getDetalles() != null) {
                for (DetalleVenta d : venta.getDetalles()) {
                    // Detalle
                    try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                        ps.setInt(1, idVenta);
                        ps.setInt(2, d.getIdProducto());
                        ps.setInt(3, d.getCantidad());
                        ps.setDouble(4, d.getPrecioUnitario());
                        ps.setDouble(5, d.getSubtotal());
                        ps.executeUpdate();
                    }
                    // Stock
                    try (PreparedStatement ps = con.prepareStatement(sqlStock)) {
                        ps.setInt(1, d.getCantidad());
                        ps.setInt(2, d.getIdProducto());
                        ps.setInt(3, d.getCantidad());
                        int filas = ps.executeUpdate();
                        if (filas == 0) { // stock insuficiente
                            con.rollback();
                            return false;
                        }
                    }
                }
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al registrar venta: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /* ================================================================
     *  ACTUALIZAR ESTADO (nuevo→pendiente→preparando→listo→entregado)
     * ================================================================ */
    @Override
    public boolean actualizarEstado(int idVenta, String nuevoEstado) {
        String sql = "UPDATE venta SET estado = ? WHERE id_venta = ?";
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idVenta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar estado: " + e.getMessage());
            return false;
        }
    }

    /* ================================================================
     *  ASIGNAR REPARTIDOR (para delivery)
     * ================================================================ */
    @Override
    public boolean asignarRepartidor(int idVenta, int idRepartidor) {
        String sql = "UPDATE venta SET id_repartidor = ?, estado = 'en_camino' "
                + "WHERE id_venta = ?";
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRepartidor);
            ps.setInt(2, idVenta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al asignar repartidor: " + e.getMessage());
            return false;
        }
    }

    /* ================================================================
     *  LISTAR TODAS (con detalles de productos via GROUP_CONCAT)
     * ================================================================ */
    @Override
    public List<Venta> listarTodas() {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT v.*, "
                + "COALESCE(v.nombre_cliente, u.nombre) AS cliente_display, "
                + "GROUP_CONCAT(CONCAT(dv.cantidad,'x ',p.nombre) SEPARATOR ', ') AS detalle_txt "
                + "FROM venta v "
                + "LEFT JOIN usuario u ON v.id_usuario = u.id_usuario "
                + "LEFT JOIN detalle_venta dv ON v.id_venta = dv.id_venta "
                + "LEFT JOIN producto p ON dv.id_producto = p.id_producto "
                + "GROUP BY v.id_venta "
                + "ORDER BY v.fecha_venta DESC";
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearVenta(rs));
        } catch (SQLException e) {
            System.err.println("Error al listar ventas: " + e.getMessage());
        }
        return lista;
    }

    /* ================================================================
     *  LISTAR POR ESTADO (para cocina, delivery, etc.)
     * ================================================================ */
    @Override
    public List<Venta> listarPorEstado(String estado) {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT v.*, "
                + "COALESCE(v.nombre_cliente, u.nombre) AS cliente_display, "
                + "GROUP_CONCAT(CONCAT(dv.cantidad,'x ',p.nombre) SEPARATOR ', ') AS detalle_txt "
                + "FROM venta v "
                + "LEFT JOIN usuario u ON v.id_usuario = u.id_usuario "
                + "LEFT JOIN detalle_venta dv ON v.id_venta = dv.id_venta "
                + "LEFT JOIN producto p ON dv.id_producto = p.id_producto "
                + "WHERE v.estado = ? "
                + "GROUP BY v.id_venta "
                + "ORDER BY v.fecha_venta ASC";
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearVenta(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar por estado: " + e.getMessage());
        }
        return lista;
    }

    /* ================================================================
     *  LISTAR POR TIPO DE SERVICIO
     * ================================================================ */
    @Override
    public List<Venta> listarPorTipoServicio(String tipoServicio) {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT v.*, "
                + "COALESCE(v.nombre_cliente, u.nombre) AS cliente_display, "
                + "GROUP_CONCAT(CONCAT(dv.cantidad,'x ',p.nombre) SEPARATOR ', ') AS detalle_txt "
                + "FROM venta v "
                + "LEFT JOIN usuario u ON v.id_usuario = u.id_usuario "
                + "LEFT JOIN detalle_venta dv ON v.id_venta = dv.id_venta "
                + "LEFT JOIN producto p ON dv.id_producto = p.id_producto "
                + "WHERE v.tipo_servicio = ? "
                + "GROUP BY v.id_venta "
                + "ORDER BY v.fecha_venta DESC";
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tipoServicio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearVenta(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar por tipo: " + e.getMessage());
        }
        return lista;
    }

    /* ================================================================
     *  BUSCAR POR ID
     * ================================================================ */
    @Override
    public Venta buscarPorId(int idVenta) {
        String sql = "SELECT v.*, "
                + "COALESCE(v.nombre_cliente, u.nombre) AS cliente_display, "
                + "GROUP_CONCAT(CONCAT(dv.cantidad,'x ',p.nombre) SEPARATOR ', ') AS detalle_txt "
                + "FROM venta v "
                + "LEFT JOIN usuario u ON v.id_usuario = u.id_usuario "
                + "LEFT JOIN detalle_venta dv ON v.id_venta = dv.id_venta "
                + "LEFT JOIN producto p ON dv.id_producto = p.id_producto "
                + "WHERE v.id_venta = ? "
                + "GROUP BY v.id_venta";
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearVenta(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar venta: " + e.getMessage());
        }
        return null;
    }

    /* ================================================================
     *  REPORTE: TOTAL INGRESOS (SUM)
     * ================================================================ */
    @Override
    public double obtenerTotalIngresos() {
        String sql = "SELECT SUM(total) AS total_ingresos FROM venta "
                + "WHERE estado NOT IN ('rechazado', 'nuevo')";
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble("total_ingresos");
        } catch (SQLException e) {
            System.err.println("Error al obtener ingresos: " + e.getMessage());
        }
        return 0;
    }

    /* ================================================================
     *  REPORTE: VENTAS POR DÍA (GROUP BY + SUM + ORDER BY)
     * ================================================================ */
    @Override
    public List<Map<String, Object>> reporteVentasPorDia() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT DATE(fecha_venta) AS dia, "
                + "COUNT(*) AS cantidad_pedidos, "
                + "SUM(total) AS ingresos_totales, "
                + "AVG(total) AS ticket_promedio "
                + "FROM venta "
                + "WHERE estado <> 'rechazado' "
                + "GROUP BY DATE(fecha_venta) "
                + "ORDER BY dia DESC "
                + "LIMIT 30";
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("dia", rs.getString("dia"));
                fila.put("cantidad_pedidos", rs.getInt("cantidad_pedidos"));
                fila.put("ingresos_totales", rs.getDouble("ingresos_totales"));
                fila.put("ticket_promedio", rs.getDouble("ticket_promedio"));
                lista.add(fila);
            }
        } catch (SQLException e) {
            System.err.println("Error en reporte por día: " + e.getMessage());
        }
        return lista;
    }

    /* ================================================================
     *  REPORTE: PRODUCTOS MÁS VENDIDOS (JOIN + GROUP BY + SUM + ORDER BY + LIMIT)
     * ================================================================ */
    @Override
    public List<Map<String, Object>> reporteProductosMasVendidos(int limite) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT p.nombre AS producto, "
                + "c.nombre AS categoria, "
                + "SUM(dv.cantidad) AS total_vendido, "
                + "SUM(dv.subtotal) AS total_ingresos "
                + "FROM detalle_venta dv "
                + "JOIN producto p ON dv.id_producto = p.id_producto "
                + "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "GROUP BY p.id_producto, p.nombre, c.nombre "
                + "ORDER BY total_vendido DESC "
                + "LIMIT ?";
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("producto", rs.getString("producto"));
                    fila.put("categoria", rs.getString("categoria"));
                    fila.put("total_vendido", rs.getInt("total_vendido"));
                    fila.put("total_ingresos", rs.getDouble("total_ingresos"));
                    lista.add(fila);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en reporte productos: " + e.getMessage());
        }
        return lista;
    }

    /* ================================================================
     *  REPORTE: INGRESOS POR CATEGORÍA (JOIN + GROUP BY + SUM)
     * ================================================================ */
    @Override
    public List<Map<String, Object>> reporteIngresosPorCategoria() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT c.nombre AS categoria, c.color, "
                + "COUNT(DISTINCT p.id_producto) AS cantidad_productos, "
                + "COALESCE(SUM(dv.subtotal), 0) AS ingresos_generados "
                + "FROM categoria c "
                + "LEFT JOIN producto p ON p.id_categoria = c.id_categoria "
                + "LEFT JOIN detalle_venta dv ON dv.id_producto = p.id_producto "
                + "GROUP BY c.id_categoria, c.nombre, c.color "
                + "ORDER BY ingresos_generados DESC";
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("categoria", rs.getString("categoria"));
                fila.put("color", rs.getString("color"));
                fila.put("cantidad_productos", rs.getInt("cantidad_productos"));
                fila.put("ingresos_generados", rs.getDouble("ingresos_generados"));
                lista.add(fila);
            }
        } catch (SQLException e) {
            System.err.println("Error en reporte categorías: " + e.getMessage());
        }
        return lista;
    }

    /* ================================================================
     *  MAPPER INTERNO
     * ================================================================ */
    private Venta mapearVenta(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setIdVenta(rs.getInt("id_venta"));
        v.setNombreCliente(rs.getString("cliente_display"));
        v.setMesa(rs.getString("mesa"));
        v.setTipoServicio(rs.getString("tipo_servicio"));
        v.setMetodoPago(rs.getString("metodo_pago"));
        v.setEstado(rs.getString("estado"));
        v.setNotas(rs.getString("notas"));
        v.setTotal(rs.getDouble("total"));
        v.setFechaVenta(rs.getTimestamp("fecha_venta"));
        v.setDetalleTexto(rs.getString("detalle_txt"));
        return v;
    }
}