package io.sclera.dto;

import java.util.List;

/** STUB: vendor transfer DTO */
public class VendorTransferDTO {
    private String vendor_organisation_id;
    private List<VendorDTO> vendors;

    public String getVendor_organisation_id() { return vendor_organisation_id; }
    public void setVendor_organisation_id(String vendor_organisation_id) { this.vendor_organisation_id = vendor_organisation_id; }
    public List<VendorDTO> getVendors() { return vendors; }
    public void setVendors(List<VendorDTO> vendors) { this.vendors = vendors; }
}
