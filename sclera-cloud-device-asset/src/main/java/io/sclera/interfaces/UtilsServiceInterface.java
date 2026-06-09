package io.sclera.interfaces;

import io.sclera.dto.PhonebookAddressDto;

/** Service contract for the matching service class. */
public interface UtilsServiceInterface {
    String upsertPhoneAddressById(String username, String vdmsid, String dockername, PhonebookAddressDto phonebookaddressdto);
}
