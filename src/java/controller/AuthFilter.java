package controller;

import Modelos.Usuario;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Filtro de seguridad: si no hay sesión activa redirige al login.
 * Las rutas /api/* retornan JSON 401 en lugar de redirigir.
 */
@WebFilter(urlPatterns = {"/pages/*", "/api/*"})
public class AuthFilter implements Filter {

    // Rutas que NO requieren autenticación
    private static final String[] PUBLICAS = {
        "/pages/index.html",
        "/pages/FormularioRegistro.html",
        "/api/login",
        "/api/usuarios/registrar",
        "/api/usuarios/check",
        "/api/categorias",   // clientes pueden ver categorías
        "/api/productos"     // clientes pueden ver productos
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getServletPath()
                + (req.getPathInfo() != null ? req.getPathInfo() : "");

        // Permitir rutas públicas
        for (String pub : PUBLICAS) {
            if (path.startsWith(pub)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // Verificar sesión
        HttpSession session = req.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuario") : null;

        if (usuario == null) {
            if (path.startsWith("/api/")) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.setStatus(401);
                resp.getWriter().print("{\"ok\":false,\"mensaje\":\"No autenticado\","
                        + "\"redirect\":\"../pages/index.html\"}");
            } else {
                resp.sendRedirect(req.getContextPath() + "/pages/index.html");
            }
            return;
        }

        // Verificar permisos por rol para rutas de API sensibles
        if (path.startsWith("/api/ventas") && "POST".equals(req.getMethod())) {
            // Solo mozo, admin o cliente autenticado puede registrar ventas
            String rol = usuario.getNombreRol().toLowerCase();
            if (!rol.equals("admin") && !rol.equals("mozo") && !rol.equals("cliente")) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.setStatus(403);
                resp.getWriter().print("{\"ok\":false,\"mensaje\":\"Sin permisos\"}");
                return;
            }
        }
        System.out.println("PATH: " + path);
        chain.doFilter(request, response);
    }
}