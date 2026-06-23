package io.sclera.service;

import io.sclera.Repository.TechnicianRepository;
import io.sclera.dto.TechnicianDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Coverage for TechnicianService.getAvailableTechnicianCountryCodePhoneByDeviceId: resolves tagged
 * technicians, fetches each with country-code/phone/availability, and filters to those marked
 * "Available". Only technicianRepository is exercised (the other constructor deps stay null).
 */
@ExtendWith(MockitoExtension.class)
class TechnicianServiceMoreTest {

    @Mock TechnicianRepository technicianRepository;

    @InjectMocks TechnicianService service;

    @Test
    void getAvailable_noTaggedTechnicians_returnsEmpty() {
        when(technicianRepository.getAllTaggedTechnicianIds("d1")).thenReturn(List.of());
        assertThat(service.getAvailableTechnicianCountryCodePhoneByDeviceId("d1")).isEmpty();
    }

    @Test
    void getAvailable_availableTechnician_isReturned() {
        when(technicianRepository.getAllTaggedTechnicianIds("d1")).thenReturn(List.of("t1"));
        TechnicianDTO dto = mock(TechnicianDTO.class);
        when(dto.getAvailability()).thenReturn("Available");
        when(technicianRepository.getTechnicianWithCountryCodePhoneAndAvailabilityById(eq("t1"), anyString()))
                .thenReturn(dto);

        assertThat(service.getAvailableTechnicianCountryCodePhoneByDeviceId("d1")).containsExactly(dto);
    }

    @Test
    void getAvailable_unavailableTechnician_isFilteredOut() {
        when(technicianRepository.getAllTaggedTechnicianIds("d1")).thenReturn(List.of("t1"));
        TechnicianDTO dto = mock(TechnicianDTO.class);
        when(dto.getAvailability()).thenReturn("Busy");
        when(technicianRepository.getTechnicianWithCountryCodePhoneAndAvailabilityById(eq("t1"), anyString()))
                .thenReturn(dto);

        assertThat(service.getAvailableTechnicianCountryCodePhoneByDeviceId("d1")).isEmpty();
    }
}
