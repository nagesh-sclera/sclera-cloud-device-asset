package io.sclera.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@Table(indexes = {
        @Index(name = "idx_vua_user_access", columnList = "user_id, access_time, vdms_id")
})
@NoArgsConstructor
@AllArgsConstructor
public class VdmsUserActivity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(length = 64, nullable = false)
    private String user_id;

    @Column(length = 64, nullable = false)
    private String vdms_id;

    @Column(nullable = false)
    private Long access_time;
}
