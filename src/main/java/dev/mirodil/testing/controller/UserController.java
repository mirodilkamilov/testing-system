package dev.mirodil.testing.controller;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/users")
public class UserController extends HttpServlet {
//    private UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        List<User> users = userService.getAllUsers();
        List<String> users = new ArrayList<>();
        users.add("John Doe 1");
        users.add("John Doe 2");
        users.add("John Doe 3");
//        response.getWriter().write(new ObjectMapper().writeValueAsString(users));
        response.getWriter().write(new Gson().toJson(users));
    }
}