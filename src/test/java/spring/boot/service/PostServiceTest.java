package spring.boot.service;

import spring.boot.entity.Post;
import spring.boot.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    
    @Mock
    private PostRepository postRepository;
    
    @InjectMocks
    private PostService postService;
    
    private Post testPost;
    
    @BeforeEach
    void setUp() {
        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setContent("Test Content");
        testPost.setAuthorName("Test Author");
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());
    }
    
    @Test
    void testCreatePost() {
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        
        Post result = postService.createPost(testPost);
        
        assertNotNull(result);
        assertEquals("Test Post", result.getTitle());
        verify(postRepository, times(1)).save(testPost);
    }
    
    @Test
    void testGetAllPosts() {
        List<Post> posts = new ArrayList<>();
        posts.add(testPost);
        when(postRepository.findAll()).thenReturn(posts);
        
        List<Post> result = postService.getAllPosts();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(postRepository, times(1)).findAll();
    }
    
    @Test
    void testGetPostById() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        
        Optional<Post> result = postService.getPostById(1L);
        
        assertTrue(result.isPresent());
        assertEquals("Test Post", result.get().getTitle());
        verify(postRepository, times(1)).findById(1L);
    }
    
    @Test
    void testDeletePost() {
        doNothing().when(postRepository).deleteById(1L);
        
        postService.deletePost(1L);
        
        verify(postRepository, times(1)).deleteById(1L);
    }
}

