package faang.school.achievement.controllers;

import faang.school.achievement.dto.AchievementDto;
import faang.school.achievement.dto.AchievementFilterDto;
import faang.school.achievement.dto.AchievementProgressDto;
import faang.school.achievement.dto.UserAchievementDto;
import faang.school.achievement.service.AchievementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {AchievementController.class})
class AchievementControllerTest {

    @MockBean
    AchievementService achievementService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void getAllAchievement() throws Exception {
        AchievementFilterDto achievementFilterDto = AchievementFilterDto.builder()
                .title("first")
                .build();

        AchievementDto firstAchievementDto = AchievementDto.builder()
                .title("first")
                .description("first achievement")
                .points(50)
                .build();

        when(achievementService.getAllAchievement(achievementFilterDto)).thenReturn(Collections.singletonList(firstAchievementDto));

        mockMvc.perform(get("/api/v1/achievements/all").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(achievementFilterDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("first")))
                .andExpect(jsonPath("$[0].description", is("first achievement")))
                .andExpect(jsonPath("$[0].points", is(50)));

        verify(achievementService, times(1)).getAllAchievement(achievementFilterDto);
    }

    @Test
    void gatAllUserAchievementById() throws Exception {
        long userId = 1L;

        AchievementDto achievementDto = AchievementDto.builder()
                .title("first")
                .description("first achievement")
                .points(50)
                .build();

        UserAchievementDto userAchievementDto = UserAchievementDto.builder()
                .userId(1L)
                .achievement(achievementDto)
                .build();

        when(achievementService.getAllUserAchievementById(userId)).thenReturn(Collections.singletonList(userAchievementDto));

        mockMvc.perform(get("/api/v1/achievements/user/{userId}/all", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId", is(1)))
                .andExpect(jsonPath("$[0].achievement.title", is("first")))
                .andExpect(jsonPath("$[0].achievement.description", is("first achievement")))
                .andExpect(jsonPath("$[0].achievement.points", is(50)));

        verify(achievementService, times(1)).getAllUserAchievementById(userId);

    }

    @Test
    void getAchievementById() throws Exception {
        long achievementId = 1;

        AchievementDto achievementDto = AchievementDto.builder()
                .title("first")
                .description("first achievement")
                .points(50)
                .build();

        when(achievementService.getAchievementById(achievementId)).thenReturn(achievementDto);

        mockMvc.perform(get("/api/v1/achievements/achievement/{achievementId}", achievementId))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("first")))
                .andExpect(jsonPath("$.description", is("first achievement")))
                .andExpect(jsonPath("$.points", is(50)));

        verify(achievementService, times(1)).getAchievementById(achievementId);
    }

    @Test
    void getAllNotReceivedUserAchievement() throws Exception {
        long userId = 1L;
        AchievementDto achievementDto = AchievementDto.builder()
                .title("first")
                .description("first achievement")
                .points(50)
                .build();

        AchievementProgressDto achievementProgressDto = AchievementProgressDto.builder()
                .achievement(achievementDto)
                .currentPoints(20)
                .build();

        when(achievementService.getAllNotReceivedUserAchievement(userId)).thenReturn(Collections.singletonList(achievementProgressDto));

        mockMvc.perform(get("/api/v1/achievements/user/{userId}/progress", userId))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].achievement.title", is("first")))
                .andExpect(jsonPath("$[0].achievement.description", is("first achievement")))
                .andExpect(jsonPath("$[0].achievement.points", is(50)))
                .andExpect(jsonPath("$[0].currentPoints", is(20)));

        verify(achievementService, times(1)).getAllNotReceivedUserAchievement(userId);
    }
}