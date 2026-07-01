/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelos;

/**
 *
 * @author HP
 */
public class Categoria {
  private int idCategoria;
    private String nombre;
    private String color;
    private boolean estado;
 
    public Categoria() {
    }
 
    public Categoria(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
    }
 
    public int getIdCategoria() {
        return idCategoria;
    }
 
    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }
 
    public String getNombre() {
        return nombre;
    }
 
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
 
    public String getColor() {
        return color;
    }
 
    public void setColor(String color) {
        this.color = color;
    }
 
    public boolean isEstado() {
        return estado;
    }
 
    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
