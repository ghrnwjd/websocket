package com.ghrnwjd.chat.repository;

import com.ghrnwjd.chat.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Integer> {

}
