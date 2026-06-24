package io.sclera.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class TrustedOrigin {

    @Id
    @Column(length = 128)
    private String origin;

}
