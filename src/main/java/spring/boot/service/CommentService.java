package spring.boot.service;

import spring.boot.entity.Comment;
import spring.boot.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    // Create
    public Comment createComment(Comment comment) {
        return commentRepository.save(comment);
    }
    
    // Read All
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }
    
    // Read By ID
    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }
    
    // Read by Post ID
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }
    
    // Update
    public Comment updateComment(Long id, Comment commentDetails) {
        Optional<Comment> comment = commentRepository.findById(id);
        if (comment.isPresent()) {
            Comment existingComment = comment.get();
            if (commentDetails.getContent() != null) {
                existingComment.setContent(commentDetails.getContent());
            }
            if (commentDetails.getAuthorName() != null) {
                existingComment.setAuthorName(commentDetails.getAuthorName());
            }
            return commentRepository.save(existingComment);
        }
        return null;
    }
    
    // Delete
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
    
    // Delete all comments for a post
    public void deleteCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        commentRepository.deleteAll(comments);
    }
    
    // Search by author
    public List<Comment> searchByAuthor(String authorName) {
        return commentRepository.findByAuthorNameContainingIgnoreCase(authorName);
    }
}

