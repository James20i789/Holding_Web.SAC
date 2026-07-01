/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelos;

/**
 *
 * @author ElBatan
 */
public class DetalleVenta {
 
    private int idDetalle;
    private int idVenta;
    private int idProducto;
    private String nombreProducto; // se llena al hacer JOIN con producto
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
 
    public DetalleVenta() {
    }
 
    public DetalleVenta(int idProducto, int cantidad, double precioUnitario) {
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = cantidad * precioUnitario;
    }
 
    public int getIdDetalle() {
        return idDetalle;
    }
 
    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }
 
    public int getIdVenta() {
        return idVenta;
    }
 
    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
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
 
    public int getCantidad() {
        return cantidad;
    }
 
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
 
    public double getPrecioUnitario() {
        return precioUnitario;
    }
 
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
 
    public double getSubtotal() {
        return subtotal;
    }
 
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}
