package io.sclera.service;

import com.fasterxml.uuid.Generators;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.alibaba.fastjson.JSONArray;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import io.sclera.Repository.QrCodeRepository;
import io.sclera.dto.QrCodeDTO;
import io.sclera.queryrepository.QrCodeQueryRepository;
import io.sclera.utils.QrImageStorageService;
import io.sclera.utils.ResourceUrlConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.sql.DataSource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Manages QR code generation, bulk export, tagging, and local persistence.
 * Cloud-sync (WebSocket/WebClient), multi-tenant dispatch, and AWS-presigned
 * URL generation are intentionally omitted in this self-contained deployment.
 */
@Service("qrCodeService")
@Slf4j
public class QrCodeService {

    @Autowired
    QrCodeRepository qrCodeRepository;

    @Autowired
    QrCodeQueryRepository qrCodeQueryRepository;

    @Autowired
    DataSource dataSource;

    @Autowired
    QrImageStorageService imageStorage;

    @Autowired
    ResourceUrlConfig resourceUrlConfig;

    // QrCodeTemplateService is being created in Task 20; wired here for branded PDF support.
    @Autowired(required = false)
    io.sclera.service.QrCodeTemplateService qrCodeTemplateService;

    // =========================================================================
    // Count / lookup (retained from Phase 3)
    // =========================================================================

    public Integer getQrCodeCountByDeviceId(String deviceId) {
        log.info("getQrCodeCountByDeviceId");
        return qrCodeRepository.getQrCodeCountByDeviceId(deviceId);
    }

    public Integer countByDeviceId(String deviceId) {
        log.info("countByDeviceId");
        return (int) qrCodeRepository.countByDeviceId(deviceId);
    }

    // =========================================================================
    // Bulk reads (retained from Phase 3)
    // =========================================================================

    public Set<QrCodeDTO> getQrCodesByDeviceIds(Set<String> deviceIds) {
        log.info("getQrCodesByDeviceIds");
        return qrCodeRepository.getQrCodesByDeviceIds(deviceIds);
    }

    public Set<QrCodeDTO> getQrCodesByLocationIds(Set<String> locationIds) {
        log.info("getQrCodesByLocationIds");
        return qrCodeRepository.getQrCodesByLocationIds(locationIds);
    }

    public Set<QrCodeDTO> getQrCodeDetailsByIds(Set<String> qrcodeIds) {
        log.info("getQrCodeDetailsByIds");
        return qrCodeRepository.getQrCodeDetailsByIds(qrcodeIds);
    }

    /** UNION/native query — delegates to QrCodeRepository (not ClientQrCodeRepository). */
    public Set<QrCodeDTO> getClientQrCodeDetailsByIds(Set<String> clientQrcodeIds) {
        log.info("getClientQrCodeDetailsByIds");
        return qrCodeRepository.getClientQrCodeDetailsByIds(clientQrcodeIds);
    }

    // =========================================================================
    // Tagged-ID lookups (retained from Phase 3)
    // =========================================================================

    public JSONArray getDeviceIdsTaggedToQrCode(String vdmsId) {
        log.info("getDeviceIdsTaggedToQrCode");
        return qrCodeRepository.getDeviceIdsTaggedToQrCode(vdmsId);
    }

    public JSONArray getLocationIdsTaggedToQrCode(String vdmsId) {
        log.info("getLocationIdsTaggedToQrCode");
        return qrCodeRepository.getLocationIdsTaggedToQrCode(vdmsId);
    }

    // =========================================================================
    // Timestamp (retained from Phase 3)
    // =========================================================================

    public BigInteger getMaxUpdatedQrCodeTimeStamp(String id) {
        log.info("getMaxUpdatedQrCodeTimeStamp");
        return qrCodeRepository.getMaxUpdatedQrCodeTimeStamp(id);
    }

    // =========================================================================
    // JDBC batch upsert helper (retained from Phase 3)
    // =========================================================================

    public void upsertQrCodesInBatch(Set<QrCodeDTO> qrCodes) {
        log.info("upsertQrCodesInBatch");
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                    qrCodeQueryRepository.getQueryForUpsertQrCodesInBatch());
            int batchCounter = 0;
            int maxBatchLimit = 100;

            for (QrCodeDTO qrCode : qrCodes) {
                try {
                    ps.setString(1, qrCode.getId());
                    ps.setString(2, qrCode.getImageUrl());
                    ps.setString(3, qrCode.getLocationId());
                    ps.setString(4, qrCode.getVdmsId());
                    ps.setString(5, qrCode.getDeviceId());
                    ps.setString(6, qrCode.getCreatedBy());
                    ps.setString(7, String.valueOf(qrCode.getCreationTime()));
                    ps.setString(8, qrCode.getBatchId());
                    ps.setString(9, qrCode.getQrCodeLink());
                    ps.setString(10, qrCode.getUpdatedTime());
                    ps.setString(11, qrCode.getUpdatedBy());
                    ps.addBatch();
                    batchCounter++;
                    if (batchCounter == maxBatchLimit) {
                        ps.executeBatch();
                        log.info("added 100 qrcodes in a batch");
                        ps.clearBatch();
                        batchCounter = 0;
                    }
                } catch (Exception e) {
                    log.error("Exception in batch update of qrcodes", e);
                }
            }
            if (batchCounter > 0) {
                ps.executeBatch();
                log.info("Executed batch update of: {} qrcodes", batchCounter);
            }
            ps.close();
        } catch (Exception e) {
            log.error("Exception in batch update of qrcodes", e);
        }
    }

    // =========================================================================
    // QR code generation (ZXing)
    // =========================================================================

    /**
     * Generates a single QR code PNG image for the given data URL.
     * Width and height default to 500 if null; ErrorCorrectionLevel is H.
     */
    private byte[] generateQRCodeContentById(String qrCodeId) {
        log.info("generateQRCodeContentById: qrCodeId={}", qrCodeId);
        String data = resourceUrlConfig.getGlobal_qrcode_server_url() + "/" + qrCodeId;
        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 500, 500, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            log.info("Generated QR code content by id {}", qrCodeId);
            return toByteArray(image, "png");
        } catch (IOException | WriterException e) {
            log.error("Error generating QR code content: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Generates a single QR code PNG image with custom width/height.
     * Falls back to 500x500 when width or height is null.
     */
    private byte[] generateQRCodeContent(String data, Float width, Float height) {
        int w = (width != null) ? width.intValue() : 500;
        int h = (height != null) ? height.intValue() : 500;
        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, w, h, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            return toByteArray(image, "png");
        } catch (IOException | WriterException e) {
            log.error("Error generating QR code: {}", e.getMessage());
        }
        return null;
    }

    public static byte[] toByteArray(BufferedImage bi, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, format, baos);
        return baos.toByteArray();
    }

    // =========================================================================
    // Single or small-batch generateQRCode (direct download — caller writes to response)
    // =========================================================================

    /**
     * Generates count QR codes and returns the export bytes (zip/pdf/txt) directly.
     * Stores each PNG to QrImageStorageService and persists via addQrCode (see MISSING note).
     *
     * @param orgId      customer organisation id
     * @param email      creating user email
     * @param qrCodeCount number of codes to generate
     * @param width      page width in inches (null = 500px default)
     * @param height     page height in inches (null = 500px default)
     * @param type       export type: "zip", "pdf", or "txt"
     * @param brand      if true, embed brand template in PDF
     * @param defaultTemplate if true, use the default branded template
     * @param templateUrl optional URL for a custom branded template image
     * @return export bytes ready to write to an HTTP response or store
     */
    public byte[] generateQRCode(
            String orgId,
            String email,
            Integer qrCodeCount,
            Float width,
            Float height,
            String type,
            boolean brand,
            boolean defaultTemplate,
            String templateUrl) throws IOException {

        log.info("generateQRCode: orgId={}, email={}, count={}, type={}", orgId, email, qrCodeCount, type);

        String batchId = Generators.timeBasedGenerator().generate().toString();
        List<QrCodeDTO> qrCodeDTOS = new ArrayList<>();
        Map<String, byte[]> contentByQrCodeId = new HashMap<>();

        for (int i = 0; i < qrCodeCount; i++) {
            String qrCodeId = Generators.timeBasedGenerator().generate().toString();
            String imageUrl = resourceUrlConfig.getServer_qrcode_images_url() + "/" + batchId + "/" + qrCodeId;
            String data = resourceUrlConfig.getGlobal_qrcode_server_url() + "/" + qrCodeId;
            BigInteger creationTime = BigInteger.valueOf(System.currentTimeMillis());
            byte[] qrCodeContent = generateQRCodeContent(data, width, height);

            // Store the PNG to local/S3 storage
            if (qrCodeContent != null) {
                imageStorage.store(qrCodeContent, batchId + "/" + qrCodeId, "png");
            }

            QrCodeDTO dto = new QrCodeDTO();
            dto.setId(qrCodeId);
            dto.setImageUrl(imageUrl);
            dto.setQrCodeLink(data);
            dto.setBatchId(batchId);
            dto.setCreatedBy(email);
            dto.setCreationTime(creationTime);
            qrCodeDTOS.add(dto);
            contentByQrCodeId.put(qrCodeId, qrCodeContent);

            // Persist to DB — requires QrCodeRepository.addQrCode (MISSING — see report)
            try {
                qrCodeRepository.addQrCode(qrCodeId, imageUrl, data, creationTime, email, batchId);
            } catch (Exception e) {
                log.error("Failed to persist qr code id={}, batchId={}: {}", qrCodeId, batchId, e.getMessage());
            }
        }

        if ("zip".equals(type)) {
            return exportToZip(qrCodeDTOS, contentByQrCodeId, qrCodeCount, width, height, batchId, brand, defaultTemplate, templateUrl).toByteArray();
        } else if ("pdf".equals(type)) {
            return exportToPdf(qrCodeDTOS, contentByQrCodeId, width, height, brand, defaultTemplate, templateUrl).toByteArray();
        } else {
            // default: txt
            return exportToText(qrCodeDTOS).getBytes();
        }
    }

    // =========================================================================
    // Async bulk generation (generateBulkQRCode)
    // =========================================================================

    /**
     * Asynchronously generates qrCodeCount QR codes, stores the export bundle (zip/pdf/txt) via
     * QrImageStorageService, and logs the download link at INFO level.
     * Email delivery is not wired in this self-contained deployment; the link is logged instead.
     */
    public void generateBulkQRCode(
            String orgId,
            String email,
            Integer qrCodeCount,
            Float width,
            Float height,
            String type,
            String emailTo,
            boolean brand,
            boolean defaultTemplate,
            String templateUrl) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                log.info("generateBulkQRCode async start: orgId={}, email={}, count={}, type={}", orgId, email, qrCodeCount, type);

                String batchId = Generators.timeBasedGenerator().generate().toString();
                List<QrCodeDTO> qrCodeDTOS = new ArrayList<>();
                Map<String, byte[]> contentByQrCodeId = new HashMap<>();

                for (int i = 0; i < qrCodeCount; i++) {
                    String qrCodeId = Generators.timeBasedGenerator().generate().toString();
                    String imageUrl = resourceUrlConfig.getServer_qrcode_images_url() + "/" + batchId + "/" + qrCodeId;
                    String data = resourceUrlConfig.getGlobal_qrcode_server_url() + "/" + qrCodeId;
                    BigInteger creationTime = BigInteger.valueOf(System.currentTimeMillis());
                    byte[] qrCodeContent = generateQRCodeContent(data, width, height);

                    if (qrCodeContent != null) {
                        imageStorage.store(qrCodeContent, batchId + "/" + qrCodeId, "png");
                    }

                    QrCodeDTO dto = new QrCodeDTO();
                    dto.setId(qrCodeId);
                    dto.setImageUrl(imageUrl);
                    dto.setQrCodeLink(data);
                    dto.setBatchId(batchId);
                    dto.setCreatedBy(email);
                    dto.setCreationTime(creationTime);
                    qrCodeDTOS.add(dto);
                    contentByQrCodeId.put(qrCodeId, qrCodeContent);

                    // Persist to DB — requires QrCodeRepository.addQrCode (MISSING — see report)
                    try {
                        qrCodeRepository.addQrCode(qrCodeId, imageUrl, data, creationTime, email, batchId);
                    } catch (Exception e) {
                        log.error("Failed to persist qr code id={}: {}", qrCodeId, e.getMessage());
                    }
                }

                // Store the export bundle
                String exportKey;
                byte[] exportBytes;
                String ext;

                if ("zip".equals(type)) {
                    exportBytes = exportToZip(qrCodeDTOS, contentByQrCodeId, qrCodeCount, width, height, batchId, brand, defaultTemplate, templateUrl).toByteArray();
                    exportKey = "qrcode_" + qrCodeCount + "_" + batchId;
                    ext = "zip";
                } else if ("pdf".equals(type)) {
                    exportBytes = exportToPdf(qrCodeDTOS, contentByQrCodeId, width, height, brand, defaultTemplate, templateUrl).toByteArray();
                    exportKey = "qrcode_" + qrCodeCount + "_" + width + "x" + height + "_" + batchId;
                    ext = "pdf";
                } else {
                    exportBytes = exportToText(qrCodeDTOS).getBytes();
                    exportKey = "qrcode_" + qrCodeCount + "_" + batchId;
                    ext = "txt";
                }

                String storedUrl = imageStorage.store(exportBytes, exportKey, ext);
                String link = resourceUrlConfig.getServices_cloud_server_url() + "/qr/download?key=" + exportKey;
                // Email delivery not wired in self-contained deployment; log the download link.
                log.info("Bulk QR export ready for email={}, download link={}", emailTo, link);

            } catch (Exception e) {
                log.error("generateBulkQRCode async error: {}", e.getMessage(), e);
            }
        });
        executorService.shutdown();
    }

    // =========================================================================
    // Tagging and upsert
    // =========================================================================

    /**
     * Upserts QR code tag assignment (device/location/vdmsId).
     * Cloud sync is omitted; persists locally via qrCodeRepository.upsertQrcode (MISSING — see report).
     */
    public void upsertQrcode(QrCodeDTO qrCodeDTO, String loggedInUser) {
        log.info("upsertQrcode: id={}, loggedInUser={}", qrCodeDTO != null ? qrCodeDTO.getId() : null, loggedInUser);
        if (qrCodeDTO == null) {
            log.error("upsertQrcode called with null payload");
            return;
        }
        Long updatedTime = System.currentTimeMillis();
        // Requires QrCodeRepository.upsertQrcode(deviceId, locationId, vdmsId, updatedTime, loggedInUser, syncFlag, id) — MISSING
        try {
            qrCodeRepository.upsertQrcode(
                    qrCodeDTO.getDeviceId(),
                    qrCodeDTO.getLocationId(),
                    qrCodeDTO.getVdmsId(),
                    updatedTime,
                    loggedInUser,
                    1,
                    qrCodeDTO.getId());
        } catch (Exception e) {
            log.error("upsertQrcode failed for id={}: {}", qrCodeDTO.getId(), e.getMessage());
        }
    }

    /**
     * Tags a batch of QR codes to a vdms/org context.
     * Cloud WebSocket/multi-tenant sync is omitted.
     * Requires QrCodeRepository.tagAdcQrCode (MISSING — see report).
     */
    public void tagQrCodeByVdmsId(String orgId, String vdmsId, List<QrCodeDTO> qrCodeDTOList) {
        log.info("tagQrCodeByVdmsId: vdmsId={}, orgId={}, count={}", vdmsId, orgId, qrCodeDTOList != null ? qrCodeDTOList.size() : 0);
        if (qrCodeDTOList == null || qrCodeDTOList.isEmpty() || vdmsId == null) {
            log.error("tagQrCodeByVdmsId called with invalid params");
            return;
        }
        BigInteger updatedAt = BigInteger.valueOf(System.currentTimeMillis());
        String batchId = Generators.timeBasedGenerator().generate().toString();
        for (QrCodeDTO qrCodeDTO : qrCodeDTOList) {
            // Requires QrCodeRepository.tagAdcQrCode(deviceId, locationId, vdmsId, updatedBy, updatedAt, batchId, syncFlag, orgId, id) — MISSING
            try {
                qrCodeRepository.tagAdcQrCode(
                        qrCodeDTO.getDeviceId(),
                        qrCodeDTO.getLocationId(),
                        vdmsId,
                        qrCodeDTO.getUpdatedBy(),
                        updatedAt,
                        batchId,
                        1,
                        orgId,
                        qrCodeDTO.getId());
            } catch (Exception e) {
                log.error("tagAdcQrCode failed for id={}: {}", qrCodeDTO.getId(), e.getMessage());
            }
        }
    }

    /**
     * Updates device/location assignment of a QR code by id.
     * Requires QrCodeRepository.updateQrCodeById (MISSING — see report).
     */
    public void updateQrcodeById(String qrCodeId, QrCodeDTO qrCodeDTO) {
        log.info("updateQrcodeById: qrCodeId={}", qrCodeId);
        BigInteger updatedAt = BigInteger.valueOf(System.currentTimeMillis());
        // Requires QrCodeRepository.updateQrCodeById(deviceId, updatedBy, updatedAt, syncFlag, qrCodeId) — MISSING
        try {
            qrCodeRepository.updateQrCodeById(
                    qrCodeDTO.getDeviceId(),
                    qrCodeDTO.getUpdatedBy(),
                    updatedAt,
                    1,
                    qrCodeId);
        } catch (Exception e) {
            log.error("updateQrCodeById failed for id={}: {}", qrCodeId, e.getMessage());
        }
    }

    /**
     * Updates device/location detail on a list of QR codes.
     * Requires QrCodeRepository.updateQrCodeDetailsById (MISSING — see report).
     */
    public void updateQrCodeDetails(String vdmsId, List<QrCodeDTO> qrCodeDTOS) {
        log.info("updateQrCodeDetails: vdmsId={}", vdmsId);
        qrCodeDTOS.forEach(qrCodeDTO -> {
            int checkQrCode = qrCodeRepository.checkQrCodeId(qrCodeDTO.getId());
            if (checkQrCode == 1) {
                // Requires QrCodeRepository.updateQrCodeDetailsById(deviceId, locationId, updatedBy, updatedTime, syncFlag, id) — MISSING
                try {
                    qrCodeRepository.updateQrCodeDetailsById(
                            qrCodeDTO.getDeviceId(),
                            qrCodeDTO.getLocationId(),
                            qrCodeDTO.getUpdatedBy(),
                            qrCodeDTO.getUpdatedTime(),
                            1,
                            qrCodeDTO.getId());
                } catch (Exception e) {
                    log.error("updateQrCodeDetailsById failed for id={}: {}", qrCodeDTO.getId(), e.getMessage());
                }
            } else {
                log.info("qrcode id={} not found", qrCodeDTO.getId());
            }
        });
    }

    // =========================================================================
    // Lookup methods
    // =========================================================================

    /**
     * Fetches full QR code details by id.
     * Requires QrCodeRepository.getQrCodeDetailsByQrCodeId (MISSING — see report).
     */
    public QrCodeDTO getQrCodeDetailsByQrCodeId(String qrCodeId) {
        log.info("getQrCodeDetailsByQrCodeId: qrCodeId={}", qrCodeId);
        // Requires QrCodeRepository.getQrCodeDetailsByQrCodeId(String) : QrCodeDTO — MISSING
        return qrCodeRepository.getQrCodeDetailsByQrCodeId(qrCodeId);
    }

    /**
     * Fetches QR codes associated with a vdmsId and deviceId.
     * Requires QrCodeRepository.getQrCodeDetailsByVdmsIdAndDeviceId (MISSING — see report).
     */
    public List<QrCodeDTO> getQrCodeDetailsByVdmsIdAndDeviceId(String vdmsId, String deviceId) {
        log.info("getQrCodeDetailsByVdmsIdAndDeviceId: vdmsId={}, deviceId={}", vdmsId, deviceId);
        // Requires QrCodeRepository.getQrCodeDetailsByVdmsIdAndDeviceId(String, String) : List<QrCodeDTO> — MISSING
        return qrCodeRepository.getQrCodeDetailsByVdmsIdAndDeviceId(vdmsId, deviceId);
    }

    /**
     * Fetches QR codes associated with a vdmsId and locationId.
     * Requires QrCodeRepository.getQrCodeDetailsByVdmsIdAndLocationId (MISSING — see report).
     */
    public List<QrCodeDTO> getQrCodeDetailsByVdmsIdAndLocationId(String vdmsId, String locationId) {
        log.info("getQrCodeDetailsByVdmsIdAndLocationId: vdmsId={}, locationId={}", vdmsId, locationId);
        // Requires QrCodeRepository.getQrCodeDetailsByVdmsIdAndLocationId(String, String) : List<QrCodeDTO> — MISSING
        return qrCodeRepository.getQrCodeDetailsByVdmsIdAndLocationId(vdmsId, locationId);
    }

    /**
     * Returns a page of untagged QR code IDs.
     * Requires QrCodeRepository.getUnTaggedQrCode (MISSING — see report).
     */
    public List<String> getUnTaggedQrCode(int pageNo, int pageSize) {
        log.info("getUnTaggedQrCode: pageNo={}, pageSize={}", pageNo, pageSize);
        int offset = pageSize * (pageNo - 1);
        // Requires QrCodeRepository.getUnTaggedQrCode(int pageSize, int offset) : List<String> — MISSING
        return qrCodeRepository.getUnTaggedQrCode(pageSize, offset);
    }

    /**
     * Returns QR code count for a vdmsId since lastSyncTime.
     * Requires QrCodeRepository.getQrCodeCountsByVdsId (MISSING — see report).
     */
    public int getQrCodeCounts(String vdmsId, BigInteger lastSyncTime) {
        log.info("getQrCodeCounts: vdmsId={}, lastSyncTime={}", vdmsId, lastSyncTime);
        // Requires QrCodeRepository.getQrCodeCountsByVdsId(String vdmsId, BigInteger lastSyncTime) : int — MISSING
        return qrCodeRepository.getQrCodeCountsByVdsId(vdmsId, lastSyncTime);
    }

    /**
     * Checks whether a QR code is ADC-tagged, and if so whether it is tagged to a device/location.
     * Uses existing QrCodeRepository methods (checkQrCodeId, getAdcCheckByQrCodeId,
     * getIsManagedAssetsTagged) plus getQrCodeId and getIsAdcTagged which are MISSING.
     */
    public Map<String, Object> getAdcCheckByQrCodeId(String qrCodeId) {
        log.info("getAdcCheckByQrCodeId: qrCodeId={}", qrCodeId);
        Map<String, Object> result = new HashMap<>();

        int isPresentInDb = qrCodeRepository.checkQrCodeId(qrCodeId);
        if (isPresentInDb == 0) {
            result.put("isTagged", 2);
            result.put("isAdc", false);
        } else {
            int adcCount = qrCodeRepository.getAdcCheckByQrCodeId(qrCodeId);
            if (adcCount == 1) {
                result.put("isAdc", true);
                // Requires QrCodeRepository.getIsAdcTagged(String qrCodeId) : int — MISSING
                int isTagged = qrCodeRepository.getIsAdcTagged(qrCodeId);
                result.put("isTagged", isTagged);
            } else {
                int isManagedAssetsTagged = qrCodeRepository.getIsManagedAssetsTagged(qrCodeId);
                result.put("isTagged", isManagedAssetsTagged == 1 ? 1 : 0);
                result.put("isAdc", false);
            }
        }
        return result;
    }

    // =========================================================================
    // Private export helpers
    // =========================================================================

    private ByteArrayOutputStream exportToZip(
            List<QrCodeDTO> qrCodeDTOS,
            Map<String, byte[]> contentByQrCodeId,
            Integer qrCodeCount,
            Float width,
            Float height,
            String batchId,
            boolean brand,
            boolean defaultTemplate,
            String templateUrl) throws IOException {

        ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(zipOutputStream)) {
            String pdfFileName = "qrcode_" + qrCodeCount + "_" + width + "x" + height + "_" + batchId + ".pdf";
            String txtFileName = "qrcode_" + qrCodeCount + "_" + batchId + ".txt";

            String txtContent = exportToText(qrCodeDTOS);
            ByteArrayOutputStream pdfOutputStream = exportToPdf(qrCodeDTOS, contentByQrCodeId, width, height, brand, defaultTemplate, templateUrl);

            zip.putNextEntry(new ZipEntry(pdfFileName));
            zip.write(pdfOutputStream.toByteArray());
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry(txtFileName));
            zip.write(txtContent.getBytes());
            zip.closeEntry();
        }
        return zipOutputStream;
    }

    private String exportToText(List<QrCodeDTO> qrCodeDTOS) {
        StringBuilder sb = new StringBuilder();
        for (QrCodeDTO dto : qrCodeDTOS) {
            sb.append(dto.getQrCodeLink()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Builds a PDF using iText 5. Each QR code occupies one page.
     * When defaultTemplate is true and brand is true, a branded background is loaded from the
     * classpath (images/serve_logo.png). When defaultTemplate is false and templateUrl is set,
     * the template image bytes are fetched via QrImageStorageService and used as the background.
     * When QrCodeTemplateService (Task 20) is available, template resolution is delegated to it.
     */
    private ByteArrayOutputStream exportToPdf(
            List<QrCodeDTO> qrCodeDTOS,
            Map<String, byte[]> contentByQrCodeId,
            Float width,
            Float height,
            boolean brand,
            boolean defaultTemplate,
            String templateUrl) throws IOException {

        float pageWidthPt = (width != null ? width : 3.0f) * 72f;
        float pageHeightPt = (height != null ? height : 3.0f) * 72f;

        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        Document document = new Document(new Rectangle(pageWidthPt, pageHeightPt), 0, 0, 0, 0);
        try {
            PdfWriter.getInstance(document, pdfOutputStream);
            document.open();

            byte[] templateBytes = null;
            if (!defaultTemplate && templateUrl != null && !templateUrl.isEmpty()) {
                if (qrCodeTemplateService != null) {
                    templateBytes = qrCodeTemplateService.fetchTemplateBytes(templateUrl);
                } else {
                    try {
                        templateBytes = imageStorage.fetch(templateUrl);
                    } catch (Exception e) {
                        log.warn("Could not fetch template from storage key={}: {}", templateUrl, e.getMessage());
                    }
                }
            } else if (defaultTemplate && brand) {
                try (InputStream is = getClass().getClassLoader().getResourceAsStream("images/serve_logo.png")) {
                    if (is != null) {
                        templateBytes = is.readAllBytes();
                    } else {
                        log.warn("Brand template image not found at classpath:images/serve_logo.png");
                    }
                }
            }

            for (QrCodeDTO dto : qrCodeDTOS) {
                byte[] qrBytes = contentByQrCodeId.get(dto.getId());
                if (qrBytes == null) {
                    log.warn("No QR content for id={}, skipping page", dto.getId());
                    continue;
                }

                document.newPage();

                // Draw background template
                if (templateBytes != null) {
                    Image bg = Image.getInstance(templateBytes);
                    bg.setAbsolutePosition(0, 0);
                    bg.scaleAbsolute(pageWidthPt, pageHeightPt);
                    document.add(bg);
                }

                // Draw QR code image, centred
                Image qrImage = Image.getInstance(qrBytes);
                float qrSize;
                if (width != null && height != null && Float.compare(width, height) == 0) {
                    qrSize = pageWidthPt * 0.63f;
                } else if (width != null && height != null && width > height) {
                    qrSize = pageHeightPt * 0.59f;
                } else {
                    qrSize = pageWidthPt * 0.90f;
                }
                float qrX = (pageWidthPt - qrSize) / 2f;
                float qrY;
                if (brand && templateBytes != null) {
                    float verticalOffset = pageHeightPt * 0.075f;
                    qrY = (pageHeightPt - qrSize) / 2f - verticalOffset;
                } else {
                    qrY = (pageHeightPt - qrSize) / 2f;
                }
                qrImage.scaleAbsolute(qrSize, qrSize);
                qrImage.setAbsolutePosition(qrX, qrY);
                document.add(qrImage);
            }

            document.close();
        } catch (DocumentException e) {
            log.error("iText PDF generation error: {}", e.getMessage(), e);
            throw new IOException("PDF generation failed: " + e.getMessage(), e);
        }
        return pdfOutputStream;
    }
}
