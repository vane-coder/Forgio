package com.forgio.service;

import com.forgio.dto.request.NewsFeedRequest;
import com.forgio.dto.response.NewsFeedResponse;
import com.forgio.entity.Factory;
import com.forgio.entity.NewsFeed;
import com.forgio.entity.User;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.FactoryRepository;
import com.forgio.repository.NewsFeedRepository;
import com.forgio.repository.UserRepository;
import com.forgio.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NewsFeedService {

    private final NewsFeedRepository newsFeedRepository;
    private final FactoryRepository factoryRepository;
    private final UserRepository userRepository;

    private UUID currentUserId() {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getUserId();
    }

    @Transactional(readOnly = true)
    public List<NewsFeedResponse> listPosts() {
        UUID factoryId = TenantContext.getFactoryId();
        return newsFeedRepository.findByFactory_FactoryIdOrderByCreatedAtDesc(factoryId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NewsFeedResponse createPost(NewsFeedRequest req) {
        UUID factoryId = TenantContext.getFactoryId();
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        // reload the author fresh from the DB inside this transaction
        User author = userRepository.findById(currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        NewsFeed post = NewsFeed.builder()
                .factory(factory)
                .author(author)
                .content(req.content())
                .build();

        return toResponse(newsFeedRepository.save(post));
    }

    private NewsFeedResponse toResponse(NewsFeed n) {
        User a = n.getAuthor();
        return new NewsFeedResponse(
                n.getPostId(),
                n.getContent(),
                a != null ? a.getUserId() : null,
                a != null ? a.getName()   : null,
                n.getCreatedAt());
    }
}