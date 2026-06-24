package io.sclera.service;

import com.alibaba.fastjson2.JSONObject;
import com.amazonaws.HttpMethod;
import com.fasterxml.uuid.Generators;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.ClientQrCodeDTO;
import io.sclera.dto.QrCodeDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VdmsSyncDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.QrCodeRepository;
import io.sclera.util.ScleraUtils;
import io.sclera.util.SocketUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class QrCodeService {
    @Autowired
    private QrCodeRepository qrCodeRepository;
    @Autowired
    private UserActionLogService userActionLogService;
    @Autowired
    private UserActivityService userActivityService;
    @Autowired
    private ResourceUrlConfig resourceUrlConfig;

    @Autowired
    private AwsService awsService;

    @Autowired
    private UserService userService;

    @Autowired
    private WebClientAlertService webClientAlertService;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Autowired
    private ClientQrCodeService clientQrCodeService;

    @Autowired
    private VdmsService vdmsService;

    @Autowired
    private SocketUtils socketUtils;

    @Autowired
    private WebClientService webClientService;

    // qr code sync changes
    public ResponseEntity<?> upsertQrcode(QrCodeDTO qrCodeDTO, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: QrCodeDTO:{}, loggedInUser:{}", qrCodeDTO, loggedInUser);
        Long updatedTime = System.currentTimeMillis();

        if (qrCodeDTO == null) {
            log.error("Invalid client params, EndPoint:  {}", httpServletRequest.getRequestURI());
            logFailure("Invalid client params", loggedInUser, qrCodeDTO);
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

        QrCodeDTO existingQrCodeData = qrCodeRepository.getQrCodeDetailsByQrCodeId(qrCodeDTO.getId());
        log.info("Existing QrCodeDTO: {}", existingQrCodeData);

        if (existingQrCodeData == null) {
            log.error("qrCode is not found, EndPoint:{}", httpServletRequest.getRequestURI());
            logFailure("qrCode is not found", loggedInUser, qrCodeDTO);
            throw new ClientException("qrCode is not Found", 799, httpServletRequest.getRequestURI());
        }

        handleQrCodeSyncChanges(existingQrCodeData, qrCodeDTO, loggedInUser, updatedTime, httpServletRequest);
        VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder()
                .qr_sync(1)
                .build();
        int isMultiTenant = vdmsService.getMultiTenantCheck(qrCodeDTO.getVdmsId());
        if (isMultiTenant == 1) {
            String awsRegion = vdmsService.getAwsRegionByVdmsId(qrCodeDTO.getVdmsId());
            webClientService.multiTenantSyncApiCall(qrCodeDTO.getVdmsId(), vdmsSyncDTO,awsRegion, httpServletRequest);
        } else {
            socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + qrCodeDTO.getVdmsId() + "/sync/data", vdmsSyncDTO);
        }
        logActivityLogs(existingQrCodeData, qrCodeDTO, loggedInUser);

        ResponseDTO responseDTO = ScleraUtils.generatePayload("Updated details successfully", 200, true);
        log.info("Updating qrCode details. EndPoint {}", httpServletRequest.getRequestURI());

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    private void handleQrCodeSyncChanges(QrCodeDTO existingQrCodeData, QrCodeDTO qrCodeDTO, String loggedInUser, Long updatedTime, HttpServletRequest httpServletRequest) {
        String existingVdmsId = existingQrCodeData.getVdmsId();
        String newVdmsId = qrCodeDTO.getVdmsId();

        if (existingVdmsId == null || !existingVdmsId.equals(newVdmsId)) {
            // Update sync state for the new VDMS
            vdmsService.updateQrCodeSyncByVdmsId(1, newVdmsId, httpServletRequest);

            if (existingVdmsId != null) {
                // Update sync state for the old VDMS
                vdmsService.updateQrCodeSyncByVdmsId(2, existingVdmsId, httpServletRequest);
            }
            // Insert/Update QR code details
            qrCodeRepository.upsertQrcode(qrCodeDTO.getDeviceId(), qrCodeDTO.getLocationId(), newVdmsId, updatedTime, loggedInUser, 1, qrCodeDTO.getId());
        } else {
            int qrCodeSyncState = vdmsService.getQrCodeSyncByVdmsId(newVdmsId);
            if (qrCodeSyncState == 2) {
                vdmsService.updateQrCodeSyncByVdmsId(2, newVdmsId, httpServletRequest);
            } else {
                vdmsService.updateQrCodeSyncByVdmsId(1, newVdmsId, httpServletRequest);
            }
            qrCodeRepository.upsertQrcode(qrCodeDTO.getDeviceId(), qrCodeDTO.getLocationId(), newVdmsId, updatedTime, loggedInUser, 1, qrCodeDTO.getId());

        }
    }

    private void logActivityLogs(QrCodeDTO existingQrCodeData, QrCodeDTO qrCodeDTO, String loggedInUser) {
        String toType = qrCodeDTO.getDeviceId() != null ? "device" : "location";
        String toId = qrCodeDTO.getDeviceId() != null ? qrCodeDTO.getDeviceId() : qrCodeDTO.getLocationId();

        if (existingQrCodeData.getDeviceId() == null && existingQrCodeData.getLocationId() == null) {
            String message = String.format("QR code tagged to %s with ID:%s in VDMS:%s", toType, toId, qrCodeDTO.getVdmsId());
            userActivityService.addUserActivityLogs(loggedInUser, "qr_code", toType, "UPDATE", "success", message, toId, qrCodeDTO.getVdmsId());
            userActionLogService.addUserActionLog(loggedInUser, "QrCode", "UPDATE", message, "success");
        } else {
            String fromType = existingQrCodeData.getDeviceId() != null ? "device" : "location";
            String fromId = existingQrCodeData.getDeviceId() != null ? existingQrCodeData.getDeviceId() : existingQrCodeData.getLocationId();
            String message = String.format("QR code tagged from %s with ID:%s to %s with ID:%s in VDMS:%s", fromType, fromId, toType, toId, qrCodeDTO.getVdmsId());
            userActivityService.addUserActivityLogs(loggedInUser, "qr_code", toType, "UPDATE", "success", message, toId, qrCodeDTO.getVdmsId());
            userActionLogService.addUserActionLog(loggedInUser, "QrCode", "UPDATE", message, "success");
        }
    }

    private void logFailure(String reason, String loggedInUser, QrCodeDTO qrCodeDTO) {
        userActivityService.addUserActivityLogs(loggedInUser, "qr_code", null, "UPDATE", "failed", reason, null, qrCodeDTO != null ? qrCodeDTO.getVdmsId() : null);
        userActionLogService.addUserActionLog(loggedInUser, "QrCode", "UPDATE", reason, "failed");
    }

    public ResponseEntity<?> getQrCodeDetailsByQrCodeId(
            String qrCodeId, HttpServletRequest httpServletRequest) {
        log.info("Payload: qrCodeId:{}", qrCodeId);
        QrCodeDTO qrCodeDTO = qrCodeRepository.getQrCodeDetailsByQrCodeId(qrCodeId);
        log.info("QrCodeDTO: {}", qrCodeDTO);
        if (qrCodeDTO != null) {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(qrCodeDTO, 200, true);
            log.info(
                    "Fetching qrCode details by qrCodeId {}, EndPoint :{}",
                    qrCodeId,
                    httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("QrCode does not exist, EndPoint:  {}", httpServletRequest.getRequestURI());
            throw new ClientException("QrCode does not exist", 799, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getQrCodeDetailsByVdmsIdAndDeviceId(
            String vdmsId, String deviceId, HttpServletRequest httpServletRequest) {
        log.info("Payload: vdmsId:{}, deviceId:{}", vdmsId, deviceId);
        List<QrCodeDTO> qrCodeDTO =
                qrCodeRepository.getQrCodeDetailsByVdmsIdAndDeviceId(vdmsId, deviceId);

        List<ClientQrCodeDTO> clientQrCodeDetails =
                clientQrCodeService.getClientQrCodeDetailsByVdmsIdAndDeviceIds(vdmsId, List.of(deviceId));
        if (!clientQrCodeDetails.isEmpty()) {
            for (int i = 0; i < clientQrCodeDetails.size(); i++) {

                QrCodeDTO newQrCodeDTO = new QrCodeDTO();
                newQrCodeDTO.setId(clientQrCodeDetails.get(i).getId());
                newQrCodeDTO.setLocationId(clientQrCodeDetails.get(i).getLocationId());
                newQrCodeDTO.setDeviceId(clientQrCodeDetails.get(i).getDeviceId());
                newQrCodeDTO.setVdmsId(clientQrCodeDetails.get(i).getVdmsId());
                newQrCodeDTO.setCreatedBy(clientQrCodeDetails.get(i).getCreatedBy());
                newQrCodeDTO.setUpdated_by(clientQrCodeDetails.get(i).getUpdated_by());
                newQrCodeDTO.setCreationTime(clientQrCodeDetails.get(i).getCreationTime().toString());
                qrCodeDTO.add(newQrCodeDTO);
            }
        }
        if (qrCodeDTO != null) {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(qrCodeDTO, 200, true);
            log.info(
                    "Fetching qrCode details by vdmsId: {} and deviceId {}, EndPoint: {}",
                    vdmsId,
                    deviceId,
                    httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("QrCode does not exist, EndPoint:  {}", httpServletRequest.getRequestURI());
            throw new ClientException("QrCode does not exist", 799, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getQrCodeDetailsByVdmsIdAndLocationId(
            String vdmsId, String locationId, HttpServletRequest httpServletRequest) {
        log.info("Payload: vdmsId:{}, locationId:{}", vdmsId, locationId);
        List<QrCodeDTO> qrCodeDTO =
                qrCodeRepository.getQrCodeDetailsByVdmsIdAndLocationId(vdmsId, locationId);

        List<ClientQrCodeDTO> clientQrCodeDetails =
                clientQrCodeService.getClientQrCodeDetailsByVdmsIdAndLocationIds(
                        vdmsId, List.of(locationId));
        if (!clientQrCodeDetails.isEmpty()) {
            for (int i = 0; i < clientQrCodeDetails.size(); i++) {

                QrCodeDTO newQrCodeDTO = new QrCodeDTO();
                newQrCodeDTO.setId(clientQrCodeDetails.get(i).getId());
                newQrCodeDTO.setLocationId(clientQrCodeDetails.get(i).getLocationId());
                newQrCodeDTO.setDeviceId(clientQrCodeDetails.get(i).getDeviceId());
                newQrCodeDTO.setVdmsId(clientQrCodeDetails.get(i).getVdmsId());
                newQrCodeDTO.setCreatedBy(clientQrCodeDetails.get(i).getCreatedBy());
                newQrCodeDTO.setUpdated_by(clientQrCodeDetails.get(i).getUpdated_by());
                newQrCodeDTO.setCreationTime(clientQrCodeDetails.get(i).getCreationTime().toString());
                qrCodeDTO.add(newQrCodeDTO);
            }
        }

        if (qrCodeDTO != null) {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(qrCodeDTO, 200, true);
            log.info(
                    "Fetching qrCode details by vdmsId: {} and locationId: {},EndPoint: {}",
                    vdmsId,
                    locationId,
                    httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("QrCode does not exist, EndPoint:  {}", httpServletRequest.getRequestURI());
            throw new ClientException("QrCode does not exist", 799, httpServletRequest.getRequestURI());
        }
    }

    public void generateQRCode(
            String orgId,
            String email,
            Integer qrCodeCount,
            Float width,
            Float height,
            String type,
            boolean brand, boolean defaultTemplate, String templateUrl,
            HttpServletResponse httpServletResponse,
            HttpServletRequest httpServletRequest)
            throws IOException {
        log.info(
                "Payload: orgId:{},email:{},qrCodeCount:{},width:{},height:{},type:{},brand:{}",
                orgId,
                email,
                qrCodeCount,
                width,
                height,
                type, brand);
        ExecutorService thread = Executors.newSingleThreadExecutor();
        List<QrCodeDTO> qrCodeDTOS = new ArrayList<>();

        String batchId = Generators.timeBasedGenerator().generate().toString();
        for (int i = 0; i < qrCodeCount; i++) {
            String qrCodeId = Generators.timeBasedGenerator().generate().toString();
            String qrCodeImageUrl = String.format(resourceUrlConfig.getQrCodeUrl(), batchId, qrCodeId);
            String data = String.format(resourceUrlConfig.getQrCodeDataUrl(), qrCodeId);
            BigInteger creationTime = BigInteger.valueOf(System.currentTimeMillis());
            byte[] qrCodeContent = this.generateQRCodeContentById(qrCodeId);
            try {
                QrCodeDTO qrCodeDTO = new QrCodeDTO(qrCodeId, qrCodeImageUrl, data, qrCodeContent);
                qrCodeDTOS.add(qrCodeDTO);
                qrCodeRepository.addQrCode(qrCodeId, qrCodeImageUrl, data, creationTime, email, batchId);
            } catch (Exception e) {
                log.error(batchId);
                log.error("Error: {}", e.getMessage());
            }
        }

        // Download the file
        if (type.equals("zip")) {
            this.downloadZip(qrCodeDTOS, qrCodeCount, width, height, batchId, brand, defaultTemplate, templateUrl, httpServletResponse);
        } else if (type.equals("pdf")) {
            this.downloadPdf(qrCodeDTOS, qrCodeCount, width, height, batchId, brand, defaultTemplate, templateUrl, httpServletResponse);
        } else if (type.equals("txt")) {
            this.downloadText(qrCodeDTOS, qrCodeCount, batchId, httpServletResponse);
        }

        Runnable uploadQRCodesToS3 = () -> awsService.uploadQRCodesToS3(qrCodeDTOS, batchId);
        thread.submit(uploadQRCodesToS3);
        thread.shutdown();
        userActionLogService.addUserActionLog(
                email, "QrCode", "ADD", "Generated:" + qrCodeCount + " Qr-Code", "success");
        log.info("Generated QR code Successfully.EndPoint:{}", httpServletRequest.getRequestURI());
    }

    private void uploadText(
            List<QrCodeDTO> qrCodeDTOS,
            Integer qrCodeCount,
            String batchId,
            HttpServletRequest httpServletRequest)
            throws IOException {
        String text = this.exportQRCodesToText(qrCodeDTOS);
        awsService.addQrCodeFileToAWSS3(
                text.getBytes(),
                resourceUrlConfig.getQrCodeFileUrl(),
                resourceUrlConfig.getQrCodeFileDirectory(),
                ".txt",
                "qrcode_" + qrCodeCount + "_" + batchId,
                httpServletRequest);
    }

    private void uploadPdf(
            List<QrCodeDTO> qrCodeDTOS,
            Integer qrCodeCount,
            Float width,
            Float height,
            String batchId,
            boolean brand, boolean defaultTemplate, String templateUrl,
            HttpServletRequest httpServletRequest)
            throws IOException {
        ByteArrayOutputStream pdfOutputStream = null;
        if (defaultTemplate) {
            pdfOutputStream = this.exportDefaultQRCodesTemplateWithContentToPDF(qrCodeDTOS, width, height, brand);
        } else {
            pdfOutputStream = this.exportQRCodesToPDF(qrCodeDTOS, width, height, templateUrl);
        }
        awsService.addQrCodeFileToAWSS3(
                pdfOutputStream.toByteArray(),
                resourceUrlConfig.getQrCodeFileUrl(),
                resourceUrlConfig.getQrCodeFileDirectory(),
                ".pdf",
                "qrcode_" + qrCodeCount + "_" + width + "x" + height + "_" + batchId,
                httpServletRequest);
    }

    private void uploadZip(
            List<QrCodeDTO> qrCodeDTOS,
            Integer qrCodeCount,
            Float width,
            Float height,
            String batchId,
            boolean brand, boolean defaultTemplate, String templateUrl,
            HttpServletRequest httpServletRequest)
            throws IOException {
        ByteArrayOutputStream zipOutputStream =
                this.exportQRCodesToZIP(qrCodeDTOS, qrCodeCount, width, height, batchId, brand, defaultTemplate, templateUrl);

        awsService.addQrCodeFileToAWSS3(
                zipOutputStream.toByteArray(),
                resourceUrlConfig.getQrCodeFileUrl(),
                resourceUrlConfig.getQrCodeFileDirectory(),
                ".zip",
                "qrcode_" + qrCodeCount + "_" + batchId,
                httpServletRequest);
    }

    private void downloadText(
            List<QrCodeDTO> qrCodeDTOS,
            Integer qrCodeCount,
            String batchId,
            HttpServletResponse httpServletResponse)
            throws IOException {
        String text = this.exportQRCodesToText(qrCodeDTOS);
        httpServletResponse.setContentType("text/plain");
        String fileName = "qrcode_" + qrCodeCount + "_" + batchId + ".txt";
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= " + fileName;
        httpServletResponse.setHeader(headerKey, headerValue);
        OutputStream out = httpServletResponse.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        writer.write(text);
        writer.flush();
        writer.close();
    }

    private void downloadPdf(
            List<QrCodeDTO> qrCodeDTOS,
            Integer qrCodeCount,
            Float width,
            Float height,
            String batchId,
            boolean brand, boolean defaultTemplate, String templateUrl,
            HttpServletResponse httpServletResponse)
            throws IOException {
        ByteArrayOutputStream pdfOutputStream = null;
        try {
            if (defaultTemplate) {
                pdfOutputStream = this.exportDefaultQRCodesTemplateWithContentToPDF(qrCodeDTOS, width, height, brand);
            } else {
                pdfOutputStream = this.exportQRCodesToPDF(qrCodeDTOS, width, height, templateUrl);
            }
        } catch (IOException e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        httpServletResponse.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue =
                "attachment; filename=qrcode_"
                        + qrCodeCount
                        + "_"
                        + width
                        + "x"
                        + height
                        + "_"
                        + batchId
                        + ".pdf";
        httpServletResponse.setHeader(headerKey, headerValue);
        pdfOutputStream.writeTo(httpServletResponse.getOutputStream());
    }

    private void downloadZip(
            List<QrCodeDTO> qrCodeDTOS,
            Integer qrCodeCount,
            Float width,
            Float height,
            String batchId,
            boolean brand, boolean defaultTemplate, String templateUrl,
            HttpServletResponse httpServletResponse)
            throws IOException {
        ByteArrayOutputStream zipOutputStream =
                this.exportQRCodesToZIP(qrCodeDTOS, qrCodeCount, width, height, batchId, brand, defaultTemplate, templateUrl);

        httpServletResponse.setContentType("application/zip");
        String zipFileName = "qrcode_" + qrCodeCount + "_" + batchId + ".zip";
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=" + zipFileName);
        zipOutputStream.writeTo(httpServletResponse.getOutputStream());
        httpServletResponse.getOutputStream().flush();
    }

    private ByteArrayOutputStream exportQRCodesToZIP(
            List<QrCodeDTO> qrCodeDTOS, Integer qrCodeCount, Float width, Float height, String batchId, boolean brand, boolean defaultTemplate, String templateUrl)
            throws IOException {
        log.info("Payload: qrCodeCount:{}, Width:{}, Height:{}", qrCodeCount, width, height);
        ByteArrayOutputStream zipOutputStream;
        zipOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(zipOutputStream);
        try {
            String pdfFileName =
                    "qrcode_" + qrCodeCount + "_" + width + "x" + height + "_" + batchId + ".pdf";
            String txtFileName = "qrcode_" + qrCodeCount + "_" + batchId + ".txt";

            // Generate text file content
            String txtContent = exportQRCodesToText(qrCodeDTOS);

            // Generate PDF content
            ByteArrayOutputStream pdfOutputStream = null;
            if (defaultTemplate) {
                pdfOutputStream = this.exportDefaultQRCodesTemplateWithContentToPDF(qrCodeDTOS, width, height, brand);
            } else {
                pdfOutputStream = this.exportQRCodesToPDF(qrCodeDTOS, width, height, templateUrl);
            }

            // Add PDF content to the zip folder
            zip.putNextEntry(new ZipEntry(pdfFileName));
            zip.write(pdfOutputStream.toByteArray());
            zip.closeEntry();

            // Add text file content to the zip folder
            zip.putNextEntry(new ZipEntry(txtFileName));
            zip.write(txtContent.getBytes());
            zip.closeEntry();

        } catch (IOException e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            // Close the zip folder
            zip.close();
        }
        return zipOutputStream;
    }

    private String exportQRCodesToText(List<QrCodeDTO> qrCodeDTOS) throws IOException {
        log.info("Payload: QrCodeDTO:{}", qrCodeDTOS);
        StringBuilder stringBuilder = new StringBuilder();
        for (QrCodeDTO qrCodeDTO : qrCodeDTOS) {
            String qrCodeLink = qrCodeDTO.getQr_code_link();
            stringBuilder.append(qrCodeLink).append("\n");
        }
        return stringBuilder.toString();
    }

//    private ByteArrayOutputStream exportQRCodesToPDF(
//            List<QrCodeDTO> qrCodeDTOS, Float width, Float height, boolean brand) throws IOException {
//        log.info("Payload: Width:{}, Height:{}", width, height);
//        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
//        try (PDDocument document = new PDDocument()) {
//            float pageWidth = width * 72;
//            float pageHeight = height * 72;
//            for (QrCodeDTO qrCodeDTO : qrCodeDTOS) {
//                PDPage page = new PDPage(new PDRectangle(pageWidth, pageHeight));
//                document.addPage(page);
//                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
//                    PDImageXObject serveImage = null;
//                    if (brand) {
//                        try (InputStream imageStream = getClass().getClassLoader().getResourceAsStream("images/serve_logo.png")) {
//                            if (imageStream == null) {
//                                throw new RuntimeException("Image file not found: images/serve_logo.png");
//                            }
//                            serveImage = PDImageXObject.createFromByteArray(document, imageStream.readAllBytes(), "serve_logo.png");
//                            contentStream.drawImage(serveImage, 0, 0, pageWidth, pageHeight);
//                        }
//                    }
//                    PDImageXObject qrCodeImage = PDImageXObject.createFromByteArray(document, qrCodeDTO.getQrCodeContent(), "qrcode");
//                    float qrCodeWidth, qrCodeHeight;
//                    if (Float.compare(width, height) == 0) {
//                        qrCodeWidth = pageWidth * 0.63f;
//                        qrCodeHeight = qrCodeWidth;
//                    } else if (width > height) {
//                        qrCodeWidth = pageHeight * 0.59f;
//                        qrCodeHeight = qrCodeWidth;
//                    } else {
//                        qrCodeWidth = pageWidth * 0.90f;
//                        qrCodeHeight = qrCodeWidth;
//                    }
//                    float qrCodeX = (pageWidth - qrCodeWidth) / 2;
//                    float qrCodeY;
//
//                    if (brand && serveImage != null) {
//                        float verticalOffset = pageHeight * 0.075f;
//                        qrCodeY = (pageHeight - qrCodeHeight) / 2 - verticalOffset;
//                    } else {
//                        qrCodeY = (pageHeight - qrCodeHeight) / 2;
//                    }
//                    contentStream.drawImage(qrCodeImage, qrCodeX, qrCodeY, qrCodeWidth, qrCodeHeight);
//                }
//            }
//
//            document.save(pdfOutputStream);
//        } catch (IOException e) {
//            log.error("Error generating PDF: {}", e.getMessage());
//            throw e;
//        }
//
//        return pdfOutputStream;
//    }

    private byte[] generateQRCodeContentById(String qrCodeId) {
        log.info("Payload: qrCodeId: {}", qrCodeId);
        String data = String.format(resourceUrlConfig.getQrCodeDataUrl(), qrCodeId);
        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 500, 500, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            log.info("Generated QrCode content by Id {}", qrCodeId);
            return toByteArray(image, "png");
        } catch (IOException | WriterException e) {
            log.error("Error: {}", e.getMessage());
        }
        return null;
    }

    public static byte[] toByteArray(BufferedImage bi, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, format, baos);
        return baos.toByteArray();
    }

    public QrCodeDTO getQrCodeDataByQrCodeId(String qrcodeId) {
        log.info("Payload: qrcodeId: {}", qrcodeId);
        QrCodeDTO qrCodeDTO = qrCodeRepository.getVdmsInfoByQrCodeId(qrcodeId);
        log.info("Fetching QrCode data by QrCode Id: {}", qrcodeId);
        return qrCodeDTO;
    }

    public boolean isValidPresignedUrl(String preSignedUrl) {
        try {
            URL url = new URL(preSignedUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteQrCodeByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info(
                "Successfully Deleting Qr-Code data For vdms_Id:{},EndPoint:{}",
                vdmsId,
                httpServletRequest.getRequestURI());
        qrCodeRepository.deleteQrCodeByVdmsId(vdmsId);
    }

    public ResponseEntity<?> getQrCodeDetailsByType(
            String vdmsId, String id, String type, HttpServletRequest httpServletRequest) {
        log.info("Payload : vdmsId :{} , Id : {} Type : {} ", vdmsId, id, type);

        if (vdmsId != null && type != null) {
            ResponseDTO responseDTO = null;
            if (type.equals("device")) {
                List<QrCodeDTO> qrCodeDTO =
                        qrCodeRepository.getQrCodeDetailsByVdmsIdAndDeviceId(vdmsId, id);
                if (qrCodeDTO != null) {
                    responseDTO = ScleraUtils.generatePayload(qrCodeDTO, 200, true);
                    log.info(
                            "Fetching qrCode details by vdmsId: {} and deviceId {}, EndPoint: {}",
                            vdmsId,
                            id,
                            httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    log.error("QrCode does not exist, EndPoint:  {}", httpServletRequest.getRequestURI());
                    throw new ClientException(
                            "QrCode does not exist", 799, httpServletRequest.getRequestURI());
                }
            } else if (type.equals("location")) {

                List<QrCodeDTO> qrCodeDTO =
                        qrCodeRepository.getQrCodeDetailsByVdmsIdAndLocationId(vdmsId, id);
                if (qrCodeDTO != null) {
                    responseDTO = ScleraUtils.generatePayload(qrCodeDTO, 200, true);
                    log.info(
                            "Fetching qrCode details by vdmsId: {} and locationId: {},EndPoint: {}",
                            vdmsId,
                            id,
                            httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    log.error("QrCode does not exist, EndPoint:  {}", httpServletRequest.getRequestURI());
                    throw new ClientException(
                            "QrCode does not exist", 799, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Invalid client param,EndPoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Invalid client param", 700, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid client param,EndPoint: {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client param", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> generateBulkQRCode(
            String orgId,
            String email,
            Integer qrCodeCount,
            Float width,
            Float height,
            String type,
            String emailTo,
            boolean brand, boolean defaultTemplate, String templateUrl,
            HttpServletRequest httpServletRequest)
            throws IOException {
        log.info(
                "Payload: orgId:{},email:{},qrCodeCount:{},width:{},height:{},type:{},brand:{}",
                orgId,
                email,
                qrCodeCount,
                width,
                height,
                type, brand);
        generateBulkQRCode(orgId, email, qrCodeCount, width, height, type, emailTo, brand, defaultTemplate, templateUrl);
        ResponseDTO responseDTO =
                ScleraUtils.generatePayload("Qr Code Link will be Sent in Email Shortly", 200, true);
        log.info("Generated QR code Successfully.EndPoint {}", httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public void generateBulkQRCode(
            String orgId,
            String email,
            Integer qrCodeCount,
            Float width,
            Float height,
            String type,
            String emailTo,
            boolean brand, boolean defaultTemplate, String templateUrl) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(
                () -> {
                    List<QrCodeDTO> qrCodeDTOS = new ArrayList<>();
                    String qrCodeId;
                    String batchId = Generators.timeBasedGenerator().generate().toString();
                    String key = null;
                    for (int i = 0; i < qrCodeCount; i++) {
                        qrCodeId = Generators.timeBasedGenerator().generate().toString();
                        String qrCodeImageUrl =
                                String.format(resourceUrlConfig.getQrCodeUrl(), batchId, qrCodeId);
                        String data = String.format(resourceUrlConfig.getQrCodeDataUrl(), qrCodeId);
                        BigInteger creationTime = BigInteger.valueOf(System.currentTimeMillis());
                        byte[] qrCodeContent = this.generateQRCodeContentById(qrCodeId);
                        try {
                            QrCodeDTO qrCodeDTO = new QrCodeDTO(qrCodeId, qrCodeImageUrl, data, qrCodeContent);
                            qrCodeDTOS.add(qrCodeDTO);
                            qrCodeRepository.addQrCode(
                                    qrCodeId, qrCodeImageUrl, data, creationTime, email, batchId);
                        } catch (Exception e) {
                            log.error(batchId);
                            log.error("Error: {}", e.getMessage());
                        }
                    }

                    // Upload to S3
                    if (type.equals("zip")) {
                        try {
                            this.uploadZip(qrCodeDTOS, qrCodeCount, width, height, batchId, brand, defaultTemplate, templateUrl, null);
                        } catch (IOException e) {
                            log.error(e.getMessage());
                        }
                        key = String.format(resourceUrlConfig.getZipDirectory(), qrCodeCount, batchId, type);
                    } else if (type.equals("pdf")) {
                        try {
                            this.uploadPdf(qrCodeDTOS, qrCodeCount, width, height, batchId, brand, defaultTemplate, templateUrl, null);
                        } catch (IOException e) {
                            log.error(e.getMessage());
                        }
                        key =
                                String.format(
                                        resourceUrlConfig.getPdfDirectory(), qrCodeCount, width, height, batchId, type);
                    } else if (type.equals("txt")) {
                        try {
                            this.uploadText(qrCodeDTOS, qrCodeCount, batchId, null);
                        } catch (IOException e) {
                            log.error(e.getMessage());
                        }
                        key = String.format(resourceUrlConfig.getZipDirectory(), qrCodeCount, batchId, type);
                    }

                    int expirationMinutes = 60 * 24; // valid for 24hrs
                    String url =
                            awsService.getPreSignedUrlForFileUpload(key, expirationMinutes, HttpMethod.GET);
                    String encodedUrl = Base64.getEncoder().encodeToString(url.getBytes());
                    String link = resourceUrlConfig.getLink() + "?key=" + key + "&redirectUrl=" + encodedUrl;

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", emailTo);
                    jsonObject.put("to", emailTo);
                    jsonObject.put("link", link);
                    webClientAlertService.qrCodeAlert(jsonObject);
                    log.info("Sending QR Code alert.");

                    awsService.uploadQRCodesToS3(qrCodeDTOS, batchId);
                    userActionLogService.addUserActionLog(
                            email, "QrCode", "ADD", "Generated:" + qrCodeCount + " Qr-Code", "success");
                });
        executorService.shutdown();
    }

    public HttpHeaders getQrCodeRedirectionLink(String key, String redirectUrl) {
        return awsService.getQrCodeRedirectionLink(key, redirectUrl);
    }

    public ResponseEntity<ResponseDTO> getQrCodeRecordsByVdmsIdAndLastSyncTime(
            String orgId,
            String email,
            String vdmsId,
            BigInteger lastSyncTime,
            String loggedInUser,
            int pageNo,
            int pageSize,
            HttpServletRequest httpServletRequest) {
        log.info(
                "Payload: OrgId: {}, Email: {}, VdmsId: {}, LastSyncTime: {}, LoggedInUser: {},pageNo:{},pageSize:{}",
                orgId,
                email,
                vdmsId,
                lastSyncTime,
                loggedInUser,
                pageNo,
                pageSize);

        int offset = pageSize * (pageNo - 1);
        List<QrCodeDTO> qrCodeDTOList =
                qrCodeRepository.getQrCodeRecordsByVdmsIdAndLastSyncTime(vdmsId, lastSyncTime, pageSize, offset);

        if (qrCodeDTOList != null) {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(qrCodeDTOList, 200, true);
            log.info(
                    "Fetching QR Code Records By VdmsId: {} And LastSyncTime: {}. Endpoint: {}",
                    vdmsId,
                    lastSyncTime,
                    httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            log.error(
                    "QR Code Records does not exist, EndPoint: {}",
                    httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }
    }

    public ResponseEntity<?> updateQrCodeDetails(String vdmsId, List<QrCodeDTO> qrCodeDTOS, HttpServletRequest httpServletRequest) {
        log.info("Payload:QrCodeDTO:{},vdmsId:{}", qrCodeDTOS, vdmsId);

        qrCodeDTOS.forEach(qrCodeDTO -> {
            int checkQrCode = qrCodeRepository.checkQrCodeId(qrCodeDTO.getId());
            if (checkQrCode == 1) {
                vdmsService.updateQrCodeSyncByVdmsId(1, vdmsId, httpServletRequest);
                VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder()
                        .qr_sync(1)
                        .build();
                int isMultiTenant = vdmsService.getMultiTenantCheck(qrCodeDTO.getVdmsId());
                if (isMultiTenant == 1) {
                    String awsRegion = vdmsService.getAwsRegionByVdmsId(qrCodeDTO.getVdmsId());
                    webClientService.multiTenantSyncApiCall(qrCodeDTO.getVdmsId(), vdmsSyncDTO,awsRegion, httpServletRequest);
                } else {
                    socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + qrCodeDTO.getVdmsId() + "/sync/data", vdmsSyncDTO);
                }
                qrCodeRepository.updateQrCodeDetailsById(qrCodeDTO.getDeviceId(), qrCodeDTO.getLocationId(), qrCodeDTO.getUpdated_by(), qrCodeDTO.getUpdated_time(), 1, qrCodeDTO.getId());
            } else {
                log.info("qrcode id :{} not exist ", qrCodeDTO.getId());
                userActionLogService.addUserActionLog("successfully updated qr code details", "QrCode", "UPDATE", "QrCode not exist for id:" + qrCodeDTO.getId(), "failed");
            }
        });
        ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> getQrCodeCounts(String vdmsId, BigInteger lastSyncTime, HttpServletRequest httpServletRequest) {
        log.info("Payload:vdmsId:{},lastSyncTime:{}", vdmsId, lastSyncTime);
        int qrCodeCount = qrCodeRepository.getQrCodeCountsByVdsId(vdmsId, lastSyncTime);
        int clientQrCodeCount = clientQrCodeService.getClientQrCodeCounts(vdmsId, lastSyncTime, httpServletRequest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("qrCodeCount", qrCodeCount);
        jsonObject.put("clientQrCodeCount", clientQrCodeCount);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(jsonObject, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getUnTaggedQrCode(String vdmsId, String loggedInUser, int pageNo, int pageSize, HttpServletRequest httpServletRequest) {
        int offset = pageSize * (pageNo - 1);
        List<String> ids = qrCodeRepository.getUnTaggedQrCode(pageSize, offset);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(ids, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    private ByteArrayOutputStream exportQRCodesToPDF(List<QrCodeDTO> qrCodeDTOS, Float width, Float height, String templateUrl) throws IOException {
        log.info("Payload: Width:{}, Height:{}", width, height);
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();

        float pageWidth = width * 72;
        float pageHeight = height * 72;
        String fileName = awsService.getFilePathByImageUrl(templateUrl);
        byte[] templateBytes = awsService.downloadTemplateFromS3(fileName);

        try (PDDocument document = new PDDocument()) {
            for (QrCodeDTO qrCodeDTO : qrCodeDTOS) {
                PDPage page = new PDPage(new PDRectangle(pageWidth, pageHeight));
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                    PDImageXObject serveImage = PDImageXObject.createFromByteArray(document, templateBytes, "template.png");
                    contentStream.drawImage(serveImage, 0, 0, pageWidth, pageHeight);

                    PDImageXObject qrCodeImage = PDImageXObject.createFromByteArray(document, qrCodeDTO.getQrCodeContent(), "qrcode");
                    float qrSize = Math.min(pageWidth, pageHeight) * 0.75f;
                    float qrCodeX = (pageWidth - qrSize) / 2;
                    float centeredY = (pageHeight - qrSize) / 2;
                    float upwardShift = pageHeight * 0.17f;
                    float qrCodeY = centeredY + upwardShift;
                    contentStream.drawImage(qrCodeImage, qrCodeX, qrCodeY, qrSize, qrSize);
                }
            }
            document.save(pdfOutputStream);
        } catch (IOException e) {
            log.error("Error generating PDF: {}", e.getMessage());
            throw e;
        }
        return pdfOutputStream;
    }

    private ByteArrayOutputStream exportDefaultQRCodesTemplateWithContentToPDF(List<QrCodeDTO> qrCodeDTOS, Float width, Float height, boolean brand) throws IOException {
        log.info("Payload: Width:{}, Height:{}", width, height);
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument()) {
            float pageWidth = width * 72;
            float pageHeight = height * 72;
            for (QrCodeDTO qrCodeDTO : qrCodeDTOS) {
                PDPage page = new PDPage(new PDRectangle(pageWidth, pageHeight));
                document.addPage(page);
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    PDImageXObject serveImage = null;
                    if (brand) {
                        try (InputStream imageStream = getClass().getClassLoader().getResourceAsStream("images/serve_logo.png")) {
                            if (imageStream == null) {
                                throw new RuntimeException("Image file not found: images/serve_logo.png");
                            }
                            serveImage = PDImageXObject.createFromByteArray(document, imageStream.readAllBytes(), "serve_logo.png");
                            contentStream.drawImage(serveImage, 0, 0, pageWidth, pageHeight);
                        }
                    }
                    PDImageXObject qrCodeImage = PDImageXObject.createFromByteArray(document, qrCodeDTO.getQrCodeContent(), "qrcode");
                    float qrCodeWidth, qrCodeHeight;
                    if (Float.compare(width, height) == 0) {
                        qrCodeWidth = pageWidth * 0.63f;
                        qrCodeHeight = qrCodeWidth;
                    } else if (width > height) {
                        qrCodeWidth = pageHeight * 0.59f;
                        qrCodeHeight = qrCodeWidth;
                    } else {
                        qrCodeWidth = pageWidth * 0.90f;
                        qrCodeHeight = qrCodeWidth;
                    }
                    float qrCodeX = (pageWidth - qrCodeWidth) / 2;
                    float qrCodeY;

                    if (brand && serveImage != null) {
                        float verticalOffset = pageHeight * 0.075f;
                        qrCodeY = (pageHeight - qrCodeHeight) / 2 - verticalOffset;
                    } else {
                        qrCodeY = (pageHeight - qrCodeHeight) / 2;
                    }
                    contentStream.drawImage(qrCodeImage, qrCodeX, qrCodeY, qrCodeWidth, qrCodeHeight);
                }
            }

            document.save(pdfOutputStream);
        } catch (IOException e) {
            log.error("Error generating PDF: {}", e.getMessage());
            throw e;
        }

        return pdfOutputStream;
    }

    public ResponseEntity<ResponseDTO> tagQrCodeByVdmsId(String orgId, String vdmsId, List<QrCodeDTO> qrCodeDTOList, HttpServletRequest httpServletRequest) {
        log.info("Tagging {} qrCodes for vdmsId: {}, orgId: {}", qrCodeDTOList.size(), vdmsId, orgId);
        if (!qrCodeDTOList.isEmpty() && vdmsId != null) {
            BigInteger updatedAt = BigInteger.valueOf(System.currentTimeMillis());
            String batchId = Generators.timeBasedGenerator().generate().toString();
            for (QrCodeDTO qrCodeDTO : qrCodeDTOList) {
                qrCodeRepository.tagAdcQrCode(qrCodeDTO.getDeviceId(), qrCodeDTO.getLocationId(), vdmsId, qrCodeDTO.getUpdatedBy(), updatedAt,
                        batchId, 1, orgId, qrCodeDTO.getId());

                userActivityService.addUserActivityLogs(qrCodeDTO.getUpdatedBy(), "qr_code", qrCodeDTO.getDeviceId() != null ? "device" : "location", "UPDATE", "success",
                        "QrCode is tagged to " + (qrCodeDTO.getDeviceId() != null ? "Device ID: " + qrCodeDTO.getDeviceId() : "Location ID: " + qrCodeDTO.getLocationId()), qrCodeDTO.getDeviceId() != null ? qrCodeDTO.getDeviceId() : qrCodeDTO.getLocationId(), vdmsId);
                userActionLogService.addUserActionLog(qrCodeDTO.getUpdatedBy(), "QrCode", "UPDATE", "QrCode is tagged to " + (qrCodeDTO.getDeviceId() != null ? "Device ID: " + qrCodeDTO.getDeviceId() : "Location ID: " + qrCodeDTO.getLocationId()), "success");

            }
            vdmsService.updateQrCodeSyncByVdmsId(1, vdmsId, httpServletRequest);
            VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder()
                    .qr_sync(1)
                    .build();
            int isMultiTenant = vdmsService.getMultiTenantCheck(vdmsId);
            if (isMultiTenant == 1) {
                String awsRegion = vdmsService.getAwsRegionByVdmsId(vdmsId);
                webClientService.multiTenantSyncApiCall(vdmsId, vdmsSyncDTO,awsRegion, httpServletRequest);
            } else {
                socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdmsId + "/sync/data", vdmsSyncDTO);
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload("QrCode Details has been Updated Successfully", 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid client params, EndPoint:  {}", httpServletRequest.getRequestURI());
            userActivityService.addUserActivityLogs(null, "qr_code", null, "UPDATE", "failed", "Invalid client params", null, vdmsId);
            userActionLogService.addUserActionLog(null, "QrCode", "UPDATE", "Invalid client param", "failed");
            throw new ClientException("Invalid client params", 700, null);
        }
    }

    public ResponseEntity<ResponseDTO> getAdcCheckByQrCodeId(String qrCodeId) {
        log.info("Fetching adc check by qrCodeId: {}", qrCodeId);
        QrCodeDTO qrCodeDTO = new QrCodeDTO();
        int isPresentInDb = qrCodeRepository.getQrCodeId(qrCodeId);
        if (isPresentInDb == 0) {
            qrCodeDTO.setIsTagged(2);
        } else {
            int count = qrCodeRepository.getAdcCheckByQrCodeId(qrCodeId);
            if (count == 1) {
                qrCodeDTO.setIsAdc(true);
                int isTagged = qrCodeRepository.getIsAdcTagged(qrCodeId);
                qrCodeDTO.setIsTagged(isTagged);
            } else {
                int isManagedAssetsTagged = qrCodeRepository.getIsManagedAssetsTagged(qrCodeId);
                qrCodeDTO.setIsTagged(isManagedAssetsTagged == 1 ? 1 : 0);
                qrCodeDTO.setIsAdc(false);
            }
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload(qrCodeDTO, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> updateQrcodeById(String qrCodeId, QrCodeDTO qrCodeDTO) {
        log.info("Updating QrCode details for qrCodeId: {}", qrCodeId);
        BigInteger updatedAt = BigInteger.valueOf(System.currentTimeMillis());
        qrCodeRepository.updateQrCodeById(qrCodeDTO.getDeviceId(), qrCodeDTO.getUpdatedBy(), updatedAt, 1, qrCodeId);
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Qrcode has been tagged to device with id:" + qrCodeDTO.getDeviceId() + " successfully", 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
