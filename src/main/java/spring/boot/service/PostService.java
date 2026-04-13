package spring.boot.service;

import spring.boot.entity.Post;
import spring.boot.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    
    @Autowired
    private PostRepository postRepository;
    
    // Create
    public Post createPost(Post post) {
        return postRepository.save(post);
    }
    
    // Read All
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }
    
    // Read By ID
    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }
    
    // Update
    public Post updatePost(Long id, Post postDetails) {
        Optional<Post> post = postRepository.findById(id);
        if (post.isPresent()) {
            Post existingPost = post.get();
            if (postDetails.getTitle() != null) {
                existingPost.setTitle(postDetails.getTitle());
            }
            if (postDetails.getContent() != null) {
                existingPost.setContent(postDetails.getContent());
            }
            if (postDetails.getAuthorName() != null) {
                existingPost.setAuthorName(postDetails.getAuthorName());
            }
            return postRepository.save(existingPost);
        }
        return null;
    }
    
    // Delete
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
    
    // Search by title
    public List<Post> searchByTitle(String title) {
        return postRepository.findByTitleContainingIgnoreCase(title);
    }
    
    // Search by author
    public List<Post> searchByAuthor(String authorName) {
        return postRepository.findByAuthorNameContainingIgnoreCase(authorName);
    }
}

