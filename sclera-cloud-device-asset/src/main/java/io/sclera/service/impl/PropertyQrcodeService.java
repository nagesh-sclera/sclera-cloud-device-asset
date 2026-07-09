package io.sclera.service.impl;
import io.sclera.service.*;

import com.fasterxml.uuid.Generators;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import io.sclera.Repository.PropertyQrCodeRepository;
import io.sclera.Repository.PropertyServiceRepository;
import io.sclera.Repository.PropertyServiceRequestRepository;
import io.sclera.Repository.PropertyServiceResponseRepository;
import io.sclera.dto.LocationDTO;
import io.sclera.dto.PropertyQrcodeDTO;
import io.sclera.dto.PropertyServiceDTO;
import io.sclera.dto.PropertyServiceRequestDTO;
import io.sclera.dto.PropertyServiceResponseDTO;
import io.sclera.utils.QrImageStorageService;
import io.sclera.utils.ResourceUrlConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * Local-only property QR-code service for sclera-cloud-device-asset.
 *
 * <p>Ported from sclera-vdms-edge-server {@code PropertyQrcodeService}.
 * All cloud-sync calls (APICallService upsert/add/delete Cloud variants) and the
 * cloud WebSocket push (SocketService.socketPropertyServiceValueUpdate) have
 * been removed. Image persistence is delegated to {@link QrImageStorageService}
 * instead of writing directly to the filesystem.
 */
@Service("propertyQrcodeService")
public class PropertyQrcodeService {

    @Autowired
    PropertyServiceRepository propertyServiceRepository;

    @Autowired
    PropertyQrCodeRepository propertyQrCodeRepository;

    @Autowired
    PropertyServiceRequestRepository propertyServiceRequestRepository;

    @Autowired
    PropertyServiceResponseRepository propertyServiceResponseRepository;

    @Autowired
    QrImageStorageService imageStorage;

    @Autowired
    ResourceUrlConfig resourceUrlConfig;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Cascade-cleanup when a location is deleted: removes every QR code that
     * was bound to that location (called by LocationService).
     */
    public void updatePropertyServiceLocations(String location_id) {
        Set<String> property_service_ids =
                propertyQrCodeRepository.getPropertyServicesByLocationId(location_id);

        for (String property_service_id : property_service_ids) {
            this.deletePropertyServiceLocation(property_service_id, location_id);
        }
    }

    /**
     * Create or update a property service together with its request fields.
     * Uniqueness of the service name is enforced within the same vdms_id scope.
     *
     * @return the persisted {@link PropertyServiceDTO}, or {@code null} if the
     *         name collides with an existing service.
     */
    public PropertyServiceDTO upsertPropertyServiceDetails(String username, String vdmsid,
                                                           PropertyServiceDTO propertyService) {
        try {
            // Enforce unique name within this vdms scope
            Set<PropertyServiceDTO> existing = this.getPropertyServices(username, vdmsid);
            for (PropertyServiceDTO existingService : existing) {
                if (!existingService.getId().equals(propertyService.getId())) {
                    if (existingService.getName().equalsIgnoreCase(propertyService.getName())) {
                        return null;
                    }
                }
            }

            // Assign a new ID if this is a create
            if (propertyService.getId() == null) {
                String id = vdmsid + "_" + Generators.timeBasedGenerator().generate().toString();
                propertyService.setId(id);
            }

            this.upsertPropertyService(vdmsid, propertyService);

            // Upsert each request field
            for (PropertyServiceRequestDTO request : propertyService.getProperty_service_requests()) {
                if (request.getId() == null) {
                    String id = vdmsid + "_" + Generators.timeBasedGenerator().generate().toString();
                    request.setId(id);
                }
                this.upsertPropertyServiceRequest(propertyService.getId(), request);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        // Build response with requests + any responses already on linked locations
        PropertyServiceDTO response =
                this.getPropertyServicesById(username, vdmsid, propertyService.getId());
        if (response != null) {
            response.setProperty_service_requests(
                    this.getPropertyServiceRequestsByServiceId(propertyService.getId()));

            Set<PropertyQrcodeDTO> propertyQrcodes =
                    this.getPropertyServiceLocations(propertyService.getId());

            if (propertyQrcodes != null) {
                for (PropertyQrcodeDTO qrcode : propertyQrcodes) {
                    this.addPropertyServiceResponses(vdmsid, propertyService.getId(), qrcode.getId());
                }
            }
        }
        return response;
    }

    /**
     * Attach a set of locations to a property service, generating a QR code for
     * each location and initialising the response rows.
     */
    public void addPropertyServiceLocations(String username, String vdmsid,
                                             String property_service_id,
                                             Set<LocationDTO> locationDTOS) {
        try {
            for (LocationDTO locationDTO : locationDTOS) {
                String location_id = locationDTO.getLocation_id();

                String property_qrcode_id =
                        vdmsid + "_" + Generators.timeBasedGenerator().generate().toString();

                // Generate and store QR image
                String image_url = this.generateQrcode(property_qrcode_id);

                // Persist the QR-code row
                this.addPropertyQrcode(property_qrcode_id, image_url,
                        property_service_id, location_id);

                // Initialise response rows for this QR code
                this.addPropertyServiceResponses(vdmsid, property_service_id, property_qrcode_id);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Generate a ZXing QR code image (500x500 JPEG) encoding the property-scan
     * URL, persist it via {@link QrImageStorageService}, and return the public URL.
     */
    public String generateQrcode(String property_qrcode_id) {
        String base = resourceUrlConfig.getServices_cloud_server_url();
        String data = base + "/services?property_qrcode_id=" + property_qrcode_id;
        try {
            BitMatrix matrix =
                    new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 500, 500);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            byte[] bytes = toByteArray(image, "jpeg");
            return imageStorage.store(bytes, property_qrcode_id, "jpeg");
        } catch (IOException | WriterException e) {
            System.out.println(e);
            return null;
        }
    }

    /**
     * Bulk-update a set of property service responses (e.g. values captured
     * after scanning a QR code). Timestamps are refreshed to now.
     */
    public void multiUpdatePropertyServiceResponse(String username, String vdmsid,
                                                    Set<PropertyServiceResponseDTO> propertyServiceResponses) {
        for (PropertyServiceResponseDTO response : propertyServiceResponses) {
            BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
            response.setTimestamp(timestamp);
            this.updatePropertyServiceResponse(response.getProperty_qrcode_id(), response);
        }
    }

    /**
     * Return all QR-code/location entries bound to a given property service.
     */
    public Set<PropertyQrcodeDTO> getPropertyServiceLocationsById(String username, String vdmsid,
                                                                   String property_service_id) {
        return this.getPropertyServiceLocations(property_service_id);
    }

    /**
     * Delete a set of property service request fields (and their upstream cloud
     * references — cloud calls removed; local delete only).
     */
    public void deletePropertyServiceRequests(String username, String vdmsid,
                                               Set<PropertyServiceRequestDTO> propertyServiceRequests) {
        for (PropertyServiceRequestDTO request : propertyServiceRequests) {
            propertyServiceRequestRepository.deleteById(request.getId());
        }
    }

    /**
     * Remove the QR-code entries (and stored images) for the given locations
     * under a property service.
     */
    public void deletePropertyServiceLocations(String username, String vdmsid,
                                                String property_service_id,
                                                Set<String> locations) {
        for (String location_id : locations) {
            this.deletePropertyServiceLocation(property_service_id, location_id);
        }
    }

    /**
     * Delete a property service and the QR images for all locations bound to it.
     */
    public void deletePropertyService(String username, String vdmsid, String property_service_id) {
        Set<PropertyQrcodeDTO> propertyQrcodes =
                this.getPropertyServiceLocations(property_service_id);
        propertyServiceRepository.deleteById(property_service_id);
        if (propertyQrcodes != null) {
            for (PropertyQrcodeDTO qrcode : propertyQrcodes) {
                imageStorage.delete(qrcode.getId());
            }
        }
    }

    /**
     * Return QR codes for the zone map filtered by building / floor / location /
     * service, with their response values populated.
     */
    public Set<PropertyQrcodeDTO> getZoneMap(String username, String vdmsid,
                                              String building_id, String floor_id,
                                              String location_id, String property_service_id) {
        return this.getPropertyQrcodesByIds(building_id, floor_id, location_id, property_service_id);
    }

    // -------------------------------------------------------------------------
    // Package-visible helpers (accessible by tests)
    // -------------------------------------------------------------------------

    public Set<PropertyServiceDTO> getPropertyServices(String username, String vdmsid) {
        Set<PropertyServiceDTO> propertyServices = propertyServiceRepository.getPropertyServices();
        for (PropertyServiceDTO service : propertyServices) {
            service.setProperty_service_requests(
                    this.getPropertyServiceRequestsByServiceId(service.getId()));
        }
        return propertyServices;
    }

    void updatePropertyServiceResponse(String property_qrcode_id,
                                        PropertyServiceResponseDTO response) {
        propertyServiceResponseRepository.updatePropertyServiceResponse(
                property_qrcode_id,
                response.getProperty_service_request_id(),
                response.getValue(),
                response.getAlert(),
                response.getTimestamp());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void upsertPropertyService(String vdmsid, PropertyServiceDTO propertyService) {
        propertyServiceRepository.upsertPropertyService(
                propertyService.getId(), propertyService.getName(), vdmsid);
    }

    private void upsertPropertyServiceRequest(String property_service_id,
                                               PropertyServiceRequestDTO request) {
        propertyServiceRequestRepository.upsertPropertyServiceRequest(
                request.getId(), request.getLabel(), request.getOptions(),
                request.getType(), property_service_id);
    }

    private PropertyServiceDTO getPropertyServicesById(String username, String vdmsid, String id) {
        return propertyServiceRepository.getPropertyServicesById(vdmsid, id);
    }

    private Set<PropertyQrcodeDTO> getPropertyServiceLocations(String property_service_id) {
        return propertyQrCodeRepository.getPropertyServiceLocations(property_service_id);
    }

    private void addPropertyQrcode(String id, String image_url,
                                    String property_service_id, String location_id) {
        propertyQrCodeRepository.addPropertyQrcode(id, image_url, property_service_id, location_id);
    }

    private Set<PropertyServiceRequestDTO> getPropertyServiceRequestsByServiceId(
            String property_service_id) {
        return propertyServiceRequestRepository
                .getPropertyServiceRequestsByServiceId(property_service_id);
    }

    private Set<PropertyServiceResponseDTO> addPropertyServiceResponses(String vdmsid,
                                                                         String property_service_id,
                                                                         String property_qrcode_id) {
        Set<PropertyServiceResponseDTO> added = new HashSet<>();
        Set<PropertyServiceRequestDTO> requests =
                this.getPropertyServiceRequestsByServiceId(property_service_id);

        for (PropertyServiceRequestDTO request : requests) {
            Integer count = propertyServiceResponseRepository
                    .getPropertyServiceResponseCount(property_qrcode_id, request.getId());

            if (count == null || count == 0) {
                String id = vdmsid + "_" + Generators.timeBasedGenerator().generate().toString();
                BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());

                propertyServiceResponseRepository.addPropertyServiceResponse(
                        id, null, timestamp, property_qrcode_id, request.getId());

                added.add(new PropertyServiceResponseDTO(
                        id, false, timestamp, null, property_qrcode_id, request.getId()));
            }
        }
        return added;
    }

    private void deletePropertyServiceLocation(String property_service_id, String location_id) {
        PropertyQrcodeDTO qrcode =
                propertyQrCodeRepository.getPropertyQrcode(property_service_id, location_id);
        if (qrcode != null) {
            propertyQrCodeRepository.deleteById(qrcode.getId());
            imageStorage.delete(qrcode.getId());
        }
    }

    private Set<PropertyQrcodeDTO> getPropertyQrcodesByIds(String building_id, String floor_id,
                                                             String location_id,
                                                             String property_service_id) {
        Set<PropertyQrcodeDTO> qrcodes = propertyQrCodeRepository
                .getPropertyQrcodeByFloor(building_id, floor_id, location_id, property_service_id);

        for (PropertyQrcodeDTO qrcode : qrcodes) {
            Set<PropertyServiceResponseDTO> responses =
                    propertyServiceResponseRepository.getPropertyServiceRequestResponsesByIds(
                            property_service_id, qrcode.getId());
            qrcode.setProperty_service_responses(responses);
        }
        return qrcodes;
    }

    // -------------------------------------------------------------------------
    // Image utility
    // -------------------------------------------------------------------------

    private static byte[] toByteArray(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }
}
