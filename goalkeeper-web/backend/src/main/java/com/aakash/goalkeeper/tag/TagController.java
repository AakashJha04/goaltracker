package com.aakash.goalkeeper.tag;

import com.aakash.goalkeeper.goal.GoalService;
import com.aakash.goalkeeper.security.UserPrincipal;
import com.aakash.goalkeeper.tag.dto.TagDtos.TagCreateRequest;
import com.aakash.goalkeeper.tag.dto.TagDtos.TagDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class TagController {

    private final TagService service;
    private final GoalService goals;

    public TagController(TagService service, GoalService goals) {
        this.service = service;
        this.goals = goals;
    }

    @GetMapping("/api/tags")
    public List<TagDto> list(@AuthenticationPrincipal UserPrincipal principal) {
        return service.list(principal.id());
    }

    @GetMapping("/api/goals/{goalId}/tags")
    public List<TagDto> tagsForGoal(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID goalId) {
        goals.requireOwned(principal.id(), goalId);
        return service.tagsForGoal(goalId);
    }

    @PostMapping("/api/tags")
    @ResponseStatus(HttpStatus.CREATED)
    public TagDto create(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody TagCreateRequest req) {
        return service.create(principal.id(), req);
    }

    @DeleteMapping("/api/tags/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        service.delete(principal.id(), id);
    }

    @PostMapping("/api/goals/{goalId}/tags/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void attach(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID goalId, @PathVariable UUID tagId) {
        service.attach(principal.id(), goalId, tagId);
    }

    @DeleteMapping("/api/goals/{goalId}/tags/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void detach(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID goalId, @PathVariable UUID tagId) {
        service.detach(principal.id(), goalId, tagId);
    }
}
