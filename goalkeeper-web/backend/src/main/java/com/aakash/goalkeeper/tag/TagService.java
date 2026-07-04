package com.aakash.goalkeeper.tag;

import com.aakash.goalkeeper.activity.ActivityService;
import com.aakash.goalkeeper.activity.ActivityType;
import com.aakash.goalkeeper.common.ApiException;
import com.aakash.goalkeeper.goal.GoalService;
import com.aakash.goalkeeper.tag.dto.TagDtos.TagCreateRequest;
import com.aakash.goalkeeper.tag.dto.TagDtos.TagDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tags;
    private final GoalTagRepository goalTags;
    private final GoalService goals;
    private final ActivityService activity;

    public TagService(TagRepository tags, GoalTagRepository goalTags, GoalService goals, ActivityService activity) {
        this.tags = tags;
        this.goalTags = goalTags;
        this.goals = goals;
        this.activity = activity;
    }

    public List<TagDto> list(UUID userId) {
        return tags.findByUserIdOrderByName(userId).stream().map(this::toDto).toList();
    }

    @Transactional
    public TagDto create(UUID userId, TagCreateRequest req) {
        String name = req.name().trim();
        if (tags.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new ApiException(HttpStatus.CONFLICT, "You already have a tag named \"" + name + "\"");
        }
        Tag t = new Tag();
        t.setUserId(userId);
        t.setName(name);
        tags.save(t);
        return toDto(t);
    }

    @Transactional
    public void delete(UUID userId, UUID tagId) {
        Tag t = requireOwned(userId, tagId);
        tags.delete(t);
    }

    @Transactional
    public void attach(UUID userId, UUID goalId, UUID tagId) {
        goals.requireOwned(userId, goalId);
        Tag tag = requireOwned(userId, tagId);
        if (!goalTags.existsByGoalIdAndTagId(goalId, tagId)) {
            goalTags.save(new GoalTag(goalId, tagId));
            activity.record(goalId, ActivityType.TAG_ADDED, "Tag added: " + tag.getName());
        }
    }

    @Transactional
    public void detach(UUID userId, UUID goalId, UUID tagId) {
        goals.requireOwned(userId, goalId);
        Tag tag = requireOwned(userId, tagId);
        goalTags.deleteByGoalIdAndTagId(goalId, tagId);
        activity.record(goalId, ActivityType.TAG_REMOVED, "Tag removed: " + tag.getName());
    }

    public List<TagDto> tagsForGoal(UUID goalId) {
        List<UUID> tagIds = goalTags.findByGoalId(goalId).stream().map(GoalTag::getTagId).toList();
        if (tagIds.isEmpty()) return List.of();
        return tags.findAllById(tagIds).stream().map(this::toDto).toList();
    }

    /** Batch fetch to avoid an N+1 when rendering a page of goals. */
    public Map<UUID, List<TagDto>> tagsForGoals(Collection<UUID> goalIds) {
        if (goalIds.isEmpty()) return Map.of();
        List<GoalTag> links = goalTags.findByGoalIdIn(goalIds);
        if (links.isEmpty()) return Map.of();
        Map<UUID, TagDto> tagById = tags.findAllById(links.stream().map(GoalTag::getTagId).distinct().toList())
                .stream().collect(Collectors.toMap(Tag::getId, this::toDto));
        return links.stream().collect(Collectors.groupingBy(
                GoalTag::getGoalId,
                Collectors.mapping(gt -> tagById.get(gt.getTagId()), Collectors.toList())));
    }

    public UUID resolveTagIdByName(UUID userId, String name) {
        return tags.findByUserIdAndNameIgnoreCase(userId, name).map(Tag::getId).orElse(null);
    }

    public List<UUID> goalIdsForTag(UUID tagId) {
        return goalTags.findGoalIdsByTagId(tagId);
    }

    private Tag requireOwned(UUID userId, UUID tagId) {
        return tags.findByIdAndUserId(tagId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tag not found"));
    }

    private TagDto toDto(Tag t) {
        return new TagDto(t.getId(), t.getName());
    }
}
