package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.InboxMessageUserState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface InboxMessageUserStateRepository extends JpaRepository<InboxMessageUserState, Long> {
    Optional<InboxMessageUserState> findByUserUserIdAndMessageId(Long userId, String messageId);

    List<InboxMessageUserState> findByUserUserIdAndMessageIdIn(Long userId, Collection<String> messageIds);
}
