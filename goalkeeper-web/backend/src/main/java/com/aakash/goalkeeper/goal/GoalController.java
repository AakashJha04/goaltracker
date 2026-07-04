package com.aakash.goalkeeper.goal;

import com.aakash.goalkeeper.goal.dto.GoalDtos.*;
import com.aakash.goalkeeper.goal.dto.PageResponse;
import com.aakash.goalkeeper.security.UserPrincipal;
import com.aakash.goalkeeper.tag.TagService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private static final int MAX_PAGE_SIZE = 100;

    private final GoalService service;
    private final TagService tags;

    public GoalController(GoalService service, TagService tags) {
        this.service = service;
        this.tags = tags;
    }

    @GetMapping
    public PageResponse<GoalDto> list(@AuthenticationPrincipal UserPrincipal principal,
                                       @RequestParam(required = false) GoalStatus status,
                                       @RequestParam(required = false) String category,
                                       @RequestParam(required = false) String search,
                                       @RequestParam(required = false) String tag,
                                       @RequestParam(defaultValue = "createdAt") String sort,
                                       @RequestParam(defaultValue = "desc") String dir,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        int boundedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(Math.max(page, 0), boundedSize, Sort.by(direction, sort));

        List<UUID> restrictToIds = null;
        if (tag != null && !tag.isBlank()) {
            UUID tagId = tags.resolveTagIdByName(principal.id(), tag);
            restrictToIds = tagId == null ? List.of() : tags.goalIdsForTag(tagId);
        }
        return service.list(principal.id(), status, category, search, restrictToIds, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoalDto create(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody GoalCreateRequest req) {
        return service.create(principal.id(), req);
    }

    @GetMapping("/{id}")
    public GoalDto get(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        return service.get(principal.id(), id);
    }

    @PutMapping("/{id}")
    public GoalDto update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id,
                           @Valid @RequestBody GoalUpdateRequest req) {
        return service.update(principal.id(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        service.delete(principal.id(), id);
    }

    @PatchMapping("/{id}/status")
    public GoalDto updateStatus(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id,
                                 @Valid @RequestBody StatusUpdateRequest req) {
        return service.updateStatus(principal.id(), id, req.status());
    }

    @PatchMapping("/{id}/progress")
    public GoalDto updateProgress(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id,
                                   @Valid @RequestBody ProgressUpdateRequest req) {
        return service.updateProgress(principal.id(), id, req.progress());
    }
}
