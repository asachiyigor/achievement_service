package faang.school.achievement.service;

import faang.school.achievement.dto.AchievementDto;
import faang.school.achievement.dto.AchievementFilterDto;
import faang.school.achievement.dto.AchievementProgressDto;
import faang.school.achievement.dto.UserAchievementDto;
import faang.school.achievement.filters.achievementFilters.AchievementFilter;
import faang.school.achievement.mapper.AchievementMapper;
import faang.school.achievement.model.Achievement;
import faang.school.achievement.model.AchievementProgress;
import faang.school.achievement.model.Rarity;
import faang.school.achievement.repository.AchievementProgressRepository;
import faang.school.achievement.repository.AchievementRepository;
import faang.school.achievement.repository.UserAchievementRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock
    AchievementRepository achievementRepository;

    @Mock
    AchievementProgressRepository achievementProgressRepository;

    @Mock
    UserAchievementRepository userAchievementRepository;

    @Mock
    AchievementMapper achievementMapper;

    @InjectMocks
    AchievementService achievementService;


    @Test
    void positiveGetAllAchievementWithFilter() {
        AchievementFilter filterMock = Mockito.mock(AchievementFilter.class);

        AchievementFilterDto achievementFilterDto = AchievementFilterDto.builder()
                .title("first")
                .build();

        Achievement achievement1 = Achievement.builder()
                .id(1L)
                .title("first")
                .description("Description 1")
                .rarity(Rarity.COMMON)
                .points(10)
                .build();

        Achievement achievement2 = Achievement.builder()
                .id(2L)
                .title("second")
                .description("Description 2")
                .rarity(Rarity.RARE)
                .points(20)
                .build();

        AchievementDto achievementDto1 = AchievementDto.builder()
                .title("first")
                .description("Description 1")
                .rarity(Rarity.COMMON)
                .points(10)
                .build();

        List<Achievement> achievements = List.of(achievement1, achievement2);

        when(achievementRepository.findAll()).thenReturn(achievements);
        when(filterMock.isApplicable(achievementFilterDto)).thenReturn(true);
        when(filterMock.apply(any(), eq(achievementFilterDto))).thenReturn(Stream.of(achievement1));

        when(achievementMapper.toDtoFromEntity(achievement1)).thenReturn(achievementDto1);

        List<AchievementFilter> achievementFilters = List.of(filterMock);

        AchievementService achievementService = new AchievementService(
                achievementRepository,
                achievementProgressRepository,
                userAchievementRepository,
                achievementFilters,
                achievementMapper
        );

        List<AchievementDto> result = achievementService.getAllAchievement(achievementFilterDto);

        verify(filterMock).isApplicable(achievementFilterDto);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("first", result.get(0).getTitle());
        assertEquals("Description 1", result.get(0).getDescription());
        assertEquals(Rarity.COMMON, result.get(0).getRarity());
        assertEquals(10, result.get(0).getPoints());
    }

    @Test
    void negativeGetAllAchievementWithDescriptionFilter() {
        AchievementFilter filterMock = Mockito.mock(AchievementFilter.class);

        AchievementFilterDto achievementFilterDto = AchievementFilterDto.builder()
                .title("nonexistent")
                .build();

        Achievement achievement1 = Achievement.builder()
                .id(1L)
                .title("first")
                .description("Description 1")
                .rarity(Rarity.COMMON)
                .points(10)
                .build();

        Achievement achievement2 = Achievement.builder()
                .id(2L)
                .title("second")
                .description("Description 2")
                .rarity(Rarity.RARE)
                .points(20)
                .build();

        List<Achievement> achievements = List.of(achievement1, achievement2);

        when(achievementRepository.findAll()).thenReturn(achievements);
        when(filterMock.isApplicable(achievementFilterDto)).thenReturn(false);

        List<AchievementFilter> achievementFilters = List.of(filterMock);

        AchievementService achievementService = new AchievementService(
                achievementRepository,
                achievementProgressRepository,
                userAchievementRepository,
                achievementFilters,
                achievementMapper
        );

        List<AchievementDto> result = achievementService.getAllAchievement(achievementFilterDto);

        verify(filterMock).isApplicable(achievementFilterDto);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void PositiveGetAllUserAchievementById() {
        long userId = 1L;
        AchievementDto firstAchievementDto =
                AchievementDto.builder()
                        .description("first")
                        .title("first")
                        .build();
        AchievementDto secondAchievementDto =
                AchievementDto.builder()
                        .description("second")
                        .title("second")
                        .build();

        List<UserAchievementDto> userAchievementDtos = List.of(
                UserAchievementDto.builder()
                        .userId(1L)
                        .achievement(firstAchievementDto)
                        .build(),
                UserAchievementDto.builder()
                        .userId(1L)
                        .achievement(secondAchievementDto)
                        .build()
        );

        when(userAchievementRepository.findByUserId(anyLong())).thenReturn(new ArrayList<>());
        when(achievementMapper.toUserAchievementDto(anyList())).thenReturn(userAchievementDtos);

        List<UserAchievementDto> result = achievementService.getAllUserAchievementById(userId);

        verify(userAchievementRepository, times(1)).findByUserId(eq(userId));
        verify(achievementMapper, times(1)).toUserAchievementDto(anyList());

        assertEquals(2, result.size());
        assertEquals(firstAchievementDto, result.get(0).getAchievement());
        assertEquals(secondAchievementDto, result.get(1).getAchievement());
    }

    @Test
    void NegativeGetAllUserAchievementById() {
        long userId = 1L;

        when(userAchievementRepository.findByUserId(anyLong())).thenReturn(new ArrayList<>());
        when(achievementMapper.toUserAchievementDto(anyList())).thenReturn(new ArrayList<>());

        List<UserAchievementDto> result = achievementService.getAllUserAchievementById(userId);

        verify(userAchievementRepository, times(1)).findByUserId(eq(userId));
        verify(achievementMapper, times(1)).toUserAchievementDto(anyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void NegativeGetAllUserAchievementByIdWithException() {
        long userId = 1L;

        when(userAchievementRepository.findByUserId(anyLong())).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> achievementService.getAllUserAchievementById(userId));

        verify(userAchievementRepository, times(1)).findByUserId(eq(userId));
    }

    @Test
    void PositiveGetAchievementById() {
        long achievementId = 1L;

        Achievement achievement = Achievement.builder()
                .id(achievementId)
                .title("title")
                .description("description")
                .build();

        AchievementDto expectedDto = AchievementDto.builder()
                .title("title")
                .description("description")
                .build();

        when(achievementRepository.findById(achievementId)).thenReturn(Optional.of(achievement));
        when(achievementMapper.toDtoFromEntity(achievement)).thenReturn(expectedDto);

        AchievementDto actualDto = achievementService.getAchievementById(achievementId);

        verify(achievementRepository).findById(achievementId);
        verify(achievementMapper).toDtoFromEntity(achievement);

        assertEquals(expectedDto, actualDto);
    }

    @Test
    void NegativeGetAchievementById() {
        long achievementId = 1L;

        when(achievementRepository.findById(achievementId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                achievementService.getAchievementById(achievementId));

        assertEquals("Достижение не найдено", exception.getMessage());
    }

    @Test
    void positiveGetAllNotReceivedUserAchievement() {
        long userId = 1L;

        AchievementDto achievementDto = AchievementDto.builder()
                .title("Test Achievement")
                .description("Test Description")
                .rarity(Rarity.COMMON)
                .points(100)
                .build();

        AchievementProgress achievementProgress = AchievementProgress.builder()
                .userId(userId)
                .achievement(Achievement.builder()
                        .id(1L)
                        .title("Test Achievement")
                        .description("Test Description")
                        .rarity(Rarity.COMMON)
                        .points(100)
                        .build())
                .currentPoints(50)
                .build();

        AchievementProgressDto achievementProgressDto = AchievementProgressDto.builder()
                .achievement(achievementDto)
                .currentPoints(50)
                .build();

        List<AchievementProgress> achievementProgresses = List.of(achievementProgress);
        List<AchievementProgressDto> expectedDtos = List.of(achievementProgressDto);

        when(achievementProgressRepository.findByUserId(eq(userId))).thenReturn(achievementProgresses);
        when(achievementMapper.toAchievementProgress(anyList())).thenReturn(expectedDtos);

        List<AchievementProgressDto> result = achievementService.getAllNotReceivedUserAchievement(userId);

        verify(achievementProgressRepository, times(1)).findByUserId(eq(userId));
        verify(achievementMapper, times(1)).toAchievementProgress(anyList());

        assertEquals(1, result.size());
        assertEquals(achievementDto, result.get(0).getAchievement());
        assertEquals(50, result.get(0).getCurrentPoints());
    }

    @Test
    void negativeGetAllNotReceivedUserAchievement() {
        long userId = 1L;

        AchievementProgress achievementProgress = AchievementProgress.builder()
                .userId(userId)
                .achievement(Achievement.builder()
                        .id(1L)
                        .title("Test Achievement")
                        .description("Test Description")
                        .rarity(Rarity.COMMON)
                        .points(100)
                        .build())
                .currentPoints(50)
                .build();

        when(achievementProgressRepository.findByUserId(eq(userId))).thenReturn(List.of(achievementProgress));
        when(achievementMapper.toAchievementProgress(anyList())).thenReturn(new ArrayList<>());

        List<AchievementProgressDto> result = achievementService.getAllNotReceivedUserAchievement(userId);

        verify(achievementProgressRepository, times(1)).findByUserId(eq(userId));
        verify(achievementMapper, times(1)).toAchievementProgress(anyList());

        assertTrue(result.isEmpty());
    }
}