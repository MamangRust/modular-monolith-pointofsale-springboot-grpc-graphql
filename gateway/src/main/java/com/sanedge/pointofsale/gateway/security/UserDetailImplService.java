package com.sanedge.pointofsale.gateway.security;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.pb.role.FindByUserIdRoleRequest;
import com.sanedge.pointofsale.pb.role.RoleQueryServiceGrpc;
import com.sanedge.pointofsale.pb.role.RoleResponse;
import com.sanedge.pointofsale.pb.user.ApiResponseUser;
import com.sanedge.pointofsale.pb.user.FindByUsernameUserRequest;
import com.sanedge.pointofsale.pb.user.UserQueryServiceGrpc;
import com.sanedge.pointofsale.pb.user.UserResponse;

import lombok.RequiredArgsConstructor;

/**
 * Gateway-side {@link UserDetailsService}. The gateway owns no database, so the
 * user and its authorities are loaded from the user and role services over gRPC.
 */
@Service
@RequiredArgsConstructor
public class UserDetailImplService implements UserDetailsService {

    private final UserQueryServiceGrpc.UserQueryServiceBlockingStub userQueryStub;
    private final RoleQueryServiceGrpc.RoleQueryServiceBlockingStub roleQueryStub;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ApiResponseUser response;
        try {
            response = userQueryStub.findByUsername(FindByUsernameUserRequest.newBuilder()
                    .setUsername(username)
                    .build());
        } catch (Exception e) {
            throw new UsernameNotFoundException("User Not Found with username: " + username, e);
        }

        if (!response.hasData() || response.getData().getId() == 0) {
            throw new UsernameNotFoundException("User Not Found with username: " + username);
        }

        UserResponse user = response.getData();

        List<GrantedAuthority> authorities = roleQueryStub
                .findByUserId(FindByUserIdRoleRequest.newBuilder().setUserId(user.getId()).build())
                .getDataList().stream()
                .map(RoleResponse::getName)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();

        return new UserDetailsImpl((long) user.getId(), user.getUsername(), user.getEmail(), "", authorities);
    }
}
