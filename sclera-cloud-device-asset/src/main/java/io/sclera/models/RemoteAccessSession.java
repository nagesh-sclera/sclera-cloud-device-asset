package io.sclera.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Represents a remote-access session opened against a managed device, linking the session to its target {@link Device}.
 */
@Entity
public class RemoteAccessSession {
    @Id
    private Long id;

    @ManyToOne
    private Device device;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }
}