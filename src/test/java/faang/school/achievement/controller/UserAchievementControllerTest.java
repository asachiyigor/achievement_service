package faang.school.achievement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.achievement.config.context.UserContext;
import faang.school.achievement.dto.UserAchievementDto;
import faang.school.achievement.service.UserAchievementService;
import org.junit.jupiter.api.BeforeEach;
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
    private UserAchievementDto userAchievementDto;

    @BeforeEach
    public void setUp() {
        userAchievementDto = UserAchievementDto
                .builder()
                .userId(1L)
                .achievementId(2L)
                .build();
    }

    @Test
    @DisplayName("Test addUserAchievement - Success")
    public void testAddUserAchievementSuccess() throws Exception {
        when(userAchievementService.addUserAchievement(any(UserAchievementDto.class))).thenReturn(userAchievementDto);
        mockMvc.perform(post(USER_ACHIEVEMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-user-id", "2")
                        .content(objectMapper.writeValueAsString(userAchievementDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(userAchievementDto)));
    }

    static Stream<Object[]> invalidUserAchievementDto() {
        return Stream.of(
                new Object[]{UserAchievementDto.builder()
                        .userId(-1L)
                        .achievementId(2L)
                        .build()
                },
                new Object[]{UserAchievementDto.builder()
                        .userId(1L)
                        .achievementId(-2L)
                        .build()
                },
                new Object[]{UserAchievementDto.builder()
                        .achievementId(2L)
                        .build()
                },
                new Object[]{UserAchievementDto.builder()
                        .userId(1L)
                        .build()
                }
        );

    }

    @ParameterizedTest
    @MethodSource("invalidUserAchievementDto")
    @DisplayName("Test addUserAchievement - Validation Errors")
    public void testAddUserAchievementInvalidDto(UserAchievementDto invalidDto) throws Exception {
        mockMvc.perform(post(USER_ACHIEVEMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-user-id", "2")
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}
