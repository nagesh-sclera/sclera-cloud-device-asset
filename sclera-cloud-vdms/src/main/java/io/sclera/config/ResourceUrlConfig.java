package io.sclera.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class ResourceUrlConfig {


    @Value("${sclera.resourceLocations}")
    private String[] resourceLocations;

    @Value("${sclera.pathPatterns}")
    private String[] pathPatterns;

    @Value("${sclera.floor.image.url}")
    private String floorImageUrl;

    @Value("${sclera.floor.image.directory}")
    private String floorImageDirectory;

    @Value("${sclera.integration.image.url}")
    private String integrationImageUrl;

    @Value("${sclera.integration.image.directory}")
    private String integrationImageDirectory;

    @Value("${sclera.fx.app.directory}")
    private String scleraFXAppDirectory;

    @Value("${sclera.fx.app.url}")
    private String scleraFXAppUrl;

    @Value("${sclera.user.image.url}")
    private String userImageUrl;

    @Value("${sclera.user.image.directory}")
    private String userImageDirectory;

    @Value("${sclera.vdms.image.url}")
    private String vdmsImageUrl;

    @Value("${sclera.vdms.image.directory}")
    private String vdmsImageDirectory;

    @Value("${sclera.vdms.sql.url}")
    private String vdmsBackupUrl;

    @Value("${sclera.vdms.sql.directory}")
    private String vdmsBackupDirectory;

    @Value("${sclera.qrcode.url}")
    private String qrCodeUrl;

    @Value("${sclera.qrcode.directory}")
    private String qrCodeDirectory;

    @Value("${sclera.qrcode.dataUrl}")
    private String qrCodeDataUrl;

    @Value("${sclera.qrcode.file.directory}")
    private String qrCodeFileDirectory;

    @Value("${sclera.qrcode.file.url}")
    private String qrCodeFileUrl;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int batchSize;

    @Value("${sclera.qrcode.emailLink}")
    private String link;

    @Value("${sclera.qrcode.file.zipDirectory}")
    private String zipDirectory;

    @Value("${sclera.qrcode.file.txtDirectory}")
    private String txtDirectory;

    @Value("${sclera.qrcode.file.pdfDirectory}")
    private String pdfDirectory;

    @Value("${sclera.qrcode.file.errorPage}")
    private String errorPage;

    @Value("${sclera.feature.image.url}")
    private String featureImageUrl;

    @Value("${sclera.feature.image.directory}")
    private String featureImageDirectory;

    @Value("${sclera.digitalTwinTemplate.image.url}")
    private String digitalTwinTemplateUrl;

    @Value("${sclera.digitalTwinTemplate.image.directory}")
    private String digitalTwinTemplateDirectory;

    @Value("${sclera.category.image.directory}")
    private String categoryImageDirectory;

    @Value("${sclera.category.image.url}")
    private String categoryImageUrl;

    @Value("${sclera.subCategory.image.directory}")
    private String subCategoryImageDirectory;

    @Value("${sclera.subCategory.image.url}")
    private String subCategoryImageUrl;

    @Value("${sclera.device.url}")
    private String deviceImageUrl;

    @Value("${sclera.device.directory}")
    private String deviceImageDirectory;

    @Value("${sclera.floor.image.tilesDirectory}")
    private String floorTilesDirectory;

    @Value("${sclera.sensorCategory.image.directory}")
    private String sensorCategoryImageDirectory;

    @Value("${sclera.sensorCategory.image.url}")
    private String sensorCategoryImageUrl;

    @Value("${sclera.sensorSubCategory.image.directory}")
    private String sensorSubCategoryImageDirectory;

    @Value("${sclera.sensorSubCategory.image.url}")
    private String sensorSubCategoryImageUrl;

    @Value("${sclera.assetType.image.directory}")
    private String assetTypeImageDirectory;

    @Value("${sclera.assetType.image.url}")
    private String assetTypeImageUrl;

    @Value("${sclera.procedure.image.directory}")
    private String procedureImageDirectory;

    @Value("${sclera.procedure.image.url}")
    private String procedureImageUrl;

    @Value("${sclera.globalInspectionReport.file.folderDirectory}")
    private String downloadBulkFileDirectory;

    @Value("${sclera.globalInspectionReport.file.directory}")
    private String downloadFileDirectory;
    @Value("${sclera.globalInspectionReport.file.url}")
    private String downloadFileUrl;

    @Value("${sclera.inventory.file.directory}")
    private String inventoryFileDirectory;

    @Value("${sclera.inventory.file.url}")
    private String inventoryFileUrl;

    @Value("${sclera.inspectionReport.file.directory}")
    private String inspectionReportFileDirectory;

    @Value("${sclera.inspectionReport.file.url}")
    private String inspectionReportFileUrl;

    @Value("${sclera.sensorReport.file.directory}")
    private String sensorReportDirectory;

    @Value("${sclera.sensorReport.file.url}")
    private String sensorReportFileUrl;

    @Value("${sclera.onboardedAssetReport.file.directory}")
    private String onboardedAssetReportFileDirectory;

    @Value("${sclera.onboardedAssetReport.file.url}")
    private String onboardedAssetReportFileUrl;

    @Value("${sclera.locationCreateServiceReport.file.directory}")
    private String locationCreateReportFileDirectory;

    @Value("${sclera.locationCreateServiceReport.file.url}")
    private String locationCreateReportFileUrl;

    @Value("${sclera.reports.emailLink}")
    private String emailLink;

    @Value("${sclera.qrcode.template.url}")
    private String qrCodeTemplateUrl;

    @Value("${sclera.qrcode.template.directory}")
    private String qrCodeTemplateDirectory;

    @Value("${sclera.qrcode.templateLogo.url}")
    private String qrCodeTemplateLogoUrl;

    @Value("${sclera.qrcode.templateLogo.directory}")
    private String qrCodeTemplateLogoDirectory;
}
