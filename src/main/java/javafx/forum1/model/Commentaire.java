package javafx.forum1.model;

import java.time.LocalDateTime;

public record Commentaire(long id, long posteId, String contenu, LocalDateTime createdAt) {
}

