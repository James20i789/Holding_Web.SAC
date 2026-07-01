package controller;

import Dao.IMPL.VentaImpl;
import Modelos.DetalleVenta;
import Modelos.Usuario;
import Modelos.Venta;
import Util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * GET    /api/ventas                     → listar todas
 * GET    /api/ventas?estado=             → filtrar por estado
 * GET    /api/ventas?tipo=               → filtrar por tipo_servicio
 * GET    /api/ventas?id=                 → buscar una
 * GET    /api/ventas?reporte=ingresos    → total ingresos (admin)
 * POST   /api/ventas                     → registrar venta completa
 * PUT    /api/ventas?id=&estado=         → cambiar estado
 */
@WebServlet("/api/ventas")
public class VentaServlet extends HttpServlet {

    private final VentaImpl dao = new VentaImpl();

    /* ============================================================
       GET
       ============================================================ */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

        String idStr    = req.getParameter("id");
        String estado   = req.getParameter("estado");
        String tipo     = req.getParameter("tipo");
        String reporte  = req.getParameter("reporte");

        // ── Reporte ingresos ──────────────────────────────────────
        if ("ingresos".equals(reporte)) {
            if (!esAdminOmozo(req)) {
                resp.setStatus(403);
                resp.getWriter().write(JsonUtil.error("Acceso denegado"));
                return;
            }
            double total = dao.obtenerTotalIngresos();
            resp.getWriter().write("{\"total\":" + total + "}");
            return;
        }

        // ── Buscar por ID ─────────────────────────────────────────
        if (idStr != null && !idStr.isEmpty()) {
            try {
                Venta v = dao.buscarPorId(Integer.parseInt(idStr));
                if (v == null) {
                    resp.setStatus(404);
                    resp.getWriter().write(JsonUtil.error("Venta no encontrada"));
                } else {
                    resp.getWriter().write(serializarVenta(v));
                }
            } catch (NumberFormatException e) {
                resp.setStatus(400);
                resp.getWriter().write(JsonUtil.error("ID inválido"));
            }
            return;
        }

        // ── Filtrar por estado ────────────────────────────────────
        if (estado != null && !estado.isEmpty()) {
            List<Venta> lista = dao.listarPorEstado(estado);
            resp.getWriter().write(serializarLista(lista));
            return;
        }

        // ── Filtrar por tipo servicio ─────────────────────────────
        if (tipo != null && !tipo.isEmpty()) {
            List<Venta> lista = dao.listarPorTipoServicio(tipo);
            resp.getWriter().write(serializarLista(lista));
            return;
        }

        // ── Listar todas ──────────────────────────────────────────
        if (!esAdminOmozo(req)) {
            resp.setStatus(403);
            resp.getWriter().write(JsonUtil.error("Acceso denegado"));
            return;
        }
        resp.getWriter().write(serializarLista(dao.listarTodas()));
    }

    /* ============================================================
       POST → registrar venta completa
       Parámetros:
         nombreCliente, mesa, tipoServicio, metodoPago, notas, total
         idProducto[],  cantidad[],  precioUnitario[]   (arrays paralelos)
       ============================================================ */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");

        // Obtener id de usuario en sesion (puede ser null)
        HttpSession sesion  = req.getSession(false);
        Usuario     usuario = (sesion != null)
                ? (Usuario) sesion.getAttribute("usuario")
                : null;

        String[] idsProducto    = req.getParameterValues("idProducto");
        String[] cantidades      = req.getParameterValues("cantidad");
        String[] preciosUnitarios= req.getParameterValues("precioUnitario");

        if (idsProducto == null || idsProducto.length == 0) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error("No se enviaron productos"));
            return;
        }

        // Construir detalles
        List<DetalleVenta> detalles = new ArrayList<>();
        double totalCalculado = 0;

        for (int i = 0; i < idsProducto.length; i++) {
            try {
                int    idProd = Integer.parseInt(idsProducto[i]);
                int    cant   = Integer.parseInt(cantidades[i]);
                double precio = Double.parseDouble(preciosUnitarios[i]);
                double sub    = cant * precio;

                DetalleVenta d = new DetalleVenta(idProd, cant, precio);
                detalles.add(d);
                totalCalculado += sub;
            } catch (Exception e) {
                resp.setStatus(400);
                resp.getWriter().write(JsonUtil.error("Error en los datos del producto #" + i));
                return;
            }
        }

        // Construir venta
        Venta venta = new Venta();
        venta.setIdUsuario(usuario != null ? usuario.getIdUsuario() : null);
        venta.setNombreCliente(req.getParameter("nombreCliente"));
        venta.setMesa(req.getParameter("mesa"));
        venta.setTipoServicio(coalesce(req.getParameter("tipoServicio"), "Mesa"));
        venta.setMetodoPago(coalesce(req.getParameter("metodoPago"), "Efectivo"));
        venta.setEstado("nuevo");
        venta.setNotas(req.getParameter("notas"));
        venta.setTotal(totalCalculado);
        venta.setDetalles(detalles);

        boolean ok = dao.registrarVenta(venta);

        if (ok) {
            resp.getWriter().write(JsonUtil.ok("Pedido registrado correctamente"));
        } else {
            resp.setStatus(500);
            resp.getWriter().write(JsonUtil.error(
                "No se pudo registrar el pedido (stock insuficiente o error de base de datos)"));
        }
    }

    /* ============================================================
       PUT → cambiar estado de venta
       ?id=&estado=   o   ?id=&repartidor=
       ============================================================ */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

        String idStr        = req.getParameter("id");
        String nuevoEstado  = req.getParameter("estado");
        String repStr       = req.getParameter("repartidor");

        if (idStr == null || idStr.isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error("ID requerido"));
            return;
        }

        int idVenta;
        try {
            idVenta = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error("ID inválido"));
            return;
        }

        // Asignar repartidor
        if (repStr != null && !repStr.isEmpty()) {
            try {
                boolean ok = dao.asignarRepartidor(idVenta, Integer.parseInt(repStr));
                resp.getWriter().write(ok
                        ? JsonUtil.ok("Repartidor asignado")
                        : JsonUtil.error("No se pudo asignar repartidor"));
            } catch (NumberFormatException e) {
                resp.setStatus(400);
                resp.getWriter().write(JsonUtil.error("ID de repartidor inválido"));
            }
            return;
        }

        // Cambiar estado
        if (nuevoEstado == null || nuevoEstado.isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error("Estado requerido"));
            return;
        }

        Set<String> estadosValidos = new HashSet<>(Arrays.asList(
                "nuevo", "pendiente", "preparando", "listo",
                "en_camino", "entregado", "rechazado"));

        if (!estadosValidos.contains(nuevoEstado)) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error("Estado inválido: " + nuevoEstado));
            return;
        }

        boolean ok = dao.actualizarEstado(idVenta, nuevoEstado);
        resp.getWriter().write(ok
                ? JsonUtil.ok("Estado actualizado a: " + nuevoEstado)
                : JsonUtil.error("No se pudo actualizar el estado"));
    }

    /* ============================================================
       Serialización manual → camelCase para los JS
       ============================================================ */
    private String serializarVenta(Venta v) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fecha = v.getFechaVenta() != null
                ? sdf.format(v.getFechaVenta()) : null;

        return "{"
                + "\"idVenta\":"        + v.getIdVenta()  + ","
                + "\"nombreCliente\":\"" + JsonUtil.escapar(v.getNombreCliente()) + "\","
                + "\"mesa\":"           + (v.getMesa() == null ? "null" : "\"" + JsonUtil.escapar(v.getMesa()) + "\"") + ","
                + "\"tipoServicio\":\"" + JsonUtil.escapar(v.getTipoServicio()) + "\","
                + "\"metodoPago\":\""   + JsonUtil.escapar(v.getMetodoPago())   + "\","
                + "\"estado\":\""       + JsonUtil.escapar(v.getEstado())       + "\","
                + "\"notas\":"          + (v.getNotas() == null ? "null" : "\"" + JsonUtil.escapar(v.getNotas()) + "\"") + ","
                + "\"total\":"          + v.getTotal() + ","
                + "\"fechaVenta\":"     + (fecha == null ? "null" : "\"" + fecha + "\"") + ","
                + "\"detalleTexto\":"   + (v.getDetalleTexto() == null ? "null" : "\"" + JsonUtil.escapar(v.getDetalleTexto()) + "\"")
                + "}";
    }

    private String serializarLista(List<Venta> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(serializarVenta(lista.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    // ── Helpers ──────────────────────────────────────────────────
    private boolean esAdminOmozo(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return false;
        Usuario u = (Usuario) s.getAttribute("usuario");
        if (u == null) return false;
        String rol = u.getNombreRol() == null ? "" : u.getNombreRol().toLowerCase();
        return rol.equals("admin") || rol.equals("mozo")
                || rol.equals("cocina") || rol.equals("delivery");
    }

    private String coalesce(String valor, String defecto) {
        return (valor != null && !valor.trim().isEmpty()) ? valor.trim() : defecto;
    }
}