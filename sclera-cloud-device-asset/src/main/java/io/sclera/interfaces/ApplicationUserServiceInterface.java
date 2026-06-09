package io.sclera.interfaces;

import io.sclera.dto.InventoryApplicationUserDTO;
import java.util.*;

/** Service contract for the matching service class. */
public interface ApplicationUserServiceInterface {

    Set<String> upsertApplicationUsersSync(List<InventoryApplicationUserDTO> taggedApplicationUsers);

    Set<String> deleteApplicationUsersSync(List<InventoryApplicationUserDTO> taggedApplicationUsers);

    void processSingleUserUpsertTransaction(InventoryApplicationUserDTO user);

    void processSingleUserDeleteTransaction(InventoryApplicationUserDTO user);
}
