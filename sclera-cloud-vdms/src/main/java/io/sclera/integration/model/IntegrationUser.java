package io.sclera.integration.model;

import io.sclera.model.Customer_Organisation;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
public class IntegrationUser {

    @Id
    @Column(length = 64)
    private String username;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id", name = "customer_org_id")
    private Customer_Organisation customer_org;

    private String role;

}
