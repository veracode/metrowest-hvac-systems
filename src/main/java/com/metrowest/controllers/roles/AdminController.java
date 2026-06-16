package com.metrowest.controllers.roles;

import com.metrowest.entity.Role;
import com.metrowest.entity.User;
import com.metrowest.repo.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminController(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String root(Model model)
    {
        var users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/dashboard";
    }

    @PostMapping("/new_user")
    public String new_user(Model model,
                           @RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("role") String role_string)
    {
        var exists = userRepository.existsByUsername(username);
        var role = Role.from_string(role_string);
        if (exists)
        {
            model.addAttribute("error", "user: " + username + " already exists");
            return "admin/failure";
        }
        if (password == null || password.isEmpty())
        {
            model.addAttribute("error", "password is null or empty");
            return "admin/failure";
        }
        if (password.length() < 8)
        {
            model.addAttribute("error", "password is too short, must be at least 8 characters");
            return "admin/failure";
        }
        if (role == null)
        {
            model.addAttribute("error", role_string + " is invalid");
            return "admin/failure";
        }

        var user = new User();
        user.setUsername(username);
        user.setRole(role);
        user.setPassword_hash(passwordEncoder.encode(password));
        var saved = userRepository.save(user);
        userRepository.flush();

        model.addAttribute("user", "id="+ saved.getId() + " name=" + username);
        return "admin/success";
    }
}
