package com.example.sbfacebookauth.controller;


import com.example.sbfacebookauth.common.UserConstants;
import com.example.sbfacebookauth.entity.Post;
import com.example.sbfacebookauth.entity.PostStatus;
import com.example.sbfacebookauth.entity.User;
import com.example.sbfacebookauth.repository.PostRepository;
import com.example.sbfacebookauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository repository;

    @PostMapping("/create")
    public String createPost(@RequestBody Post post, Principal principal) {
        List<String> activeRoles = getRolesByLoggedInUser(principal);
        if(activeRoles.contains("ROLE_ADMIN") || activeRoles.contains("ROLE_MODERATOR")){
            post.setStatus(PostStatus.APPROVED);
            post.setUserName(principal.getName());
            postRepository.save(post);
            return principal.getName() + " Your post as ADMIN/MODERATOR was published!";
        }else{
            post.setStatus(PostStatus.PENDING);
            post.setUserName(principal.getName());
            postRepository.save(post);
            return principal.getName() + " Your post published successfully,Required ADMIN/MODERATOR Action!";
        }
    }

    @GetMapping("/approvePost/{postId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MODERATOR')")
    public String approvePost(@PathVariable int postId) {
        Post post = postRepository.findById(postId).get();
        post.setStatus(PostStatus.APPROVED);
        postRepository.save(post);
        return "Post Approved, It will now be visible to all users!!";
    }

    @GetMapping("/approveAll")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MODERATOR')")
    public String approveAll() {
        postRepository.findAll().stream().filter(post -> post.getStatus().equals(PostStatus.PENDING)).forEach(post -> {
            post.setStatus(PostStatus.APPROVED);
            postRepository.save(post);
        });
        return "Approved all pending posts!!";
    }

    @GetMapping("/rejectPost/{postId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MODERATOR')")
    public String rejectPost(@PathVariable int postId) {
        Post post = postRepository.findById(postId).get();
        post.setStatus(PostStatus.REJECTED);
        postRepository.save(post);
        return "Post Rejected by ADMIN, It won't be visible!!";
    }

    @GetMapping("/rejectAll")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MODERATOR')")
    public String rejectAll() {
        postRepository.findAll().stream().filter(post -> post.getStatus().equals(PostStatus.PENDING)).forEach(post -> {
            post.setStatus(PostStatus.REJECTED);
            postRepository.save(post);
        });
        return "All Pending posts REJECTED by ADMIN!!";
    }

    @GetMapping("/viewAll")
    public List<Post> viewAllApprovedPosts() {
        return postRepository.findAll()
                .stream()
                .filter(post -> post.getStatus().equals(PostStatus.APPROVED))
                .collect(Collectors.toList());
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
