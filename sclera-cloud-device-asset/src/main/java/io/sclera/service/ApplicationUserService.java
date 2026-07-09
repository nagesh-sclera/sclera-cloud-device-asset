package io.sclera.service;

import io.sclera.dto.InventoryApplicationUserDTO;
import java.util.*;

/** Service contract for the matching service class. */
public interface ApplicationUserService {

    Set<String> upsertApplicationUsersSync(List<InventoryApplicationUserDTO> taggedApplicationUsers);

    Set<String> deleteApplicationUsersSync(List<InventoryApplicationUserDTO> taggedApplicationUsers);

    void processSingleUserUpsertTransaction(InventoryApplicationUserDTO user);

    void processSingleUserDeleteTransaction(InventoryApplicationUserDTO user);
}
