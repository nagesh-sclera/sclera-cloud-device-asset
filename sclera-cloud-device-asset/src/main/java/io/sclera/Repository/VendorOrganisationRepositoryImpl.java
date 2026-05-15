package io.sclera.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
@Repository
@Primary
public class VendorOrganisationRepositoryImpl implements VendorOrganisationRepository {
    private static final Logger log = LoggerFactory.getLogger(VendorOrganisationRepositoryImpl.class);
    @Override public void addVendor(String vendorOrgId, String vdmsId) { log.warn("[STUB] VendorOrganisationRepository.addVendor"); }
}
