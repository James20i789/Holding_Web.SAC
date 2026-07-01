/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Interface;

import Modelos.Venta;
import java.util.List;
import java.util.Map;

/**
 *
 * @author HP
 */
public interface IVenta {
    boolean registrarVenta(Venta venta);
 
    boolean actualizarEstado(int idVenta, String nuevoEstado);
 
    boolean asignarRepartidor(int idVenta, int idRepartidor);
 
    List<Venta> listarTodas();
 
    List<Venta> listarPorEstado(String estado);
 
    List<Venta> listarPorTipoServicio(String tipoServicio);
 
    Venta buscarPorId(int idVenta);
 
    // ===== Reportes (GROUP BY, SUM, ORDER BY) =====
    double obtenerTotalIngresos();
 
    List<Map<String, Object>> reporteVentasPorDia();
 
    List<Map<String, Object>> reporteProductosMasVendidos(int limite);
 
    List<Map<String, Object>> reporteIngresosPorCategoria();
    
}
