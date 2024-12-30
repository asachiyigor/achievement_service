package faang.school.achievement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.achievement.client.UserServiceClient;
import faang.school.achievement.dto.AchievementDto;
import faang.school.achievement.dto.UserAchievementDto;
import faang.school.achievement.dto.UserAchievementRequestDto;
import faang.school.achievement.dto.UserDto;
import faang.school.achievement.mapper.UserAchievementMapper;
import faang.school.achievement.model.Achievement;
import faang.school.achievement.model.Rarity;
import faang.school.achievement.model.UserAchievement;
import faang.school.achievement.publisher.AchievementMessagePublisher;
import faang.school.achievement.repository.AchievementRepository;
import faang.school.achievement.repository.UserAchievementRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserAchievementServiceTest {
    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Mock
    private UserAchievementMapper userAchievementMapper;

    @Mock
    private AchievementMessagePublisher achievementMessagePublisher;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserAchievementService userAchievementService;

    @Test
    void addUserAchievementSuccess() throws IOException {
        long userId = 1L;
        long achievementId = 2L;
        UserAchievementRequestDto userAchievementRequestDto = getUserAchievementRequestDto(userId, achievementId);
        UserAchievementDto userAchievementDto = getUserAchievementDto(userId);
        UserDto userDto = getUserDto(userId);
        Achievement achievement = getAchievement();
        UserAchievement userAchievement = getUserAchievement(achievement);

        when(userServiceClient.getUser(userId)).thenReturn(userDto);
        when(achievementRepository.findById(achievementId)).thenReturn(Optional.of(achievement));
        when(userAchievementRepository.existsByUserIdAndAchievementId(userId, achievementId)).thenReturn(false);
        when(userAchievementMapper.toDto(userAchievement)).thenReturn(userAchievementDto);
        String message = "{\"userId\":1,\"achievement\":{\"title\":\"MR PRODUCTIVITY\",\"description\":\"For 1000 finished tasks\",\"rarity\":\"LEGENDARY\",\"points\":20}}";
        when(objectMapper.writeValueAsString(any(UserAchievementDto.class))).thenReturn(message);

        UserAchievementDto result = userAchievementService.addUserAchievement(userAchievementRequestDto);

        verify(userServiceClient, times(1)).getUser(userId);
        verify(achievementRepository, times(1)).findById(achievementId);
        verify(userAchievementRepository, times(1)).existsByUserIdAndAchievementId(userId, achievementId);
        verify(userAchievementRepository, times(1)).save(any(UserAchievement.class));
        verify(achievementMessagePublisher).publish(message);
        assertEquals(userAchievementDto, result);
    }

    @Test
    void addUserAchievementUserNotFound() {
        long userId = 1L;
        UserAchievementRequestDto userAchievementRequestDto = getUserAchievementRequestDto(userId, 1L);
        when(userServiceClient.getUser(userId)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userAchievementService.addUserAchievement(userAchievementRequestDto));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void addUserAchievementAchievementNotFound() {
        Long userId = 1L;
        Long achievementId = 1L;
        UserAchievementRequestDto userAchievementRequestDto = getUserAchievementRequestDto(userId, 1L);
        UserDto userDto = getUserDto(userId);

        when(userServiceClient.getUser(userId)).thenReturn(userDto);
        when(achievementRepository.findById(achievementId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userAchievementService.addUserAchievement(userAchievementRequestDto));

        assertEquals("Achievement not found", exception.getMessage());
    }

    @Test
    void addUserAchievementAlreadyExists() {
        long userId = 1L;
        long achievementId = 1L;
        UserAchievementRequestDto userAchievementRequestDto = new UserAchievementRequestDto(userId, achievementId);
        UserDto userDto = new UserDto(userId, "John Doe", "john.doe@example.com");
        Achievement achievement = getAchievement();

        when(userServiceClient.getUser(userId)).thenReturn(userDto);
        when(achievementRepository.findById(achievementId)).thenReturn(Optional.of(achievement));
        when(userAchievementRepository.existsByUserIdAndAchievementId(userId, achievementId)).thenReturn(true);

        EntityExistsException exception = assertThrows(EntityExistsException.class,
                () -> userAchievementService.addUserAchievement(userAchievementRequestDto));

        assertEquals("Achievement already exists for this user", exception.getMessage());
    }

    @Test
    void addUserAchievementPublishEventFailure() throws JsonProcessingException {
        Long userId = 1L;
        Long achievementId = 2L;
        UserAchievementRequestDto userAchievementRequestDto = new UserAchievementRequestDto(userId, achievementId);
        UserAchievementDto userAchievementDto = getUserAchievementDto(userId);
        UserDto userDto = getUserDto(userId);
        Achievement achievement = getAchievement();
        String message = "{\"userId\":1,\"achievement\":{\"title\":\"MR PRODUCTIVITY\",\"description\":\"For 1000 finished tasks\",\"rarity\":\"LEGENDARY\",\"points\":20}}";

        when(userServiceClient.getUser(userId)).thenReturn(userDto);
        when(achievementRepository.findById(achievementId)).thenReturn(Optional.of(achievement));
        when(userAchievementRepository.existsByUserIdAndAchievementId(userId, achievementId)).thenReturn(false);
        when(userAchievementMapper.toDto(getUserAchievement(achievement))).thenReturn(userAchievementDto);
        when(objectMapper.writeValueAsString(any(UserAchievementDto.class))).thenThrow(new JsonProcessingException("Serialization error") {
        });

        userAchievementService.addUserAchievement(userAchievementRequestDto);
        verify(achievementMessagePublisher, times(0)).publish(message);
    }

    private static UserAchievement getUserAchievement(Achievement achievement) {
        return UserAchievement
                .builder()
                .userId(1L)
                .achievement(achievement)
                .build();
    }

    private static UserAchievementRequestDto getUserAchievementRequestDto(Long userId, Long achievementId) {
        return UserAchievementRequestDto.builder()
                .userId(userId)
                .achievementId(achievementId)
                .build();
    }

    private UserAchievementDto getUserAchievementDto(Long userId) {
        return UserAchievementDto
                .builder()
                .userId(userId)
                .achievement(AchievementDto.builder().title("MR PRODUCTIVITY")
                        .description("For 1000 finished tasks")
                        .rarity(Rarity.LEGENDARY)
                        .points(20L).build())
                .build();
    }

    private static UserDto getUserDto(Long userId) {
        return UserDto.builder()
                .id(userId)
                .username("User1")
                .build();
    }

    private static Achievement getAchievement() {
        return Achievement.builder()
                .id(2L)
                .title("Test Achievement")
                .build();
    }
}
