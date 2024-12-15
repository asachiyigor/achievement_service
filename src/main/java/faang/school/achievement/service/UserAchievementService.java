package faang.school.achievement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.achievement.client.UserServiceClient;
import faang.school.achievement.dto.AchievementEventDto;
import faang.school.achievement.dto.UserAchievementDto;
import faang.school.achievement.dto.UserDto;
import faang.school.achievement.mapper.UserAchievementMapper;
import faang.school.achievement.model.Achievement;
import faang.school.achievement.model.UserAchievement;
import faang.school.achievement.publisher.AchievementMessagePublisher;
import faang.school.achievement.repository.AchievementRepository;
import faang.school.achievement.repository.UserAchievementRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserAchievementService {
    private final UserServiceClient userServiceClient;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserAchievementMapper userAchievementMapper;
    private final AchievementMessagePublisher achievementMessagePublisher;
    private final ObjectMapper objectMapper;

    public UserAchievementDto addUserAchievement(UserAchievementDto userAchievementDto) throws IOException {
        Long userId = userAchievementDto.getUserId();
        Long achievementId = userAchievementDto.getAchievementId();

        validateUserIdExists(userId);
        Achievement achievement = achievementRepository
                .findById(achievementId)
                .orElseThrow(() -> new EntityNotFoundException("Achievement not found"));

        if (userAchievementRepository.existsByUserIdAndAchievementId(userId, achievementId)) {
            throw new EntityExistsException("Achievement already exists for this user");
        }

        UserAchievement userAchievement = UserAchievement
                .builder()
                .userId(userId)
                .achievement(achievement)
                .build();

        userAchievementRepository.save(userAchievement);
        publishAchievementEvent(userAchievementDto);
        return userAchievementMapper.toDto(userAchievement);
    }

    private void publishAchievementEvent(UserAchievementDto userAchievementDto) throws IOException {
        AchievementEventDto achievementEventDto = AchievementEventDto
                .builder()
                .userId(userAchievementDto.getUserId())
                .achievementId(userAchievementDto.getAchievementId())
                .createdAt(LocalDateTime.now())
                .build();
        achievementMessagePublisher.publish(objectMapper.writeValueAsString(achievementEventDto));
    }

    private void validateUserIdExists(Long userId) {
        UserDto userDto = userServiceClient.getUser(userId);
        if (userDto == null) {
            throw new EntityNotFoundException("User not found");
        }
    }
}