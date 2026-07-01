package controller;

import Modelos.Usuario;
import Util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;


/**
 * GET /api/sesion
 * Devuelve datos del usuario en sesion o { ok: false }.
 * Usado por todos los JS para verificar autenticacion.
 */
@WebServlet("/api/sesion")
public class SesionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

        HttpSession sesion  = req.getSession(false);
        Usuario     usuario = (sesion != null)
                ? (Usuario) sesion.getAttribute("usuario")
                : null;

        if (usuario == null) {
            resp.setStatus(401);
            resp.getWriter().write(JsonUtil.error("No hay sesión activa"));
            return;
        }

        String json = "{"
                + "\"ok\":true,"
                + "\"id\":"       + usuario.getIdUsuario() + ","
                + "\"nombre\":\"" + JsonUtil.escapar(usuario.getNombre()) + "\","
                + "\"correo\":\"" + JsonUtil.escapar(usuario.getCorreo())  + "\","
                + "\"rol\":\""    + JsonUtil.escapar(usuario.getNombreRol()) + "\","
                + "\"esSistema\":" + usuario.isEsSistema()
                + "}";

        resp.getWriter().write(json);
    }
}