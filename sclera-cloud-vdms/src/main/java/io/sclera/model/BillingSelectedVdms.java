package io.sclera.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "billing_selected_vdms")
public class BillingSelectedVdms {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "billing_id", nullable = false)
    private BillingInfo billingInfo;

    @Column(name = "vdms_id", nullable = false)
    private String vdmsId;

}
