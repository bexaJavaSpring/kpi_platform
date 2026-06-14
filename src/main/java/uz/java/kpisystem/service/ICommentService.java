package uz.java.kpisystem.service;

import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.comment.CommentFilter;
import uz.java.kpisystem.dto.comment.CommentRequest;
import uz.java.kpisystem.dto.comment.CommentResponse;

import java.util.List;

public interface ICommentService {

    Long create(CommentRequest request);

    ApiResponse<List<CommentResponse>> getAll(CommentFilter filter);

    ApiResponse<CommentResponse> getOne(Long id);

    ApiResponse<CommentResponse> update(Long id, CommentRequest request);

    Boolean delete(Long id);
}
