package io.sclera.interfaces;

import io.sclera.service.UserActionLogDTO;

import java.util.List;

/** Service contract for {@link io.sclera.service.UserActionLogService}. */
public interface UserActionLogServiceInterface {

    void addUserAction(String username, String type, String action,
                       String message, String status, String subType, String recordId);

    void batchUpdateUserActionLogs(List<UserActionLogDTO> logs);
}
