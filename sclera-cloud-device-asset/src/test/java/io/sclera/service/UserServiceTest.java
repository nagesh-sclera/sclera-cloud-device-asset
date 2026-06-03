package io.sclera.service;

import io.sclera.Repository.UserRepository;
import io.sclera.Repository.VdmsRepository;
import io.sclera.client.APICallClient;
import io.sclera.dto.touchscreen.settings.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for UserService: pagination offset arithmetic, getUserbyOrgId update/insert
 * branching, addUsers default-language handling, getUserDetailsByEmail matching, and delegates.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock APICallClient apicallService;
    @Mock VdmsRepository vdmsRepository;

    @InjectMocks UserService service;

    // ---- delegates -------------------------------------------------------

    @Test
    void getOrganisationIdByUserEmail_delegates() {
        when(userRepository.getOrganisationIdByUserEmail("a@x.com")).thenReturn("org1");
        assertThat(service.getOrganisationIdByUserEmail("a@x.com")).isEqualTo("org1");
    }

    @Test
    void getCustomerOrgIdByVdmsId_delegates() {
        when(vdmsRepository.getCustomerOrgIdByVdmsId("v1")).thenReturn("corg1");
        assertThat(service.getCustomerOrgIdByVdmsId("v1")).isEqualTo("corg1");
    }

    @Test
    void checkUserByEmail_delegates() {
        when(userRepository.checkUserByEmail("a@x.com")).thenReturn(1);
        assertThat(service.checkUserByEmail("a@x.com")).isEqualTo(1);
    }

    // ---- pagination offset -----------------------------------------------

    @Test
    void getAllUsers_computesOffset() {
        Set<UserDTO> users = Set.of(mock(UserDTO.class));
        // pageno=3, pagesize=10 -> offset 20
        when(userRepository.getAllUsers(10, 20, "key")).thenReturn(users);
        assertThat(service.getAllUsers("u", "v", 3, 10, "key")).isSameAs(users);
    }

    @Test
    void getAllOrganisationUsersByPagination_resolvesOrgAndOffset() {
        when(vdmsRepository.getCustomerOrgIdByVdmsId("v1")).thenReturn("corg1");
        Set<UserDTO> users = Set.of(mock(UserDTO.class));
        // pageno=2, pagesize=5 -> offset 5
        when(userRepository.getAllOrganisationUsersByPagination(5, 5, "key", "corg1")).thenReturn(users);
        assertThat(service.getAllOrganisationUsersByPagination("u", "v1", 2, 5, "key")).isSameAs(users);
    }

    // ---- getUserbyOrgId branches -----------------------------------------

    @Test
    void getUserbyOrgId_existingUser_updates() {
        UserDTO u = mock(UserDTO.class);
        when(u.getEmail()).thenReturn("a@x.com");
        when(u.getOrganisation_id()).thenReturn("org1");
        when(apicallService.getUsersByOrgId("org1", "v1")).thenReturn(List.of(u));
        when(userRepository.checkUser("a@x.com", "org1")).thenReturn(1);

        service.getUserbyOrgId("org1", "v1");

        verify(userRepository).updateUser(any(), any(), any(), any(), any(), any(), any(), eq("org1"), eq("a@x.com"));
        verify(userRepository, never()).insertUser(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void getUserbyOrgId_newUser_inserts() {
        UserDTO u = mock(UserDTO.class);
        when(u.getEmail()).thenReturn("a@x.com");
        when(u.getOrganisation_id()).thenReturn("org1");
        when(apicallService.getUsersByOrgId("org1", "v1")).thenReturn(List.of(u));
        when(userRepository.checkUser("a@x.com", "org1")).thenReturn(0);

        service.getUserbyOrgId("org1", "v1");

        verify(userRepository).insertUser(eq("a@x.com"), any(), any(), any(), any(), any(), any(), any(), any(), eq("org1"), any(), any(), any());
    }

    // ---- addUsers --------------------------------------------------------

    @Test
    void addUsers_nullLanguage_defaultsToEN() {
        UserDTO u = mock(UserDTO.class);
        when(u.getLanguage()).thenReturn(null);

        UserDTO result = service.addUsers("u", "v", u);

        assertThat(result).isSameAs(u);
        verify(u).setLanguage("EN");
        verify(u).setCreation_timestamp(any());
    }

    @Test
    void addUsers_existingLanguage_keepsIt() {
        UserDTO u = mock(UserDTO.class);
        when(u.getLanguage()).thenReturn("FR");

        service.addUsers("u", "v", u);

        verify(u, never()).setLanguage(any());
    }

    // ---- getUserDetailsByEmail -------------------------------------------

    @Test
    void getUserDetailsByEmail_match_returnsUser() {
        UserDTO a = mock(UserDTO.class);
        when(a.getEmail()).thenReturn("a@x.com");
        UserDTO b = mock(UserDTO.class);
        when(b.getEmail()).thenReturn("b@x.com");

        assertThat(service.getUserDetailsByEmail("b@x.com", Set.of(a, b))).isSameAs(b);
    }

    @Test
    void getUserDetailsByEmail_noMatch_returnsNull() {
        UserDTO a = mock(UserDTO.class);
        when(a.getEmail()).thenReturn("a@x.com");
        assertThat(service.getUserDetailsByEmail("z@x.com", Set.of(a))).isNull();
    }

    // ---- delete / edit loops ---------------------------------------------

    @Test
    void deleteUsers_deletesEachEmail() {
        service.deleteUsers("u", "v", new java.util.LinkedHashSet<>(List.of("a@x.com", "b@x.com")));
        verify(userRepository).deleteById("a@x.com");
        verify(userRepository).deleteById("b@x.com");
    }
}
