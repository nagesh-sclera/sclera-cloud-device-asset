package io.sclera.interfaces;

/** Service contract for the matching service class. */
public interface AsyncServiceInterface {
    void updateVendorByMacAddress(String mac, String vendor);
}
