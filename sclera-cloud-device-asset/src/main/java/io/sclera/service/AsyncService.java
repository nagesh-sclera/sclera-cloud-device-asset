package io.sclera.service;

/** Service contract for the matching service class. */
public interface AsyncService {
    void updateVendorByMacAddress(String mac, String vendor);
}
