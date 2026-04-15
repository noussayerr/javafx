package javafx.forum1.model;

import java.time.LocalDateTime;

public record Poste(long id, String titre, String description, LocalDateTime dateCreation) {
}

