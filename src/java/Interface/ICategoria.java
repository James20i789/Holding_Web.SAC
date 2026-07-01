/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Interface;

import Modelos.Categoria;
import java.util.List;

/**
 *
 * @author HP
 */
public interface ICategoria {
 
    boolean insertar(Categoria categoria);
 
    boolean actualizar(Categoria categoria);
 
    boolean eliminar(int idCategoria);
 
    List<Categoria> listarTodas();
 
    Categoria buscarPorId(int idCategoria);
}
 