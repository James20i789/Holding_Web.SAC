package controller;

import Util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;


/**
 * GET /api/logout
 * Invalida la sesion y responde ok.
 */
@WebServlet("/api/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

        HttpSession sesion = req.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }

        resp.getWriter().write(JsonUtil.ok("Sesión cerrada"));
    }
}