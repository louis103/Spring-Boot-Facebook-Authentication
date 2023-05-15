package com.example.sbfacebookauth.repository;

import com.example.sbfacebookauth.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post,Integer> {
}
