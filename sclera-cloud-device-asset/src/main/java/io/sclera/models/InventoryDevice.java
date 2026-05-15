package io.sclera.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;


@Getter
@Setter
@Entity
public class InventoryDevice {

    @Id
    private String tracking_id;

    @OneToOne
    private Device device;

}
