package faang.school.achievement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.achievement.client.UserServiceClient;
import faang.school.achievement.dto.UserAchievementDto;
import faang.school.achievement.dto.UserAchievementRequestDto;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAchievementService {
    private final UserServiceClient userServiceClient;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserAchievementMapper userAchievementMapper;
    private final AchievementMessagePublisher achievementMessagePublisher;
    private final ObjectMapper objectMapper;

    public UserAchievementDto addUserAchievement(UserAchievementRequestDto userAchievementRequestDto) {
        Long userId = userAchievementRequestDto.getUserId();
        Long achievementId = userAchievementRequestDto.getAchievementId();

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
        UserAchievementDto userAchievementDto = userAchievementMapper.toDto(userAchievement);
        publishAchievementEvent(userAchievementDto);
        return userAchievementDto;
    }

    private void publishAchievementEvent(UserAchievementDto userAchievementDto) {
        try {
            achievementMessagePublisher.publish(objectMapper.writeValueAsString(userAchievementDto));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Achievement Event: ", e);
        }
    }

    private void validateUserIdExists(Long userId) {
        UserDto userDto = userServiceClient.getUser(userId);
        if (userDto == null) {
            throw new EntityNotFoundException("User not found");
        }
    }
}