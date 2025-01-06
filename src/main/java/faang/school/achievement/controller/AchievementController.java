package faang.school.achievement.controller;

import faang.school.achievement.dto.AchievementDto;
import faang.school.achievement.dto.AchievementFilterDto;
import faang.school.achievement.dto.AchievementProgressDto;
import faang.school.achievement.dto.UserAchievementDto;
import faang.school.achievement.service.AchievementService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/achievements")
@RequiredArgsConstructor
@Validated
public class AchievementController {
    private final AchievementService achievementService;

    @GetMapping("/all")
    public List<AchievementDto> getAllAchievement(@RequestBody AchievementFilterDto achievementFilterDto) {
        return achievementService.getAllAchievement(achievementFilterDto);
    }

    @GetMapping("user/{userId}/all")
    public List<UserAchievementDto> gatAllUserAchievementById(@PathVariable @Positive long userId) {
        return achievementService.getAllUserAchievementById(userId);
    }

    @GetMapping("achievement/{achievementId}")
    public AchievementDto getAchievementById(@PathVariable @Positive long achievementId) {
        return achievementService.getAchievementById(achievementId);
    }

    @GetMapping("user/{userId}/progress")
    public List<AchievementProgressDto> getAllNotReceivedUserAchievement(@PathVariable @Positive long userId) {
        return achievementService.getAllNotReceivedUserAchievement(userId);
    }
}
