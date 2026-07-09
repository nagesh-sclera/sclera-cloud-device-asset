package io.sclera.service;

import io.sclera.service.impl.UserActionLogDTO;

import java.util.List;

/** Service contract for {@link io.sclera.service.UserActionLogService}. */
public interface UserActionLogService {

    void addUserAction(String username, String type, String action,
                       String message, String status, String subType, String recordId);

    void batchUpdateUserActionLogs(List<UserActionLogDTO> logs);
}
