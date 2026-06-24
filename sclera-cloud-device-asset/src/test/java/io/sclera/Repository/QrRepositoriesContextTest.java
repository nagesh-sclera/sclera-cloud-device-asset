package io.sclera.Repository;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QrRepositoriesContextTest {

    @Test
    void interfacesExtendJpaRepository() {
        assertThat(org.springframework.data.jpa.repository.JpaRepository.class
                .isAssignableFrom(QrCodeRepository.class)).isTrue();
        assertThat(org.springframework.data.jpa.repository.JpaRepository.class
                .isAssignableFrom(GlobalQrcodeRepository.class)).isTrue();
    }
}
