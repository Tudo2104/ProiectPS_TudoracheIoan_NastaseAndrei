package com.example.demo.repository;

import com.example.demo.entity.Message;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface MessageRepository extends JpaRepository<Message, Long> {
}

