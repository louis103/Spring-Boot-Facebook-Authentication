package com.example.sbfacebookauth.controller;

import com.example.sbfacebookauth.common.UserConstants;
import com.example.sbfacebookauth.entity.User;
import com.example.sbfacebookauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository repository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/join")
    public String joinGroup(@RequestBody User user){
        user.setRoles(UserConstants.DEFAULT_ROLE);
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        repository.save(user);
        return "Hi "+user.getUserName()+" Welcome to this group!";
    }

    //if logged in user is ADMIN->GIVE ACCESS to ADMIN/MODERATOR
    //if logged in user is MODERATOR->GIVE ACCESS to MODERATOR

    @GetMapping("/access/{userId}/{userRole}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MODERATOR')")
    public String giveAccessToUser(@PathVariable int userId, @PathVariable String userRole, Principal principal){
        User user = repository.findById(userId).get();
        List<String> activeRoles = getRolesByLoggedInUser(principal);
        String newRole = "";
        if(activeRoles.contains(userRole)){
            newRole = user.getRoles()+","+ userRole;
            user.setRoles(newRole);
        }
        repository.save(user);
        return "Hi "+user.getUserName()+" New Role Assigned to you by "+principal.getName();
    }

    @GetMapping("/allusers")
    @Secured("ROLE_ADMIN")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<User> loadUsers(){
        return repository.findAll();
    }

    @GetMapping("/test")
    @Secured("ROLE_USER")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String testUserAccess(){
        return "User can only access this";
    }

    private List<String> getRolesByLoggedInUser(Principal principal){
        String roles = getLoggedInUser(principal).getRoles();
        List<String> assignedRoles = Arrays.stream(roles.split(",")).collect(Collectors.toList());
        if(assignedRoles.contains("ROLE_ADMIN")){
            return Arrays.stream(UserConstants.ADMIN_ACCESS).collect(Collectors.toList());
        }
        if(assignedRoles.contains("ROLE_MODERATOR")){
            return Arrays.stream(UserConstants.MODERATOR_ACCESS).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private User getLoggedInUser(Principal principal){
        return repository.findByUserName(principal.getName()).get();
    }
}
