package uz.java.kpisystem.mapper;

import org.mapstruct.*;
import uz.java.kpisystem.dto.comment.CommentRequest;
import uz.java.kpisystem.dto.comment.CommentResponse;
import uz.java.kpisystem.entity.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "parentId", ignore = true)
    Comment toEntity(CommentRequest request);

    CommentResponse toResponse(Comment comment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(CommentRequest request, @MappingTarget Comment comment);
}
