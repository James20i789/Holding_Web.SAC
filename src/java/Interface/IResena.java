/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Interface;

import Modelos.Resena;
import java.util.List;

/**
 *
 * @author HP
 */
public interface IResena {
    boolean registrar(Resena resena);
 
    List<Resena> listarPorProducto(int idProducto);
 
    double obtenerPromedioCalificacion(int idProducto);
 
    List<Resena> listarUltimas(int limite);
}
