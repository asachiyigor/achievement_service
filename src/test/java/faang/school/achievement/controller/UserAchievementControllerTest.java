package faang.school.achievement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.achievement.config.context.UserContext;
import faang.school.achievement.dto.AchievementDto;
import faang.school.achievement.dto.UserAchievementDto;
import faang.school.achievement.dto.UserAchievementRequestDto;
import faang.school.achievement.model.Rarity;
import faang.school.achievement.service.UserAchievementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserAchievementController.class)
public class UserAchievementControllerTest {
    private static final String USER_ACHIEVEMENT_URL = "/userachievement";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserContext userContext;
    @MockBean
    private UserAchievementService userAchievementService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Test addUserAchievement - Success")
    public void testAddUserAchievementSuccess() throws Exception {
        when(userAchievementService.addUserAchievement(any(UserAchievementRequestDto.class))).thenReturn(getUserAchievementDto());
        mockMvc.perform(post(USER_ACHIEVEMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-user-id", "2")
                        .content(objectMapper.writeValueAsString(getUserAchievementRequestDto())))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(getUserAchievementDto())));
    }

    static Stream<Object[]> invalidUserAchievementRequestDto() {
        return Stream.of(
                new Object[]{UserAchievementRequestDto.builder()
                        .userId(-1L)
                        .achievementId(2L)
                        .build()
                },
                new Object[]{UserAchievementRequestDto.builder()
                        .userId(1L)
                        .achievementId(-2L)
                        .build()
                },
                new Object[]{UserAchievementRequestDto.builder()
                        .achievementId(2L)
                        .build()
                },
                new Object[]{UserAchievementRequestDto.builder()
                        .userId(1L)
                        .build()
                }
        );
    }

    @ParameterizedTest
    @MethodSource("invalidUserAchievementRequestDto")
    @DisplayName("Test addUserAchievement - Validation Errors")
    public void testAddUserAchievementInvalidDto(UserAchievementRequestDto invalidDto) throws Exception {
        mockMvc.perform(post(USER_ACHIEVEMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-user-id", "2")
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
    private UserAchievementRequestDto getUserAchievementRequestDto() {
        return UserAchievementRequestDto
                .builder()
                .userId(1L)
                .achievementId(2L)
                .build();
    }

    private UserAchievementDto getUserAchievementDto() {
        return UserAchievementDto
                .builder()
                .userId(1L)
                .achievement(AchievementDto.builder().title("MR PRODUCTIVITY")
                        .description("For 1000 finished tasks")
                        .rarity(Rarity.LEGENDARY)
                        .points(20L).build())
                .build();
    }
}
