package controller;

import Dao.IMPL.CategoriaImpl;
import Modelos.Categoria;
import Modelos.Usuario;
import Util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;


/**
 * GET    /api/categorias          → listar todas activas
 * POST   /api/categorias          → insertar (admin)
 * PUT    /api/categorias?id=      → actualizar (admin)
 * DELETE /api/categorias?id=      → eliminar (admin)
 */
@WebServlet("/api/categorias")
public class CategoriaServlet extends HttpServlet {

    private final CategoriaImpl dao = new CategoriaImpl();

    /* ============================================================
       GET → listar todas (disponible para cualquier sesión válida)
       ============================================================ */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

        List<Categoria> lista = dao.listarTodas();
        // Serializar manualmente para controlar los nombres en camelCase
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            Categoria c = lista.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"idCategoria\":").append(c.getIdCategoria()).append(",")
              .append("\"nombre\":\"").append(JsonUtil.escapar(c.getNombre())).append("\",")
              .append("\"color\":\"").append(JsonUtil.escapar(c.getColor())).append("\",")
              .append("\"estado\":").append(c.isEstado())
              .append("}");
        }
        sb.append("]");
        resp.getWriter().write(sb.toString());
    }

    /* ============================================================
       POST → insertar nueva categoría (admin)
       ============================================================ */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");

        if (!esAdmin(req)) {
            resp.setStatus(403);
            resp.getWriter().write(JsonUtil.error("Acceso denegado"));
            return;
        }

        String nombre = req.getParameter("nombre");
        String color  = req.getParameter("color");

        if (nombre == null || nombre.trim().isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error("El nombre es obligatorio"));
            return;
        }

        Categoria cat = new Categoria(
                nombre.trim(),
                (color != null && !color.isEmpty()) ? color : "#e67e22"
        );

        boolean ok = dao.insertar(cat);
        resp.getWriter().write(ok
                ? JsonUtil.ok("Categoría creada")
                : JsonUtil.error("No se pudo crear la categoría (nombre duplicado?)"));
    }

    /* ============================================================
       PUT → actualizar categoría (admin)
       Body: nombre=&color=   param: ?id=
       ============================================================ */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");

        if (!esAdmin(req)) {
            resp.setStatus(403);
            resp.getWriter().write(JsonUtil.error("Acceso denegado"));
            return;
        }

        String idStr  = req.getParameter("id");
        String nombre = req.getParameter("nombre");
        String color  = req.getParameter("color");

        if (idStr == null || nombre == null || nombre.trim().isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error("Datos incompletos"));
            return;
        }

        try {
            Categoria cat = new Categoria(nombre.trim(),
                    (color != null && !color.isEmpty()) ? color : "#e67e22");
            cat.setIdCategoria(Integer.parseInt(idStr));

            boolean ok = dao.actualizar(cat);
            resp.getWriter().write(ok
                    ? JsonUtil.ok("Categoría actualizada")
                    : JsonUtil.error("No se pudo actualizar"));
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error("ID inválido"));
        }
    }

    /* ============================================================
       DELETE → eliminar categoría (admin)
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
            boolean ok = dao.eliminar(Integer.parseInt(idStr));
            resp.getWriter().write(ok
                    ? JsonUtil.ok("Categoría eliminada")
                    : JsonUtil.error("No se pudo eliminar"));
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error("ID inválido"));
        }
    }

    // ── Helper ───────────────────────────────────────────────────
    private boolean esAdmin(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return false;
        Usuario u = (Usuario) s.getAttribute("usuario");
        return u != null && "admin".equalsIgnoreCase(u.getNombreRol());
    }
}