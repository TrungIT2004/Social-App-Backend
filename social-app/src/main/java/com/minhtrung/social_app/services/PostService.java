package com.minhtrung.social_app.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.minhtrung.social_app.dtos.CreatePostRequest;
import com.minhtrung.social_app.dtos.PostDataResponse;
import com.minhtrung.social_app.enums.PostErrorCode;
import com.minhtrung.social_app.exceptions.PostException;
import com.minhtrung.social_app.models.Hashtag;
import com.minhtrung.social_app.models.Post;
import com.minhtrung.social_app.models.User;
import com.minhtrung.social_app.repositories.HashtagRepository;
import com.minhtrung.social_app.repositories.PostRepository;
import com.minhtrung.social_app.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final HashtagRepository hashtagRepository;
    private final FileStorageService fileStorageService;

    PostService(PostRepository postRepository, UserRepository userRepository, FileStorageService fileStorageService, HashtagRepository hashtagRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.hashtagRepository = hashtagRepository;
    }

    public List<PostDataResponse> retrievePosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> data = postRepository.getPosts(pageable);

        return data.stream()
                .map(row -> {
                    Post post = (Post) row[0];
                    String firstName = (String) row[1];
                    String lastName = (String) row[2];
                    String fullname = firstName + " " + lastName;
                    String profilePicUrl = (String) row[3];
                    return new PostDataResponse(post, fullname, profilePicUrl);
                })
                .collect(Collectors.toList());
    }
    
    public List<PostDataResponse> retrieveAllPostsFromOneHashtag(int page, int size, String hashtagName) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> data = postRepository.getPostsFromHashtag(pageable, hashtagName);

        List<PostDataResponse> posts = data.stream().map(row -> {
            Post p = (Post) row[0];
            String fullname = (String) row[1] + " " + row[2];
            String profilePicUrl = (String) row[3];
            return new PostDataResponse(p, fullname, profilePicUrl);
        })
                .collect(Collectors.toList());

        return posts;
    }
    
    // Check thêm phần check nội dung trống như comment
    public Post insertPost(CreatePostRequest post, UUID userId, List<MultipartFile> files) {
        Post newPost = new Post();
        User userProxy = userRepository.getReferenceById(userId);
        Set<Hashtag> hashtags = new HashSet<>();
        newPost.setUser(userProxy);
        newPost.setText(post.getText());
        newPost.setVisibility(post.getVisibility());

        List<String> wordsList = Arrays.asList(post.getText().split(" "));
        Set<String> wordsSet = new HashSet<>(wordsList);       
        
        for (String word : wordsSet) {
            if (word.startsWith("#")) {
                String hashtagName = word.substring(1).toLowerCase();

                Hashtag hashtag = hashtagRepository.findByHashtagName(hashtagName).orElseGet(() -> {
                    Hashtag newHashtag = new Hashtag();
                    newHashtag.setHashtagName(hashtagName);
                    return hashtagRepository.save(newHashtag);
                });

                hashtags.add(hashtag);
            }
        }
        
        newPost.setHashtags(hashtags);

        List<String> mediaUrls = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                    String filePath = fileStorageService.storeFile(file, "posts");
                    mediaUrls.add(filePath);
                } catch (IOException ex) {
                    log.error("Failed to store file", ex);
                    throw new RuntimeException("Failed to upload media", ex);
                }
            }

            newPost.setMediaUrls(mediaUrls);
        }
                
        Post createdPost = postRepository.save(newPost);
        log.info("User {} is creating new post", userId);
        log.debug("A new post is saved in DB: {}", newPost);
        return createdPost;
    }
    
    @Transactional
    public void removePost(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> {
            log.warn("Post {} isn't found", postId);
            throw new PostException(PostErrorCode.POST_NOT_FOUND, "Post isnt found");
        });

        if (!post.getUserId().equals(userId)) {
            log.warn("Post {} doesn't belong to user {}", postId, userId);
            throw new PostException(PostErrorCode.POST_NOT_OWNED, "Post doesn't belong to user");
        }

        postRepository.deleteById(postId);
        log.info("User {} deleted post {}", userId, postId);
    }
}

