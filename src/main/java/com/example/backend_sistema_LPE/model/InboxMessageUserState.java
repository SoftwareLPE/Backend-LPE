package com.example.backend_sistema_LPE.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "inbox_message_user_state",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "message_id"})
        },
        indexes = {
                @Index(name = "idx_inbox_state_user", columnList = "user_id"),
                @Index(name = "idx_inbox_state_message", columnList = "message_id")
        }
)
public class InboxMessageUserState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "message_id", nullable = false, length = 120)
    private String messageId;

    @Column(name = "first_seen_version")
    private LocalDateTime firstSeenVersion;

    @Column(name = "last_seen_version")
    private LocalDateTime lastSeenVersion;

    @Column(name = "opened_version")
    private LocalDateTime openedVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
