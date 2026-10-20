package com.sanedge.pointofsale.user.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.domain.responses.api.PaginationMeta;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.pb.user.ApiResponsePaginationUser;
import com.sanedge.pointofsale.pb.user.ApiResponsePaginationUserDeleteAt;
import com.sanedge.pointofsale.pb.user.ApiResponseUser;
import com.sanedge.pointofsale.pb.user.FindAllUserRequest;
import com.sanedge.pointofsale.pb.user.FindByEmailUserRequest;
import com.sanedge.pointofsale.pb.user.FindByIdUserRequest;
import com.sanedge.pointofsale.pb.user.FindByUsernameUserRequest;
import com.sanedge.pointofsale.user.domain.responses.UserResponse;
import com.sanedge.pointofsale.user.domain.responses.UserResponseDeleteAt;
import com.sanedge.pointofsale.user.service.UserQueryService;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

@ExtendWith(MockitoExtension.class)
class UserQueryHandlerTest {

    @Mock
    private UserQueryService userQueryService;

    @InjectMocks
    private UserQueryHandler handler;

    private UserResponse user() {
        return UserResponse.builder()
                .id(1)
                .username("admin")
                .firstname("Admin")
                .lastname("User")
                .email("admin@sanedge.com")
                .createdAt("2024-01-01")
                .updatedAt("2024-01-02")
                .build();
    }

    private UserResponseDeleteAt deletedUser() {
        return UserResponseDeleteAt.builder()
                .id(2)
                .firstname("John")
                .lastname("Doe")
                .email("user@sanedge.com")
                .createdAt("2024-01-01")
                .updatedAt("2024-01-02")
                .deletedAt("2024-01-03")
                .build();
    }

    @Test
    void findAll_mapsDataAndPagination() {
        ApiResponsePagination<List<UserResponse>> res = ApiResponsePagination.<List<UserResponse>>builder()
                .status("success")
                .message("Users retrieved successfully")
                .data(List.of(user()))
                .pagination(new PaginationMeta(1, 10, 1, 1))
                .build();
        when(userQueryService.findAll(any())).thenReturn(res);

        TestStreamObserver<ApiResponsePaginationUser> observer = new TestStreamObserver<>();
        handler.findAll(FindAllUserRequest.newBuilder().setPage(1).setPageSize(10).setSearch("").build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.isCompleted()).isTrue();
        ApiResponsePaginationUser out = observer.value();
        assertThat(out.getStatus()).isEqualTo("success");
        assertThat(out.getDataCount()).isEqualTo(1);
        assertThat(out.getData(0).getId()).isEqualTo(1);
        assertThat(out.getData(0).getUsername()).isEqualTo("admin");
        assertThat(out.getPaginationMeta().getTotalRecords()).isEqualTo(1);
    }

    @Test
    void findById_mapsUser() {
        when(userQueryService.findById(1)).thenReturn(ApiResponse.<UserResponse>builder()
                .status("success").message("ok").data(user()).build());

        TestStreamObserver<ApiResponseUser> observer = new TestStreamObserver<>();
        handler.findById(FindByIdUserRequest.newBuilder().setId(1).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData().getEmail()).isEqualTo("admin@sanedge.com");
    }

    @Test
    void findByEmail_mapsUser() {
        when(userQueryService.findByEmail("admin@sanedge.com")).thenReturn(ApiResponse.<UserResponse>builder()
                .status("success").message("ok").data(user()).build());

        TestStreamObserver<ApiResponseUser> observer = new TestStreamObserver<>();
        handler.findByEmail(FindByEmailUserRequest.newBuilder().setEmail("admin@sanedge.com").build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData().getUsername()).isEqualTo("admin");
    }

    @Test
    void findByUsername_mapsUser() {
        when(userQueryService.findByUsername("admin")).thenReturn(ApiResponse.<UserResponse>builder()
                .status("success").message("ok").data(user()).build());

        TestStreamObserver<ApiResponseUser> observer = new TestStreamObserver<>();
        handler.findByUsername(FindByUsernameUserRequest.newBuilder().setUsername("admin").build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData().getId()).isEqualTo(1);
    }

    @Test
    void findByActive_mapsDeleteAtData() {
        ApiResponsePagination<List<UserResponseDeleteAt>> res = ApiResponsePagination.<List<UserResponseDeleteAt>>builder()
                .status("success")
                .message("ok")
                .data(List.of(deletedUser()))
                .pagination(new PaginationMeta(1, 10, 1, 1))
                .build();
        when(userQueryService.findByActive(any())).thenReturn(res);

        TestStreamObserver<ApiResponsePaginationUserDeleteAt> observer = new TestStreamObserver<>();
        handler.findByActive(FindAllUserRequest.newBuilder().setPage(1).setPageSize(10).setSearch("").build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData(0).getDeletedAt()).isEqualTo("2024-01-03");
    }

    @Test
    void findByTrashed_mapsDeleteAtData() {
        ApiResponsePagination<List<UserResponseDeleteAt>> res = ApiResponsePagination.<List<UserResponseDeleteAt>>builder()
                .status("success")
                .message("ok")
                .data(List.of(deletedUser()))
                .pagination(new PaginationMeta(1, 10, 1, 1))
                .build();
        when(userQueryService.findByTrashed(any())).thenReturn(res);

        TestStreamObserver<ApiResponsePaginationUserDeleteAt> observer = new TestStreamObserver<>();
        handler.findByTrashed(FindAllUserRequest.newBuilder().setPage(1).setPageSize(10).setSearch("").build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData(0).getId()).isEqualTo(2);
    }

    @Test
    void findById_notFound_mapsToGrpcNotFound() {
        when(userQueryService.findById(99)).thenThrow(new ResourceNotFoundException("User not found"));

        TestStreamObserver<ApiResponseUser> observer = new TestStreamObserver<>();
        handler.findById(FindByIdUserRequest.newBuilder().setId(99).build(), observer);

        assertThat(observer.value()).isNull();
        assertThat(observer.error()).isInstanceOf(StatusRuntimeException.class);
        assertThat(((StatusRuntimeException) observer.error()).getStatus().getCode())
                .isEqualTo(Status.Code.NOT_FOUND);
    }
}
