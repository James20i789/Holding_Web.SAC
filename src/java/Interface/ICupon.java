/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Interface;

import Modelos.Cupon;

/**
 *
 * @author ElBatan
 */
public interface ICupon {
    Cupon validarCupon(String codigo);
 
    boolean registrarUso(String codigo);
 
    boolean insertar(Cupon cupon);
}
