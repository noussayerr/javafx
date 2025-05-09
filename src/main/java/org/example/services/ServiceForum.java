package org.example.services;

import org.example.entity.Forum;
import org.example.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceForum {

    private Connection connection;

    public ServiceForum() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    public void ajouter(Forum forum) throws SQLException {
        String query = "INSERT INTO forum (name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, forum.getName());
            statement.executeUpdate();
        }
    }

    public List<Forum> afficher() throws SQLException {
        List<Forum> forums = new ArrayList<>();
        String query = "SELECT id, name FROM forum";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Forum forum = new Forum();
                forum.setId(resultSet.getInt("id"));
                forum.setName(resultSet.getString("name"));
                forums.add(forum);
            }
        }
        return forums;
    }

    public void supprimer(int forumId) throws SQLException {
        String query = "DELETE FROM forum WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, forumId);
            statement.executeUpdate();
        }
    }

    public void modifier(Forum forum) throws SQLException {
        String query = "UPDATE forum SET name = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, forum.getName());
            stmt.setInt(2, forum.getId());
            stmt.executeUpdate();
        }
    }
}