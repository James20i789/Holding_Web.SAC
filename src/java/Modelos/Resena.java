/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelos;

import java.security.Timestamp;
import java.util.Date;

/**
 *
 * @author ElBatan
 */

public class Resena {
 
    private int idResena;
    private int idUsuario;
    private String nombreUsuario;
    private int idProducto;
    private String nombreProducto;
    private int calificacion;
    private String comentario;
    private Date fecha;

    public Resena() {
    }

    public Resena(int idResena, int idUsuario, String nombreUsuario, int idProducto, String nombreProducto, int calificacion, String comentario, Date fecha) {
        this.idResena = idResena;
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.fecha = fecha;
    }

    public int getIdResena() {
        return idResena;
    }

    public void setIdResena(int idResena) {
        this.idResena = idResena;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(int calificacion) {
        this.calificacion = calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
 
    
}
