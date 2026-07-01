/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Interface;

/**
 *
 * @author ElBatan
 */
import Modelos.Producto;
import java.util.List;
 
public interface IProducto {
 
    boolean insertar(Producto producto);
 
    boolean actualizar(Producto producto);
 
    boolean eliminar(int idProducto);
 
    List<Producto> listarTodos();
 
    List<Producto> listarPorCategoria(int idCategoria);
 
    List<Producto> buscarPorNombre(String texto);
 
    Producto buscarPorId(int idProducto);
 
    boolean actualizarStock(int idProducto, int cantidadVendida);
 
    List<Producto> listarMasVendidos(int limite);
 
    List<Producto> listarStockBajo(int umbral);
}
