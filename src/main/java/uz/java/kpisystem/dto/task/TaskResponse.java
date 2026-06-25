package uz.java.kpisystem.dto.task;

import lombok.Data;
import uz.java.kpisystem.dto.file.FileResponse;
import uz.java.kpisystem.dto.project.ProjectInfo;
import uz.java.kpisystem.dto.taskTag.TaskTagResponse;
import uz.java.kpisystem.dto.user.UserInfo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskResponse {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private List<TaskResponse> children;
    private String status;
    private LocalDateTime deadline;
    private String  priority;
    private LocalDate startDate;
    private Long timeEstimate;
    private Long trackTimeInMinutes;
    private String  layer;
    private String  type;
    private LocalDateTime reviewDueDate;
//    private List<UserInfo> assignerIds;
    private List<FileResponse> attachmentUrls;
    private List<TaskTagResponse> tags;
    private ProjectInfo projectInfo;

}
