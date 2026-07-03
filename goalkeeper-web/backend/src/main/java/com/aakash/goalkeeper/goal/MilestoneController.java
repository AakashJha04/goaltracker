package com.aakash.goalkeeper.goal;

import com.aakash.goalkeeper.goal.dto.MilestoneDtos.*;
import com.aakash.goalkeeper.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class MilestoneController {

    private final MilestoneService service;

    public MilestoneController(MilestoneService service) {
        this.service = service;
    }

    @GetMapping("/api/goals/{goalId}/milestones")
    public List<MilestoneDto> list(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID goalId) {
        return service.list(principal.id(), goalId);
    }

    @PostMapping("/api/goals/{goalId}/milestones")
    @ResponseStatus(HttpStatus.CREATED)
    public MilestoneDto create(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID goalId,
                                @Valid @RequestBody MilestoneCreateRequest req) {
        return service.create(principal.id(), goalId, req);
    }

    @PutMapping("/api/milestones/{id}")
    public MilestoneDto update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id,
                                @Valid @RequestBody MilestoneUpdateRequest req) {
        return service.update(principal.id(), id, req);
    }

    @DeleteMapping("/api/milestones/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        service.delete(principal.id(), id);
    }

    @PatchMapping("/api/goals/{goalId}/milestones/reorder")
    public void reorder(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID goalId,
                         @Valid @RequestBody ReorderRequest req) {
        service.reorder(principal.id(), goalId, req.orderedIds());
    }
}
