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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Additional delegate coverage for UserService beyond the main test: the repository pass-throughs,
 * offset pagination, remote-API user lookup, and the insert/edit/update field-mapping calls.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceMoreTest {

    @Mock UserRepository userRepository;
    @Mock APICallClient apicallService;
    @Mock VdmsRepository vdmsRepository;

    @InjectMocks UserService service;

    @Test
    void getUserByEmail_delegates() {
        UserDTO u = mock(UserDTO.class);
        when(userRepository.getUserByEmail("a@b.com")).thenReturn(u);
        assertThat(service.getUserByEmail("a@b.com")).isSameAs(u);
    }

    @Test
    void getAllUsersEmail_delegates() {
        Set<String> emails = Set.of("a@b.com");
        when(userRepository.getAllUsersEmail()).thenReturn(emails);
        assertThat(service.getAllUsersEmail()).isSameAs(emails);
    }

    @Test
    void getAllUsersByOrganisationId_delegates() {
        List<UserDTO> users = List.of(mock(UserDTO.class));
        when(userRepository.getAllUsersByOrganisationId("org1")).thenReturn(users);
        assertThat(service.getAllUsersByOrganisationId("org1")).isSameAs(users);
    }

    @Test
    void getUsers_delegates() {
        Set<UserDTO> users = Set.of(mock(UserDTO.class));
        when(userRepository.getUsers()).thenReturn(users);
        assertThat(service.getUsers()).isSameAs(users);
    }

    @Test
    void getUserNameByEmail_delegates() {
        when(userRepository.getUserNameByEmail("a@b.com")).thenReturn("Alice");
        assertThat(service.getUserNameByEmail("a@b.com")).isEqualTo("Alice");
    }

    @Test
    void getAllUserRoles_delegates() {
        when(userRepository.getAllUserRoles("a@b.com")).thenReturn("admin");
        assertThat(service.getAllUserRoles("a@b.com")).isEqualTo("admin");
    }

    @Test
    void getAllUserInfoByOrganisationIdAndVdmsId_delegatesToApi() {
        List<UserDTO> users = List.of(mock(UserDTO.class));
        when(apicallService.getAllUserInfoByOrganisationIdAndVdmsId("org1", "v1")).thenReturn(users);
        assertThat(service.getAllUserInfoByOrganisationIdAndVdmsId("org1", "v1")).isSameAs(users);
    }

    @Test
    void getAllOtherUsersByPagination_computesOffset() {
        Set<UserDTO> users = Set.of(mock(UserDTO.class));
        // pageno=2, pagesize=10 -> offset 10
        when(userRepository.getAllOtherUsersByPagination(10, 10, "key")).thenReturn(users);
        assertThat(service.getAllOtherUsersByPagination("u", "v", 2, 10, "key")).isSameAs(users);
    }

    @Test
    void deleteUserByEmailId_delegates() {
        service.deleteUserByEmailId("a@b.com");
        verify(userRepository).deleteById("a@b.com");
    }

    @Test
    void deleteUsersByOrganisationId_delegates() {
        service.deleteUsersByOrganisationId("org1");
        verify(userRepository).deleteUsersByOrganisationId("org1");
    }

    @Test
    void updateCustomerOrgIdForUsers_delegates() {
        service.updateCustomerOrgIdForUsers("old", "new");
        verify(userRepository).updateCustomerOrgIdForUsers("old", "new");
    }

    @Test
    void deleteUsers_deletesEachEmail() {
        service.deleteUsers("u", "v", new java.util.LinkedHashSet<>(List.of("a@b.com", "c@d.com")));
        verify(userRepository).deleteById("a@b.com");
        verify(userRepository).deleteById("c@d.com");
    }

    @Test
    void insertUsers_mapsFieldsToRepository() {
        service.insertUsers(mock(UserDTO.class));
        verify(userRepository).insertUser(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any()); // 13 fields
    }

    @Test
    void updateAllUser_mapsFieldsToRepository() {
        service.updateAllUser(mock(UserDTO.class));
        verify(userRepository).updateAllUser(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any()); // 13 fields
    }

    @Test
    void editUsers_mapsFieldsPerUser() {
        service.editUsers("u", "v", Set.of(mock(UserDTO.class)));
        verify(userRepository).editUsers(any(), any(), any(), any(), any(), any(), any(), any()); // 8 fields
    }
}
