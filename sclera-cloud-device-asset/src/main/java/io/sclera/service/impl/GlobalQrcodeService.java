package io.sclera.service.impl;
import io.sclera.service.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.uuid.Generators;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import io.sclera.Repository.GlobalQrcodeRepository;
import io.sclera.dto.GlobalQrcodeDTO;
import io.sclera.utils.QrImageStorageService;
import io.sclera.utils.ResourceUrlConfig;
import io.sclera.utils.RoundedCell;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Local QR-code service: generation (ZXing PNG → QrImageStorageService),
 * scan/lookup, iText PDF export, and delete with image cleanup.
 *
 * No cloud/sync code — all calls to APICallService, scheduled sync, and
 * WebSocket present in the edge reference have been intentionally omitted.
 *
 * Bean name is {@code globalQrcodeService} for autowiring by DeviceService /
 * LocationService.
 */
@Service("globalQrcodeService")
public class GlobalQrcodeService {

    @Autowired
    GlobalQrcodeRepository globalQrcodeRepository;

    @Autowired
    QrImageStorageService imageStorage;

    @Autowired
    ResourceUrlConfig resourceUrlConfig;

    @Autowired
    DeviceService deviceService;

    // --- package-visible constructor for unit tests (avoids reflection) ---
    GlobalQrcodeService(GlobalQrcodeRepository globalQrcodeRepository,
                        QrImageStorageService imageStorage,
                        ResourceUrlConfig resourceUrlConfig,
                        DeviceService deviceService) {
        this.globalQrcodeRepository = globalQrcodeRepository;
        this.imageStorage = imageStorage;
        this.resourceUrlConfig = resourceUrlConfig;
        this.deviceService = deviceService;
    }

    // Required by Spring (no-arg constructor for @Autowired field injection)
    public GlobalQrcodeService() {}

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Generates {@code count} generic (untagged) QR codes and persists them.
     * QR payload: {@code {base}/qrcode?vdms={vdmsid}&id={id}}
     */
    public void createGlobalQrcode(String username, String vdmsid, Integer count) {
        for (int i = 0; i < count; i++) {
            String global_qrcode_id = vdmsid + "_" + Generators.timeBasedGenerator().generate().toString();
            String image_url = this.generateGenericQrcode(global_qrcode_id, vdmsid);
            try {
                globalQrcodeRepository.upsertGlobalQrcode(global_qrcode_id, image_url, null, null);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public Set<GlobalQrcodeDTO> getGlobalQrCode(String username, String vdmsid, String qrcode_type,
                                                  String seachkey, Integer pageno, Integer pagesize,
                                                  JSONObject filterObject) {
        Integer offset = pagesize * (pageno - 1);

        JSONArray device_types_array = filterObject.getJSONArray("device_types");
        String building_id = filterObject.getString("building_id");
        String floor_id = filterObject.getString("floor_id");
        List<String> device_types = new ArrayList<>();

        try {
            if (qrcode_type.contains("location")) {
                if (building_id == null || floor_id == null) {
                    building_id = "all";
                    floor_id = "all";
                }
                return globalQrcodeRepository.getGlobalQrCodeLocation(seachkey, pagesize, offset, building_id, floor_id);
            } else if (qrcode_type.contains("device")) {
                if (device_types_array != null && device_types_array.size() > 0) {
                    ObjectMapper mapper = new ObjectMapper();
                    device_types = mapper.readValue(device_types_array.toJSONString(),
                            TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
                } else {
                    device_types.add("all");
                }
                return globalQrcodeRepository.getGlobalQrCodeDevice(seachkey, pagesize, offset, device_types);
            } else if (qrcode_type.contains("untagged")) {
                return globalQrcodeRepository.getUntaggedGlobalQrcodes(seachkey, pagesize, offset);
            } else if (qrcode_type.contains("all")) {
                return globalQrcodeRepository.getGlobalQrcodes(seachkey, pagesize, offset);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }

    /**
     * Upsert: create a new QR code if the DTO has no id, otherwise update the
     * existing record. Location and device QR counts are refreshed afterwards.
     */
    public void upsertGlobalQrcode(String username, String vdmsid, Set<GlobalQrcodeDTO> globalQrcodes) {
        Set<String> deviceIds = new HashSet<>();
        for (GlobalQrcodeDTO globalQrcode : globalQrcodes) {
            if (globalQrcode.getId() == null) {
                String global_qrcode_id = vdmsid + "_" + Generators.timeBasedGenerator().generate().toString();
                String image_url = this.generateGenericQrcode(global_qrcode_id, vdmsid);
                try {
                    globalQrcodeRepository.upsertGlobalQrcode(global_qrcode_id, image_url,
                            globalQrcode.getLocation_id(), globalQrcode.getDevice_id());
                } catch (Exception e) {
                    System.out.println(e);
                }
            } else {
                GlobalQrcodeDTO existing = globalQrcodeRepository.getGlobalQrcodeById(globalQrcode.getId());
                if (existing != null && existing.getImage_url() == null) {
                    globalQrcode.setImage_url(this.generateGenericQrcode(globalQrcode.getId(), vdmsid));
                }
                if (existing != null && existing.getDevice_id() != null) {
                    deviceIds.add(existing.getDevice_id());
                }
                globalQrcodeRepository.upsertGlobalQrcode(globalQrcode.getId(), globalQrcode.getImage_url(),
                        globalQrcode.getLocation_id(), globalQrcode.getDevice_id());
            }
            if (globalQrcode.getDevice_id() != null) {
                deviceIds.add(globalQrcode.getDevice_id());
            }
        }
        for (String deviceId : deviceIds) {
            try {
                deviceService.updateDeviceQrcodeCountByDeviceId(deviceId);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    /**
     * Delete the given QR code IDs, remove their stored images, and refresh
     * device qrcode_count for any linked devices.
     */
    public void deleteGlobalQrcode(String username, String vdmsid, Set<String> globalQrcodeIds) {
        Set<String> deviceIds = new HashSet<>();
        for (String globalQrCodeId : globalQrcodeIds) {
            GlobalQrcodeDTO globalQrcodeDTO = globalQrcodeRepository.getGlobalQrcodeById(globalQrCodeId);
            try {
                if (globalQrcodeDTO != null) {
                    imageStorage.delete(globalQrCodeId);
                }
            } catch (Exception e) {
                // best-effort image cleanup — do not abort the DB delete
            }
            if (globalQrcodeDTO != null && globalQrcodeDTO.getDevice_id() != null) {
                deviceIds.add(globalQrcodeDTO.getDevice_id());
            }
            globalQrcodeRepository.deleteGlobalQrcodeById(globalQrCodeId);
        }
        for (String deviceId : deviceIds) {
            try {
                deviceService.updateDeviceQrcodeCountByDeviceId(deviceId);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    /**
     * Export a PDF containing QR code images. If {@code qrcodes} is supplied the
     * listed ids are exported; otherwise all codes matching {@code type} and the
     * filter lists are exported.
     */
    public void exportGlobalQrCodes(HttpServletResponse response, String username, String vdmsid,
                                    List<String> qrcodes, String type, List<String> dockernames,
                                    List<String> device_types, List<String> building_ids,
                                    List<String> floor_ids, Integer width, Integer height) {
        response.setContentType("application/pdf");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String currentDateTime = dateFormatter.format(new Date());
        response.setHeader("Content-Disposition", "attachment; filename=qrcodes_" + currentDateTime + ".pdf");
        this.getQrcodeDetails(response, qrcodes, type, dockernames, device_types, building_ids, floor_ids, width, height);
    }

    public List<GlobalQrcodeDTO> getQrcodeDetail(String username, String vdmsid, GlobalQrcodeDTO globalQrcode) {
        if (globalQrcode.getId() == null) {
            globalQrcode.setId("null");
        }
        if (globalQrcode.getLocation_id() == null) {
            globalQrcode.setLocation_id("null");
        }
        if (globalQrcode.getDevice_id() == null) {
            globalQrcode.setDevice_id("null");
        }
        System.out.println(globalQrcode.getId() + globalQrcode.getDevice_id() + globalQrcode.getLocation_id());
        return globalQrcodeRepository.getQrcodeDetail(globalQrcode.getId(), globalQrcode.getLocation_id(), globalQrcode.getDevice_id());
    }

    public Integer getDeviceQrcodeCountByDeviceId(String device_id) {
        return globalQrcodeRepository.getDeviceQrcodeCountByDeviceId(device_id);
    }

    /**
     * Delete all QR codes linked to a location, cleaning up stored images.
     * Called by LocationService when a location is removed.
     * Cloud-sync that existed in the edge reference is NOT ported here.
     */
    public void deleteGlobalQRCodeByLocationId(String location_id) {
        String global_qrcode_id = globalQrcodeRepository.getGlobalQrcodeIdByLocationId(location_id);
        if (global_qrcode_id != null) {
            try {
                imageStorage.delete(global_qrcode_id);
            } catch (Exception e) {
                // best-effort
            }
            globalQrcodeRepository.deleteById(global_qrcode_id);
        }
    }

    /**
     * Upsert QR codes driven by qrcode_type ("location" | "device") embedded in
     * each DTO. Used by admin controllers.
     * Kept under the original edge name {@code addGlobalQrcode} for callers that
     * already reference it; the brief's public surface name is
     * {@link #upsertGlobalQrcode}.
     */
    public void addGlobalQrcode(String username, String vdmsid, Set<GlobalQrcodeDTO> globalQrcodes) {
        for (GlobalQrcodeDTO globalQrcode : globalQrcodes) {
            if (globalQrcode.getQrcode_type() != null && globalQrcode.getQrcode_type().contains("location")) {
                String existingId = globalQrcodeRepository.getGlobalQrcodeIdByLocation(globalQrcode.getLocation_id());
                if (existingId == null) {
                    String global_qrcode_id = vdmsid + "_" + Generators.timeBasedGenerator().generate().toString();
                    String image_url = this.generateLocationQrcode(global_qrcode_id, vdmsid, globalQrcode.getLocation_id());
                    try {
                        globalQrcodeRepository.addGlobalQrcode(global_qrcode_id, image_url,
                                globalQrcode.getLocation_id(), globalQrcode.getDevice_id());
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            } else if (globalQrcode.getQrcode_type() != null && globalQrcode.getQrcode_type().contains("device")) {
                String existingId = globalQrcodeRepository.getGlobalQrcodeIdByDevice(globalQrcode.getDevice_id());
                if (existingId == null) {
                    String global_qrcode_id = vdmsid + "_" + Generators.timeBasedGenerator().generate().toString();
                    String image_url = this.generateDeviceQrcode(global_qrcode_id, vdmsid,
                            globalQrcode.getDocker_name(), globalQrcode.getDevice_id());
                    try {
                        globalQrcodeRepository.addGlobalQrcode(global_qrcode_id, image_url,
                                globalQrcode.getLocation_id(), globalQrcode.getDevice_id());
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
        }
    }

    // =========================================================================
    // Private — QR generation
    // =========================================================================

    /**
     * Generic/untagged QR code.
     * Payload: {@code {base}/qrcode?vdms={vdmsid}&id={id}}
     * Uses ErrorCorrectionLevel.H and 500×500 as required.
     * Image stored via {@link QrImageStorageService#store(byte[], String, String)}.
     */
    private String generateGenericQrcode(String global_qrcode_id, String vdmsid) {
        String base = resourceUrlConfig.getGlobal_qrcode_server_url();
        String data = base + "/qrcode?vdms=" + vdmsid + "&id=" + global_qrcode_id;
        return encodeAndStore(data, global_qrcode_id);
    }

    /**
     * Location QR code.
     * Payload: {@code {base}/qrcode?vdms={vdmsid}&type=location&location_id={locationId}}
     */
    private String generateLocationQrcode(String global_qrcode_id, String vdmsid, String location_id) {
        String base = resourceUrlConfig.getGlobal_qrcode_server_url();
        String data = base + "/qrcode?vdms=" + vdmsid + "&type=location&location_id=" + location_id;
        return encodeAndStore(data, global_qrcode_id);
    }

    /**
     * Device QR code.
     * Payload: {@code {base}/qrcode?vdms={vdmsid}&type=device&docker_name={dockerName}&device_id={deviceId}}
     */
    private String generateDeviceQrcode(String global_qrcode_id, String vdmsid, String docker_name, String device_id) {
        String base = resourceUrlConfig.getGlobal_qrcode_server_url();
        String data = base + "/qrcode?vdms=" + vdmsid + "&type=device&docker_name=" + docker_name + "&device_id=" + device_id;
        return encodeAndStore(data, global_qrcode_id);
    }

    /**
     * Shared ZXing encode + storage.
     * Keeps the exact writer/size/error-correction from the edge reference.
     */
    private String encodeAndStore(String data, String id) {
        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 500, 500, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            byte[] bytes = toByteArray(image, "png");
            return imageStorage.store(bytes, id, "png");
        } catch (IOException | WriterException e) {
            System.out.println(e);
            return null;
        }
    }

    // =========================================================================
    // Private — PDF export helpers
    // =========================================================================

    private void getQrcodeDetails(HttpServletResponse response, List<String> globalQrcodes, String type,
                                  List<String> dockernames, List<String> device_types,
                                  List<String> building_ids, List<String> floor_ids,
                                  Integer width, Integer height) {
        List<GlobalQrcodeDTO> qrcodes = new ArrayList<>();
        if (globalQrcodes != null && !globalQrcodes.isEmpty()) {
            qrcodes = globalQrcodeRepository.getGlobalQrCodesByIds(globalQrcodes);
        } else {
            if (type != null) {
                if (type.equals("all")) {
                    qrcodes = globalQrcodeRepository.getGlobalQrcodeDetails();
                } else if (type.equals("device")) {
                    qrcodes = globalQrcodeRepository.getGlobalQrcodeDeviceDetails(dockernames, device_types);
                } else if (type.equals("location")) {
                    qrcodes = globalQrcodeRepository.getGlobalQrcodeLocationDetails(building_ids, floor_ids);
                } else if (type.equals("untagged")) {
                    qrcodes = globalQrcodeRepository.getUntaggedGlobalQrcodeDetails();
                }
            }
        }
        if (qrcodes != null && !qrcodes.isEmpty()) {
            this.exportGlobalQrCodeToPDF(response, qrcodes, width, height);
        }
    }

    /**
     * Render QR codes to PDF using iText.
     *
     * Logo files expected at classpath:images/jll_logo_latest.png and
     * classpath:images/powered_by_update.png — ported verbatim from edge.
     * If either resource is absent from device-asset resources the corresponding
     * {@code addTableCellContents} call will catch the IOException and print it,
     * leaving that cell empty rather than aborting the export.
     *
     * QR image bytes are fetched from {@link QrImageStorageService#fetch(String)}
     * (replaces the edge's direct URL load via Image.getInstance(url)).
     */
    private void exportGlobalQrCodeToPDF(HttpServletResponse response, List<GlobalQrcodeDTO> globalQrcodes,
                                         Integer width, Integer height) {
        try {
            System.out.println("size  : " + globalQrcodes.size());
            float newWidth, newHeight, newPoint;
            int extraWidth = 1;

            if (width.equals(height)) {
                newPoint = height;
            } else {
                newPoint = 1 + height / 2f;
            }

            if (width > 5) {
                extraWidth = width / 2;
            }
            newWidth = (width * 72f) - (32f * extraWidth);
            newHeight = height * 72f;

            Document document = new Document(new Rectangle(newWidth, newHeight));
            PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            document.setMargins(0, 0, 0, 0);

            PdfPTable outerTable = new PdfPTable(1);
            outerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            outerTable.setTotalWidth(document.getPageSize().getWidth());
            outerTable.setHorizontalAlignment(Element.ALIGN_CENTER);

            for (GlobalQrcodeDTO globalQrcode : globalQrcodes) {
                PdfPTable details_table = new PdfPTable(1);
                details_table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                details_table.setHorizontalAlignment(Element.ALIGN_CENTER);

                try {
                    details_table.addCell(addTableCellContents(12 * newPoint, "images/jll_logo_latest.png"));

                    // Fetch QR image bytes from storage instead of loading from URL
                    byte[] qrBytes = imageStorage.fetch(globalQrcode.getId());
                    PdfPCell imageCell;
                    if (qrBytes != null) {
                        Image img = Image.getInstance(qrBytes);
                        img.scaleToFit(newWidth, 47 * newPoint);
                        imageCell = new PdfPCell(img, true);
                        imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        imageCell.setBorder(0);
                    } else {
                        // Fallback: try loading by URL if fetch returns null
                        imageCell = createImageCell(globalQrcode.getImage_url());
                    }
                    imageCell.setFixedHeight(47 * newPoint);
                    imageCell.setPaddingBottom(4 + extraWidth);
                    imageCell.setPaddingTop(1);
                    details_table.addCell(imageCell);

                    details_table.addCell(addTableCellContents(12 * newPoint, "images/powered_by_update.png"));

                    outerTable.addCell(details_table);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }

            outerTable.completeRow();
            System.out.println("rows : " + outerTable.getRows().size());
            for (int i = 0; i < outerTable.getRows().size(); i++) {
                document.newPage();
                outerTable.writeSelectedRows(i, i + 1, document.left(), document.top(), writer.getDirectContent());
            }
            document.close();
        } catch (DocumentException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Builds a single-cell table containing a logo loaded from the classpath.
     * If the resource is absent the cell is returned empty (exception printed).
     * NOTE: {@code images/jll_logo_latest.png} and {@code images/powered_by_update.png}
     * must exist under {@code src/main/resources/images/} for logos to render.
     */
    private PdfPTable addTableCellContents(float newPoint, String classpathImagePath) {
        PdfPTable inside_table = new PdfPTable(1);
        inside_table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        try {
            java.net.URL resource = getClass().getClassLoader().getResource(classpathImagePath);
            if (resource != null) {
                PdfPCell cell = new PdfPCell(this.createImageCell(resource.toString()));
                cell.setFixedHeight(newPoint);
                cell.setPadding(-4);
                cell.setBorder(PdfPCell.NO_BORDER);
                inside_table.addCell(cell);
            } else {
                System.out.println("Logo not found on classpath: " + classpathImagePath);
                PdfPCell empty = new PdfPCell();
                empty.setFixedHeight(newPoint);
                empty.setBorder(PdfPCell.NO_BORDER);
                inside_table.addCell(empty);
            }
        } catch (DocumentException | IOException e) {
            System.out.println(e);
        }
        return inside_table;
    }

    public PdfPCell getCell(PdfPTable table) {
        PdfPCell cell = new PdfPCell(table);
        cell.setCellEvent(new RoundedCell());
        cell.setPadding(5);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    public PdfPCell createImageCell(String path) throws DocumentException, IOException {
        Image img = Image.getInstance(path);
        PdfPCell cell = new PdfPCell(img, true);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(0);
        return cell;
    }

    // =========================================================================
    // Utility
    // =========================================================================

    public static byte[] toByteArray(BufferedImage bi, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, format, baos);
        return baos.toByteArray();
    }
}
