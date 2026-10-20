package com.sanedge.pointofsale.user.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.protobuf.Empty;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.pb.user.ApiResponseUser;
import com.sanedge.pointofsale.pb.user.ApiResponseUserAll;
import com.sanedge.pointofsale.pb.user.ApiResponseUserDeleteAt;
import com.sanedge.pointofsale.pb.user.CreateUserRequest;
import com.sanedge.pointofsale.pb.user.FindByIdUserRequest;
import com.sanedge.pointofsale.pb.user.UpdateUserRequest;
import com.sanedge.pointofsale.pb.user.VerifyPasswordRequest;
import com.sanedge.pointofsale.pb.user.VerifyPasswordResponse;
import com.sanedge.pointofsale.user.domain.responses.UserResponse;
import com.sanedge.pointofsale.user.domain.responses.UserResponseDeleteAt;
import com.sanedge.pointofsale.user.service.UserCommandService;

@ExtendWith(MockitoExtension.class)
class UserCommandHandlerTest {

    @Mock
    private UserCommandService userCommandService;

    @InjectMocks
    private UserCommandHandler handler;

    private UserResponse user() {
        return UserResponse.builder()
                .id(1)
                .username("admin")
                .firstname("Admin")
                .lastname("User")
                .email("admin@sanedge.com")
                .build();
    }

    private UserResponseDeleteAt deletedUser() {
        return UserResponseDeleteAt.builder()
                .id(1)
                .firstname("Admin")
                .lastname("User")
                .email("admin@sanedge.com")
                .deletedAt("2024-01-03")
                .build();
    }

    @Test
    void create_mapsUser() {
        when(userCommandService.create(any())).thenReturn(ApiResponse.<UserResponse>builder()
                .status("success").message("User registered successfully").data(user()).build());

        TestStreamObserver<ApiResponseUser> observer = new TestStreamObserver<>();
        handler.create(CreateUserRequest.newBuilder()
                .setUsername("admin")
                .setFirstname("Admin")
                .setLastname("User")
                .setEmail("admin@sanedge.com")
                .setPassword("password")
                .setConfirmPassword("password")
                .build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getStatus()).isEqualTo("success");
        assertThat(observer.value().getData().getId()).isEqualTo(1);
    }

    @Test
    void update_mapsUser() {
        when(userCommandService.update(any())).thenReturn(ApiResponse.<UserResponse>builder()
                .status("success").message("updated").data(user()).build());

        TestStreamObserver<ApiResponseUser> observer = new TestStreamObserver<>();
        handler.update(UpdateUserRequest.newBuilder()
                .setId(1)
                .setUsername("admin")
                .setFirstname("Admin")
                .setLastname("User")
                .setEmail("admin@sanedge.com")
                .build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData().getUsername()).isEqualTo("admin");
    }

    @Test
    void trashedUser_mapsDeleteAt() {
        when(userCommandService.trashed(1)).thenReturn(ApiResponse.<UserResponseDeleteAt>builder()
                .status("success").message("trashed").data(deletedUser()).build());

        TestStreamObserver<ApiResponseUserDeleteAt> observer = new TestStreamObserver<>();
        handler.trashedUser(FindByIdUserRequest.newBuilder().setId(1).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getData().getDeletedAt()).isEqualTo("2024-01-03");
    }

    @Test
    void restoreUser_mapsDeleteAt() {
        when(userCommandService.restore(1)).thenReturn(ApiResponse.<UserResponseDeleteAt>builder()
                .status("success").message("restored").data(deletedUser()).build());

        TestStreamObserver<ApiResponseUserDeleteAt> observer = new TestStreamObserver<>();
        handler.restoreUser(FindByIdUserRequest.newBuilder().setId(1).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getStatus()).isEqualTo("success");
    }

    @Test
    void deleteUserPermanent_mapsSuccess() {
        when(userCommandService.deletePermanent(1)).thenReturn(ApiResponse.<Boolean>builder()
                .status("success").message("deleted").data(true).build());

        TestStreamObserver<ApiResponseUserAll> observer = new TestStreamObserver<>();
        handler.deleteUserPermanent(FindByIdUserRequest.newBuilder().setId(1).build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getSuccess()).isTrue();
    }

    @Test
    void restoreAllUser_mapsSuccess() {
        when(userCommandService.restoreAll()).thenReturn(ApiResponse.<Boolean>builder()
                .status("success").message("restored all").data(true).build());

        TestStreamObserver<ApiResponseUserAll> observer = new TestStreamObserver<>();
        handler.restoreAllUser(Empty.getDefaultInstance(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getSuccess()).isTrue();
    }

    @Test
    void deleteAllUserPermanent_mapsSuccess() {
        when(userCommandService.deleteAll()).thenReturn(ApiResponse.<Boolean>builder()
                .status("success").message("deleted all").data(true).build());

        TestStreamObserver<ApiResponseUserAll> observer = new TestStreamObserver<>();
        handler.deleteAllUserPermanent(Empty.getDefaultInstance(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getSuccess()).isTrue();
    }

    @Test
    void verifyPassword_mapsResult() {
        when(userCommandService.verifyPassword("admin", "password"))
                .thenReturn(com.sanedge.pointofsale.user.domain.responses.VerifyPasswordResponse.builder()
                        .valid(true)
                        .userId(1)
                        .username("admin")
                        .message("Password verified")
                        .build());

        TestStreamObserver<VerifyPasswordResponse> observer = new TestStreamObserver<>();
        handler.verifyPassword(VerifyPasswordRequest.newBuilder()
                .setUsername("admin")
                .setPassword("password")
                .build(), observer);

        assertThat(observer.error()).isNull();
        assertThat(observer.value().getValid()).isTrue();
        assertThat(observer.value().getUserId()).isEqualTo(1);
        assertThat(observer.value().getUsername()).isEqualTo("admin");
    }
}
