package uz.java.kpisystem.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.java.kpisystem.entity.enums.TaskLayer;
import uz.java.kpisystem.entity.enums.TaskPriority;
import uz.java.kpisystem.entity.enums.TaskStatus;
import uz.java.kpisystem.entity.enums.TaskType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "tasks", indexes = {
        @Index(name = "idx_taskstatus", columnList = "taskStatus")
})
public class Task extends Auditable {

    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY) // N+1 issue
    @JoinColumn(name = "project_id")
    private Project project;

    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;

    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "time_estimate")
    private Long timeEstimate;

    private Long trackTimeInMinutes;

    @ElementCollection
    @BatchSize(size = 50)
    private List<String> attachmentUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private TaskLayer layer;

    @Enumerated(EnumType.STRING)
    private TaskType type;

    @Column(name = "review_due_date")
    private LocalDateTime reviewDueDate;

    private Long parentId;
}
