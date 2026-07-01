/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelos;

/**
 *
 * @author HP
 */
public class Producto {
  private int idProducto;
    private String nombre;
    private String descripcion;
    private double precio;
    private int stock;
    private String imagen;
    private int idCategoria;
    private String nombreCategoria; // se llena al hacer JOIN con categoria
    private String colorCategoria;
    private boolean estado;
 
    public Producto() {
    }
 
    public Producto(String nombre, String descripcion, double precio, int stock, String imagen, int idCategoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.imagen = imagen;
        this.idCategoria = idCategoria;
    }
 
    public int getIdProducto() {
        return idProducto;
    }
 
    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }
 
    public String getNombre() {
        return nombre;
    }
 
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
 
    public String getDescripcion() {
        return descripcion;
    }
 
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
 
    public double getPrecio() {
        return precio;
    }
 
    public void setPrecio(double precio) {
        this.precio = precio;
    }
 
    public int getStock() {
        return stock;
    }
 
    public void setStock(int stock) {
        this.stock = stock;
    }
 
    public String getImagen() {
        return imagen;
    }
 
    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
 
    public int getIdCategoria() {
        return idCategoria;
    }
 
    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }
 
    public String getNombreCategoria() {
        return nombreCategoria;
    }
 
    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }
 
    public String getColorCategoria() {
        return colorCategoria;
    }
 
    public void setColorCategoria(String colorCategoria) {
        this.colorCategoria = colorCategoria;
    }
 
    public boolean isEstado() {
        return estado;
    }
 
    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
