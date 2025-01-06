package dev.mirodil.testing_system;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class HelloWorldController {
    @GetMapping(path = "/hello")
    public List<String> helloWorld() {
        List<String> users = new ArrayList<>();
        users.add("John Doe 1");
        users.add("John Doe 2");
        users.add("John Doe 3");
        return users;
    }
}
