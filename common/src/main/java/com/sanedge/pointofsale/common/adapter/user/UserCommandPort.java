package com.sanedge.pointofsale.common.adapter.user;

import com.sanedge.pointofsale.common.adapter.model.CreateUserCmd;
import com.sanedge.pointofsale.common.adapter.model.UpdateUserCmd;
import com.sanedge.pointofsale.common.adapter.model.User;

/**
 * Port for the user command service, replacing direct
 * {@code @GrpcClient("user")} usage in consumer services.
 */
public interface UserCommandPort {

    User create(CreateUserCmd cmd);

    User update(UpdateUserCmd cmd);
}
