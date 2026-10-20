package com.sanedge.pointofsale.common.adapter.user;

import com.sanedge.pointofsale.common.adapter.model.User;
import com.sanedge.pointofsale.common.adapter.model.VerifyPasswordResult;

/**
 * Port for the user query service, replacing direct
 * {@code @GrpcClient("user")} usage in consumer services.
 */
public interface UserQueryPort {

    User findById(int id);

    /**
     * Returns the matching user, or {@code null} when none exists. Callers decide
     * whether an absent user is a conflict or a not-found condition.
     */
    User findByEmail(String email);

    User findByUsername(String username);

    VerifyPasswordResult verifyPassword(String email, String password);
}
