package io.sclera.service;

import io.sclera.dto.PhonebookAddressDto;
import io.sclera.interfaces.UtilsServiceInterface;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to edge-D */
@Service
public class UtilsService implements UtilsServiceInterface {
    /**
     * Inserts or updates a phonebook address entry. Stub returns a placeholder value.
     */
    public String upsertPhoneAddressById(String username, String vdmsid, String dockername, PhonebookAddressDto phonebookaddressdto) {
        return "TEST";
    }
    // Methods added on demand by compile loop.
}
