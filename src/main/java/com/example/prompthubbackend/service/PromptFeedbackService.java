package com.example.prompthubbackend.service;

import com.example.prompthubbackend.dto.request.CreatePromptCommentRequest;
import com.example.prompthubbackend.dto.request.UpsertPromptReviewRequest;
import com.example.prompthubbackend.dto.response.PromptCommentResponse;
import com.example.prompthubbackend.dto.response.PromptReviewResponse;
import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.PromptComment;
import com.example.prompthubbackend.model.PromptReview;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.enums.PromptStatus;
import com.example.prompthubbackend.model.enums.PromptVisibility;
import com.example.prompthubbackend.repository.PromptCommentRepository;
import com.example.prompthubbackend.repository.PromptRepository;
import com.example.prompthubbackend.repository.PromptReviewRepository;
import com.example.prompthubbackend.repository.UserRepository;
import com.example.prompthubbackend.repository.WorkspaceMemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PromptFeedbackService {
    private final PromptRepository promptRepository;
    private final PromptCommentRepository commentRepository;
    private final PromptReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public PromptFeedbackService(PromptRepository promptRepository,
                                 PromptCommentRepository commentRepository,
                                 PromptReviewRepository reviewRepository,
                                 UserRepository userRepository,
                                 WorkspaceMemberRepository workspaceMemberRepository) {
        this.promptRepository = promptRepository;
        this.commentRepository = commentRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    @Transactional(readOnly = true)
    public List<PromptCommentResponse> getComments(String username, UUID promptId) {
        User user = username == null ? null : requireUser(username);
        Prompt prompt = requireAccessiblePrompt(promptId, user);
        return commentRepository.findByPromptAndDeletedAtIsNullOrderByCreatedAtAsc(prompt).stream()
                .map(comment -> toComment(comment, user))
                .toList();
    }

    @Transactional
    public PromptCommentResponse addComment(String username, UUID promptId, CreatePromptCommentRequest request) {
        User user = requireUser(username);
        Prompt prompt = requireAccessiblePrompt(promptId, user);
        PromptComment parent = null;
        if (request.parentId() != null) {
            parent = commentRepository.findById(request.parentId())
                    .filter(comment -> comment.getDeletedAt() == null && comment.getPrompt().getId().equals(prompt.getId()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent comment not found"));
        }
        OffsetDateTime now = OffsetDateTime.now();
        PromptComment comment = commentRepository.save(PromptComment.builder()
                .prompt(prompt).user(user).parent(parent).content(request.content().trim())
                .isEdited(false).createdAt(now).updatedAt(now).build());
        return toComment(comment, user);
    }

    @Transactional(readOnly = true)
    public PromptReviewResponse getReviews(String username, UUID promptId) {
        User user = username == null ? null : requireUser(username);
        Prompt prompt = requireAccessiblePrompt(promptId, user);
        return reviewResponse(reviewRepository.findByPromptOrderByCreatedAtDesc(prompt), user);
    }

    @Transactional
    public PromptReviewResponse upsertReview(String username, UUID promptId, UpsertPromptReviewRequest request) {
        User user = requireUser(username);
        Prompt prompt = requireAccessiblePrompt(promptId, user);
        OffsetDateTime now = OffsetDateTime.now();
        PromptReview review = reviewRepository.findByPromptAndUser(prompt, user).orElse(null);
        if (review == null) {
            review = PromptReview.builder().prompt(prompt).user(user).createdAt(now).isEdited(false).build();
        } else {
            review.setIsEdited(true);
        }
        review.setRating((short) request.rating());
        review.setContent(blankToNull(request.content()));
        review.setUpdatedAt(now);
        reviewRepository.save(review);
        return reviewResponse(reviewRepository.findByPromptOrderByCreatedAtDesc(prompt), user);
    }

    private PromptReviewResponse reviewResponse(List<PromptReview> reviews, User currentUser) {
        List<PromptReviewResponse.Review> items = reviews.stream()
                .map(review -> toReview(review, currentUser))
                .toList();
        double average = reviews.stream().mapToInt(review -> review.getRating().intValue()).average().orElse(0);
        PromptReviewResponse.Review mine = currentUser == null ? null
                : items.stream().filter(PromptReviewResponse.Review::mine).findFirst().orElse(null);
        return new PromptReviewResponse(Math.round(average * 10.0) / 10.0, items.size(), mine, items);
    }

    private PromptCommentResponse toComment(PromptComment comment, User currentUser) {
        User author = comment.getUser();
        return new PromptCommentResponse(comment.getId(), comment.getParent() == null ? null : comment.getParent().getId(),
                author.getId(), displayName(author), author.getAvatarUrl(), comment.getContent(),
                Boolean.TRUE.equals(comment.getIsEdited()), currentUser != null && author.getId().equals(currentUser.getId()),
                comment.getCreatedAt(), comment.getUpdatedAt());
    }

    private PromptReviewResponse.Review toReview(PromptReview review, User currentUser) {
        User author = review.getUser();
        return new PromptReviewResponse.Review(review.getId(), author.getId(), displayName(author), author.getAvatarUrl(),
                review.getRating(), review.getContent(), Boolean.TRUE.equals(review.getIsEdited()),
                currentUser != null && author.getId().equals(currentUser.getId()), review.getCreatedAt(), review.getUpdatedAt());
    }

    private Prompt requireAccessiblePrompt(UUID promptId, User user) {
        Prompt prompt = promptRepository.findById(promptId)
                .filter(item -> item.getDeletedAt() == null && item.getStatus() != PromptStatus.ARCHIVED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt not found"));
        boolean publicPrompt = (prompt.getVisibility() == PromptVisibility.PUBLIC || prompt.getVisibility() == PromptVisibility.UNLISTED)
                && prompt.getStatus() == PromptStatus.PUBLISHED;
        boolean author = user != null && prompt.getAuthor().getId().equals(user.getId());
        boolean workspaceMember = user != null && prompt.getVisibility() == PromptVisibility.WORKSPACE
                && workspaceMemberRepository.findByWorkspaceAndUser(prompt.getWorkspace(), user).isPresent();
        if (!publicPrompt && !author && !workspaceMember) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt not found");
        }
        return prompt;
    }

    private User requireUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private String displayName(User user) {
        return user.getDisplayName() == null || user.getDisplayName().isBlank() ? user.getUsername() : user.getDisplayName();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
