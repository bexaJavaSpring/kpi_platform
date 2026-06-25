package uz.java.kpisystem.mapper;

import org.mapstruct.*;
import uz.java.kpisystem.dto.task.TaskRequest;
import uz.java.kpisystem.dto.task.TaskResponse;
import uz.java.kpisystem.entity.Task;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(target = "parentId", ignore = true)
    Task toEntity(TaskRequest request);

    TaskResponse toResponse(Task task);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(TaskRequest request, @MappingTarget Task task);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void copyFromExisting(Task task, @MappingTarget Task copiedTask);
}
