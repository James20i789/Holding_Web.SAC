package controller;

import Dao.IMPL.UsuarioImpl;
import Modelos.Usuario;
import Util.JsonUtil;
import Util.PasswordUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;


/**
 * POST /api/login
 * Body: identificador=...&password=...
 * Respuesta: { ok, nombre, rol, mensaje }
 */
@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");

        String identificador = req.getParameter("identificador");
        String password      = req.getParameter("password");

        if (identificador == null || identificador.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error("Datos incompletos"));
            return;
        }

        UsuarioImpl dao = new UsuarioImpl();
        Usuario usuario  = dao.login(identificador.trim(), password.trim());

        if (usuario == null) {
            resp.setStatus(401);
            resp.getWriter().write(JsonUtil.error("Credenciales incorrectas"));
            return;
        }

        if (!usuario.isEstado()) {
            resp.setStatus(403);
            resp.getWriter().write(JsonUtil.error("Cuenta deshabilitada"));
            return;
        }

        // Guardar sesion
        HttpSession sesion = req.getSession(true);
        sesion.setAttribute("usuario", usuario);
        sesion.setMaxInactiveInterval(60 * 60 * 8); // 8 horas

        String json = "{"
                + "\"ok\":true,"
                + "\"nombre\":\"" + JsonUtil.escapar(usuario.getNombre()) + "\","
                + "\"rol\":\""    + JsonUtil.escapar(usuario.getNombreRol()) + "\","
                + "\"id\":"       + usuario.getIdUsuario() + ","
                + "\"mensaje\":\"Bienvenido\""
                + "}";

        resp.getWriter().write(json);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(200);
    }
}