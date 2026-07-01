/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Interface;

import Modelos.Usuario;
import java.util.List;

/**
 *
 * @author HP
 */
public interface IUsuario {
     boolean registrar(Usuario usuario);
 
    Usuario login(String identificador, String passwordPlano);
 
    boolean correoExiste(String correo);
 
    List<Usuario> listarTodos();
 
    Usuario buscarPorId(int idUsuario);
 
    boolean actualizar(Usuario usuario);
 
    boolean eliminar(int idUsuario);
 
    List<Usuario> listarPorRol(String nombreRol);
}
