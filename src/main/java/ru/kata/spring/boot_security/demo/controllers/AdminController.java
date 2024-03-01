package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.AdminService;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.until.RoleValidator;
import ru.kata.spring.boot_security.demo.until.UserValidator;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final RoleService roleService;
    private final UserValidator userValidator;
    private final RoleValidator roleValidator;


    @Autowired
    public AdminController(AdminService adminService, RoleService roleService, UserValidator userValidator, RoleValidator roleValidator) {
        this.adminService = adminService;
        this.roleService = roleService;
        this.userValidator = userValidator;
        this.roleValidator = roleValidator;
    }


    @GetMapping("/user")
    public String showAdminInfo(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        return "admin/adminPage";
    }


    @GetMapping("/users")
    public String getAllUsers(Model model) {
        List<User> userList = adminService.getAllUsers();
        model.addAttribute("userList", userList);
        return "admin/users";
    }

    @GetMapping("admin/removeUser")
    public String removeUser(@RequestParam("id") Long id) {
        adminService.removeUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/new")
    public String getAddUserForm(@ModelAttribute("user") User user) {return "admin/userAdd";}



    @PostMapping("/addUser")
    public String postAddUserForm(@ModelAttribute("user") @Valid User user,
                                   BindingResult userBindingResult,
                                   @RequestParam(value = "roles", required = false) @Valid List<String> roles,
                                   BindingResult rolesBindingResult,
                                   RedirectAttributes redirectAttributes) {

        userValidator.validate(user, userBindingResult);
        if (userBindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorsUser", userBindingResult.getAllErrors());
            return "/admin/userAdd";
        }
        roleValidator.validate(roles, rolesBindingResult);
        if (rolesBindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorsRoles", rolesBindingResult.getAllErrors());
            return "/admin/userAdd";
        }
        adminService.addUser(user, roles);
        return "redirect:/admin/users";
    }



    @GetMapping("/admin/updateUser")
    public String getEditUserForm(Model model, @RequestParam("id") Long id) {
        model.addAttribute("user", adminService.findOneById(id));
        model.addAttribute("roles", roleService.getRoles());
        return "admin/userUpdate";
    }

    @GetMapping("admin/showUser")
    public String showUserInfo(Model model, @RequestParam("id") Long id) {
        model.addAttribute("user", adminService.findOneById(id));
        model.addAttribute("roles", roleService.getRoles());
        return "admin/userInfo";
    }


    @PostMapping("/updateUser")
    public String postEditUserForm(@ModelAttribute("user") @Valid User user,
                                   @RequestParam(value = "roles", required = false) @Valid List<String> roles,
                                   BindingResult rolesBindingResult,
                                   RedirectAttributes redirectAttributes) {

        roleValidator.validate(roles, rolesBindingResult);
        if (rolesBindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorsRoles", rolesBindingResult.getAllErrors());
            return "/admin/userUpdate";
        }

        adminService.updateUser(user, roles);
        return "redirect:/admin/users";
    }

}
