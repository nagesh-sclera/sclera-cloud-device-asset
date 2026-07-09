package io.sclera.service;

import io.sclera.dto.PhonebookAddressDto;

/** Service contract for the matching service class. */
public interface UtilsService {
    String upsertPhoneAddressById(String username, String vdmsid, String dockername, PhonebookAddressDto phonebookaddressdto);
}
