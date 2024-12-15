package faang.school.achievement.controller;

import faang.school.achievement.dto.UserAchievementDto;
import faang.school.achievement.service.UserAchievementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/userachievement")
public class UserAchievementController {

    private final UserAchievementService userAchievementService;

    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    public UserAchievementDto addUserAchievement(@RequestBody @Valid UserAchievementDto userAchievementDto) throws IOException {
        return userAchievementService.addUserAchievement(userAchievementDto);
    }
}