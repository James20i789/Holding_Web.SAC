package controller;

import Dao.IMPL.ProductoImpl;
import Modelos.Producto;
import Modelos.Usuario;
import Util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

/**
 * GET    /api/productos                  → listar (filtros: ?categoria=, ?nombre=)
 * POST   /api/productos                  → crear  (admin) — soporta multipart para imagen
 * POST   /api/productos?id=              → actualizar (admin) — mismo endpoint con id
 * DELETE /api/productos?id=              → eliminar (admin)
 */
@WebServlet("/api/productos")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,        // 1 MB en memoria
    maxFileSize       = 5 * 1024 * 1024,    // 5 MB por archivo
    maxRequestSize    = 10 * 1024 * 1024    // 10 MB total
)
public class ProductoServlet extends HttpServlet {

    private final ProductoImpl dao = new ProductoImpl();

    /* ============================================================
       GET → listar productos
       ?categoria=id  filtro por categoría
       ?nombre=texto  búsqueda por nombre
       ============================================================ */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

        String catStr  = req.getParameter("categoria");
        String nombre  = req.getParameter("nombre");

        List<Producto> lista;
        if (catStr != null && !catStr.isEmpty() && !"all".equals(catStr)) {
            try {
                lista = dao.listarPorCategoria(Integer.parseInt(catStr));
            } catch (NumberFormatException e) {
                lista = dao.listarTodos();
            }
        } else if (nombre != null && !nombre.isEmpty()) {
            lista = dao.buscarPorNombre(nombre.trim());
        } else {
            lista = dao.listarTodos();
        }

        resp.getWriter().write(serializarLista(lista));
    }

    /* ============================================================
       POST → crear o actualizar producto (admin)
       Si viene el parámetro "id" → actualiza; si no → inserta.
       Acepta multipart (imagenFile) o URL (imagen).
       ============================================================ */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        resp.setContentType("application/json;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");

        if (!esAdmin(req)) {
            resp.setStatus(403);
            resp.getWriter().write(JsonUtil.error("Acceso denegado"));
            return;
        }

        String idStr       = req.getParameter("id");
        String nombre      = req.getParameter("nombre");
        String catStr      = req.getParameter("idCategoria");
        String precioStr   = req.getParameter("precio");
        String stockStr    = req.getParameter("stock");
        String descripcion = req.getParameter("descripcion");
        String imagenURL   = req.getParameter("imagen");

        // Validaciones básicas
        if (nombre == null || nombre.trim().isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error("El nombre es obligatorio"));
            return;
        }
        if (catStr == null || catStr.isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error("La categoría es obligatoria"));
            return;
        }
        double precio;
        try {
            precio = Double.parseDouble(precioStr);
            if (precio < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error("Precio inválido"));
            return;
        }

        int stock = 0;
        try { stock = Integer.parseInt(stockStr); } catch (Exception ignored) {}

        // Manejar imagen subida
        String imagenFinal = (imagenURL != null && !imagenURL.trim().isEmpty())
                ? imagenURL.trim()
                : null;

        Part filePart = null;
        try { filePart = req.getPart("imagenFile"); } catch (Exception ignored) {}

        if (filePart != null && filePart.getSize() > 0) {
            imagenFinal = guardarImagen(filePart, req);
        }

        Producto prod = new Producto(
                nombre.trim(),
                descripcion != null ? descripcion.trim() : "",
                precio,
                stock,
                imagenFinal,
                Integer.parseInt(catStr)
        );

        boolean ok;
        if (idStr != null && !idStr.trim().isEmpty()) {
            // ACTUALIZAR
            try {
                prod.setIdProducto(Integer.parseInt(idStr.trim()));
                ok = dao.actualizar(prod);
            } catch (NumberFormatException e) {
                resp.setStatus(400);
                resp.getWriter().write(JsonUtil.error("ID inválido"));
                return;
            }
            resp.getWriter().write(ok
                    ? JsonUtil.ok("Producto actualizado")
                    : JsonUtil.error("No se pudo actualizar el producto"));
        } else {
            // INSERTAR
            ok = dao.insertar(prod);
            resp.getWriter().write(ok
                    ? JsonUtil.ok("Producto creado")
                    : JsonUtil.error("No se pudo crear el producto"));
        }
    }

    /* ============================================================
       DELETE → eliminar producto (admin)
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
                    ? JsonUtil.ok("Producto eliminado")
                    : JsonUtil.error("No se pudo eliminar (tiene ventas asociadas?)"));
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error("ID inválido"));
        }
    }

    /* ============================================================
       Guardar imagen en disco  →  /Web Pages/img/productos/
       Devuelve la ruta relativa para guardar en BD.
       ============================================================ */
    private String guardarImagen(Part part, HttpServletRequest req) {
        try {
            String ext       = obtenerExtension(part);
            String fileName  = UUID.randomUUID().toString() + "." + ext;
            String uploadDir = req.getServletContext()
                    .getRealPath("/img/productos");

            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            try (InputStream is = part.getInputStream()) {
                Files.copy(is, Paths.get(uploadDir, fileName),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            return req.getContextPath() + "/img/productos/" + fileName;
        } catch (Exception e) {
            System.err.println("Error guardando imagen: " + e.getMessage());
            return null;
        }
    }

    private String obtenerExtension(Part part) {
        String header = part.getHeader("content-disposition");
        if (header != null) {
            for (String token : header.split(";")) {
                if (token.trim().startsWith("filename")) {
                    String nombre = token.substring(token.indexOf("=") + 1).trim()
                            .replace("\"", "");
                    int dot = nombre.lastIndexOf('.');
                    if (dot >= 0) return nombre.substring(dot + 1).toLowerCase();
                }
            }
        }
        return "jpg";
    }

    /* ============================================================
       Serializar lista → JSON camelCase compatible con los JS
       ============================================================ */
    private String serializarLista(List<Producto> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            Producto p = lista.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"idProducto\":").append(p.getIdProducto()).append(",")
              .append("\"nombre\":\"").append(JsonUtil.escapar(p.getNombre())).append("\",")
              .append("\"descripcion\":\"").append(JsonUtil.escapar(p.getDescripcion())).append("\",")
              .append("\"precio\":").append(p.getPrecio()).append(",")
              .append("\"stock\":").append(p.getStock()).append(",")
              .append("\"imagen\":").append(p.getImagen() == null ? "null"
                      : "\"" + JsonUtil.escapar(p.getImagen()) + "\"").append(",")
              .append("\"idCategoria\":").append(p.getIdCategoria()).append(",")
              .append("\"nombreCategoria\":\"").append(JsonUtil.escapar(p.getNombreCategoria())).append("\",")
              .append("\"colorCategoria\":\"").append(JsonUtil.escapar(p.getColorCategoria())).append("\",")
              .append("\"estado\":").append(p.isEstado())
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    // ── Helper ───────────────────────────────────────────────────
    private boolean esAdmin(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return false;
        Usuario u = (Usuario) s.getAttribute("usuario");
        return u != null && "admin".equalsIgnoreCase(u.getNombreRol());
    }
}