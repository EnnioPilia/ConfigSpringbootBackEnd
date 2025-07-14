package com.example.configbackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.configbackend.model.Object;

public interface ObjectRepository extends JpaRepository<Object, Long> {
    List<Object> findByUserId(Long userId);
}
