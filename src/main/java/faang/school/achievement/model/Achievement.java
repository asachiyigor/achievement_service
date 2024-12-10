package faang.school.achievement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="achievement")
public class Achievement implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private long id;

    @Column(name = "title", nullable = false, unique = true, length = 128)
    private String title;

    @Column(name = "description", nullable = false, unique = true, length = 1024)
    private String description;

    @Column(name = "rarity", nullable = false)
    @Enumerated(EnumType.STRING)
    private Rarity rarity;

    @JsonIgnore
    @Fetch(FetchMode.JOIN)
    @OneToMany(mappedBy = "achievement")
    private List<UserAchievement> userAchievements;

    @JsonIgnore
    @Fetch(FetchMode.JOIN)
    @OneToMany(mappedBy = "achievement")
    private List<AchievementProgress> progresses;

    @Column(name = "points", nullable = false)
    private long points;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
