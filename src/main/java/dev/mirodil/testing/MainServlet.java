package dev.mirodil.testing;

import dev.mirodil.testing.controller.UserController;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


public class MainServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException, ServletException {
        // Simplified request routing
        if (req.getRequestURI().startsWith("/users")) {
            new UserController().service(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found");
        }
    }
}
