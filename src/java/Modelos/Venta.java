/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelos;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author HP
 */
public class Venta {
 
    private int idVenta;
    private Integer idUsuario; // puede ser null (pedido anónimo desde VistaCliente)
    private String nombreCliente;
    private String mesa;
    private String tipoServicio;   // Mesa, Para Llevar, Delivery
    private String metodoPago;     // Yape, Plin, Efectivo, Tarjeta
    private String estado;         // nuevo, pendiente, preparando, listo, en_camino, entregado, rechazado
    private String notas;
    private double total;
    private Integer idRepartidor;
    private String nombreRepartidor;
    private Date fechaVenta;
    private List<DetalleVenta> detalles = new ArrayList<>();
    private String detalleTexto; // resumen tipo "2x Pollo, 1x Chicha" (para listas rápidas)

    public Venta() {
    }

    public Venta(int idVenta, Integer idUsuario, String nombreCliente, String mesa, String tipoServicio, String metodoPago, String estado, String notas, double total, Integer idRepartidor, String nombreRepartidor, Date fechaVenta, String detalleTexto) {
        this.idVenta = idVenta;
        this.idUsuario = idUsuario;
        this.nombreCliente = nombreCliente;
        this.mesa = mesa;
        this.tipoServicio = tipoServicio;
        this.metodoPago = metodoPago;
        this.estado = estado;
        this.notas = notas;
        this.total = total;
        this.idRepartidor = idRepartidor;
        this.nombreRepartidor = nombreRepartidor;
        this.fechaVenta = fechaVenta;
        this.detalleTexto = detalleTexto;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getMesa() {
        return mesa;
    }

    public void setMesa(String mesa) {
        this.mesa = mesa;
    }

    public String getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(String tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Integer getIdRepartidor() {
        return idRepartidor;
    }

    public void setIdRepartidor(Integer idRepartidor) {
        this.idRepartidor = idRepartidor;
    }

    public String getNombreRepartidor() {
        return nombreRepartidor;
    }

    public void setNombreRepartidor(String nombreRepartidor) {
        this.nombreRepartidor = nombreRepartidor;
    }

    public Date getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(Date fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }

    public String getDetalleTexto() {
        return detalleTexto;
    }

    public void setDetalleTexto(String detalleTexto) {
        this.detalleTexto = detalleTexto;
    }
 
}