/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelos;

import java.sql.Timestamp;
 
public class Usuario {
 
    private int idUsuario;
    private String nombre;
    private String correo;
    private String password;
    private String direccion;
    private int idRol;
    private String nombreRol; // se llena al hacer JOIN con la tabla rol
    private boolean esSistema;
    private boolean estado;
    private Timestamp fechaRegistro;
 
    public Usuario() {
    }
 
    public Usuario(String nombre, String correo, String password, String direccion, int idRol) {
        this.nombre = nombre;
        this.correo = correo;
        this.password = password;
        this.direccion = direccion;
        this.idRol = idRol;
    }
 
    public int getIdUsuario() {
        return idUsuario;
    }
 
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
 
    public String getNombre() {
        return nombre;
    }
 
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
 
    public String getCorreo() {
        return correo;
    }
 
    public void setCorreo(String correo) {
        this.correo = correo;
    }
 
    public String getPassword() {
        return password;
    }
 
    public void setPassword(String password) {
        this.password = password;
    }
 
    public String getDireccion() {
        return direccion;
    }
 
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
 
    public int getIdRol() {
        return idRol;
    }
 
    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }
 
    public String getNombreRol() {
        return nombreRol;
    }
 
    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }
 
    public boolean isEsSistema() {
        return esSistema;
    }
 
    public void setEsSistema(boolean esSistema) {
        this.esSistema = esSistema;
    }
 
    public boolean isEstado() {
        return estado;
    }
 
    public void setEstado(boolean estado) {
        this.estado = estado;
    }
 
    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }
 
    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}