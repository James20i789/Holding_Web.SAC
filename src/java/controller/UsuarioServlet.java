package controller;

import Dao.IMPL.UsuarioImpl;
import Modelos.Usuario;
import Util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
;

/**
 * /api/usuarios          → GET  listar todos (admin)
 * /api/usuarios/registrar → POST registrar nuevo cliente
 * /api/usuarios/check    → POST verificar si correo existe
 */
@WebServlet(urlPatterns = {"/api/usuarios", "/api/usuarios/registrar", "/api/usuarios/check"})
public class UsuarioServlet extends HttpServlet {

    private final UsuarioImpl dao = new UsuarioImpl();

    /* ============================================================
       GET /api/usuarios  → lista (solo admin)
       ============================================================ */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

        if (!esAdmin(req)) {
            resp.setStatus(403);
            resp.getWriter().write(JsonUtil.error("Acceso denegado"));
            return;
        }

        String rolFiltro = req.getParameter("rol");
        List<Usuario> lista = (rolFiltro != null && !rolFiltro.isEmpty())
                ? dao.listarPorRol(rolFiltro)
                : dao.listarTodos();

        // Ocultar passwords antes de serializar
        lista.forEach(u -> u.setPassword(null));
        resp.getWriter().write(JsonUtil.toJson(lista));
    }

    /* ============================================================
       POST /api/usuarios/registrar  → registro cliente
       POST /api/usuarios/check      → verificar correo
       ============================================================ */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");

        String path = req.getServletPath();

        // ── Verificar correo ──────────────────────────────────────
        if (path.endsWith("/check")) {
            String correo = req.getParameter("correo");
            if (correo == null || correo.trim().isEmpty()) {
                resp.getWriter().write("{\"existe\":false}");
                return;
            }
            boolean existe = dao.correoExiste(correo.trim().toLowerCase());
            resp.getWriter().write("{\"existe\":" + existe + "}");
            return;
        }

        // ── Registro ─────────────────────────────────────────────
        String nombre   = req.getParameter("nombre");
        String correo   = req.getParameter("correo");
        String password = req.getParameter("password");
        String direccion= req.getParameter("direccion");

        if (nombre == null || correo == null || password == null
                || nombre.trim().isEmpty() || correo.trim().isEmpty()
                || password.trim().isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error("Datos incompletos"));
            return;
        }

        if (dao.correoExiste(correo.trim().toLowerCase())) {
            resp.setStatus(409);
            resp.getWriter().write(JsonUtil.error("El correo ya está registrado"));
            return;
        }

        // Rol 5 = cliente (según datos base)
        Usuario nuevo = new Usuario(
                nombre.trim(),
                correo.trim().toLowerCase(),
                password,           // UsuarioImpl.registrar hace el hash
                direccion != null ? direccion.trim() : "",
                5
        );

        boolean ok = dao.registrar(nuevo);
        if (ok) {
            resp.getWriter().write(JsonUtil.ok("Registro exitoso"));
        } else {
            resp.setStatus(500);
            resp.getWriter().write(JsonUtil.error("No se pudo registrar el usuario"));
        }
    }

    /* ============================================================
       DELETE /api/usuarios?id=  → eliminar (admin)
       ============================================================ */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

        if (!esAdmin(req)) {
            resp.setStatus(403);
            resp.getWriter().write(JsonUtil.error("Acceso denegado"));
            return;
        }

        String idStr = req.getParameter("id");
        if (idStr == null) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error("ID requerido"));
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            boolean ok = dao.eliminar(id);
            resp.getWriter().write(ok
                    ? JsonUtil.ok("Usuario eliminado")
                    : JsonUtil.error("No se pudo eliminar (es usuario de sistema o no existe)"));
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error("ID inválido"));
        }
    }

    // ── Helper sesión ─────────────────────────────────────────────
    private boolean esAdmin(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return false;
        Usuario u = (Usuario) s.getAttribute("usuario");
        return u != null && "admin".equalsIgnoreCase(u.getNombreRol());
    }
}