package io.sclera.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.sclera.dto.*;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Vdms.class)

@SqlResultSetMapping(
        name = "vdmsInfoMapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsDTO.class,
                        columns = {
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "property_name", type = String.class),
                                @ColumnResult(name = "activation_status", type = String.class),
                                @ColumnResult(name = "is_block", type = Boolean.class),
                                @ColumnResult(name = "creation_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "last_seen", type = BigInteger.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "block_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "customer_org_id", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class),
                                @ColumnResult(name = "address", type = String.class),
                                @ColumnResult(name = "city", type = String.class),
                                @ColumnResult(name = "country", type = String.class),
                                @ColumnResult(name = "state", type = String.class),
                                @ColumnResult(name = "zip", type = String.class),
                                @ColumnResult(name = "end_date", type = String.class),
                                @ColumnResult(name = "plan", type = String.class),
                                @ColumnResult(name = "progress", type = Integer.class),
                                @ColumnResult(name = "primary_proxy_profile_id", type = String.class),
                                @ColumnResult(name = "longitude", type = String.class),
                                @ColumnResult(name = "latitude", type = String.class),
                                @ColumnResult(name = "first_seen", type = BigInteger.class),
                                @ColumnResult(name = "deployment_type", type = String.class),
                                @ColumnResult(name = "activation_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "trialStatus", type = Boolean.class),
                                @ColumnResult(name = "trialStartDate", type = BigInteger.class),
                                @ColumnResult(name = "trialEndDate", type = BigInteger.class),
                                @ColumnResult(name = "assetCount", type = String.class),
                                @ColumnResult(name = "region", type = String.class),
                                @ColumnResult(name = "awsRegion", type = String.class),
                                @ColumnResult(name = "isMultiTenant", type = Integer.class)
                        }
                )
        }
)


@SqlResultSetMapping(
        name = "vdmsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsDTO.class,
                        columns = {
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "property_name", type = String.class),
                                @ColumnResult(name = "activation_status", type = String.class),
                                @ColumnResult(name = "is_block", type = Boolean.class),
                                @ColumnResult(name = "creation_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "last_seen", type = BigInteger.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "block_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "customer_org_id", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class),
                                @ColumnResult(name = "address", type = String.class),
                                @ColumnResult(name = "city", type = String.class),
                                @ColumnResult(name = "country", type = String.class),
                                @ColumnResult(name = "state", type = String.class),
                                @ColumnResult(name = "zip", type = String.class),
                                @ColumnResult(name = "end_date", type = String.class),
                                @ColumnResult(name = "plan", type = String.class),
                                @ColumnResult(name = "progress", type = Integer.class),
                                @ColumnResult(name = "primary_proxy_profile_id", type = String.class),
                                @ColumnResult(name = "longitude", type = String.class),
                                @ColumnResult(name = "latitude", type = String.class),
                                @ColumnResult(name = "first_seen", type = BigInteger.class),
                                @ColumnResult(name = "deployment_type", type = String.class),
                                @ColumnResult(name = "activation_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "region", type = String.class),
                                @ColumnResult(name = "awsRegion", type = String.class)
                        }
                )
        }
)


@SqlResultSetMapping(
        name = "quicksearchmapping",
        classes = {
                @ConstructorResult(
                        targetClass = QuickSearchDTO.class,
                        columns = {
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "property_name", type = String.class)
                        }
                )
        }
)

@SqlResultSetMapping(
        name = "vdmssyncmapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsSyncDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "user_sync", type = Integer.class),
                                @ColumnResult(name = "image_sync", type = Integer.class),
                                @ColumnResult(name = "proxy_server_host_sync", type = Integer.class),
                                @ColumnResult(name = "proxy_client_sync", type = Integer.class),
                                @ColumnResult(name = "service_value_sync", type = Integer.class),
                                @ColumnResult(name = "vdms_transfer", type = Integer.class),
                                @ColumnResult(name = "qr_sync", type = Integer.class),
                                @ColumnResult(name = "nfc_sync", type = Integer.class),
                                @ColumnResult(name = "barcode_sync", type = Integer.class),
                                @ColumnResult(name = "corrigo_sync", type = Integer.class)
                        }
                )
        }
)

@SqlResultSetMapping(
        name = "vdmsproxyprofilemapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "property_name", type = String.class),
                                @ColumnResult(name = "primary_proxy_profile_id", type = String.class),
                                @ColumnResult(name = "proxy_profile_name", type = String.class),
                                @ColumnResult(name = "lastSeen", type = BigInteger.class),
                                @ColumnResult(name = "first_seen", type = BigInteger.class),
                        }
                )
        }
)


@SqlResultSetMapping(
        name = "vdmsstatusmapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "last_seen", type = BigInteger.class),
                                @ColumnResult(name = "property_name", type = String.class)
                        }
                )
        }
)

@SqlResultSetMapping(
        name = "statusmapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "last_seen", type = BigInteger.class),
                        }
                )
        }
)


@NamedNativeQuery(
        name = "Vdms.getProxyProfilesTaggedToVdmsIdByOrganisationId",
        query = "SELECT v.id , v.property_name ,v.primary_proxy_profile_id ,pp.name AS proxy_profile_name,v.last_seen As lastSeen,v.first_seen FROM vdms v " +
                "LEFT JOIN proxy_profile pp ON pp.id = v.primary_proxy_profile_id WHERE v.customer_org_id = ?1",
        resultSetMapping = "vdmsproxyprofilemapping"
)


@NamedNativeQuery(
        name = "Vdms.getVdmsSyncByVdmsId",
        query = "SELECT v.id , v.user_sync ,v.image_sync ,v.proxy_server_host_sync ,v.proxy_client_sync ,v.service_value_sync," +
                " v.vdms_transfer, v.qr_sync, v.nfc_sync, v.bar_code_sync AS barcode_sync, v.corrigo_sync FROM vdms v WHERE v.id = ?1",
        resultSetMapping = "vdmssyncmapping"
)

@NamedNativeQuery(
        name = "Vdms.getQuickSearchListByMasterUserOrganisationId",
        query = "SELECT v.id AS vdms_id , v.property_name FROM vdms v WHERE v.customer_org_id = ?1",
        resultSetMapping = "quicksearchmapping"
)

@NamedNativeQuery(
        name = "Vdms.getQuickSearchListByUserOrganisationIdAndEmail",
        query = "SELECT v.id AS vdms_id , v.property_name FROM vdms v RIGHT JOIN vdms_visibility vv ON v.id = vv.vdms_id "
                + "WHERE v.customer_org_id = ?1 AND vv.email = ?2",
        resultSetMapping = "quicksearchmapping"
)

@NamedNativeQuery(
        name = "Vdms.getQuickSearchListByMasterVendorOrganisationId",
        query = "SELECT DISTINCT v.id AS vdms_id , v.property_name FROM vdms v LEFT JOIN docker d ON v.id = d.vdms_id "
                + "WHERE d.vendor_org_id = ?1",
        resultSetMapping = "quicksearchmapping"
)

@NamedNativeQuery(
        name = "Vdms.getQuickSearchListByVendorOrganisationIdAndEmail",
        query = "SELECT DISTINCT v.id AS vdms_id ,v.property_name FROM vdms v LEFT JOIN docker d ON v.id = d.vdms_id "
                + "RIGHT JOIN vdms_visibility vv ON v.id = vv.vdms_id "
                + "WHERE d.vendor_org_id = ?1 AND vv.email = ?2 ",
        resultSetMapping = "quicksearchmapping"
)

@NamedNativeQuery(
        name = "Vdms.getQuickSearchListByPropertyAdminOrganisationIdAndEmail",
        query = "SELECT DISTINCT v.id AS vdms_id ,v.property_name FROM vdms v LEFT JOIN docker d ON v.id = d.vdms_id "
                + "RIGHT JOIN vdms_visibility vv ON v.id = vv.vdms_id "
                + "WHERE d.customer_org_id = ?1 AND vv.email = ?2 ",
        resultSetMapping = "quicksearchmapping"
)

@NamedNativeQuery(
        name = "Vdms.getQuickSearchListByAdmin",
        query = "SELECT v.id AS vdms_id ,v.property_name FROM vdms v ",
        resultSetMapping = "quicksearchmapping"
)
@NamedNativeQuery(
        name = "Vdms.getVdmsPropertyInfoByVdmsId",
        query = "SELECT v.id AS vdms_id ,v.property_name FROM vdms v WHERE v.id IN (?1) ",
        resultSetMapping = "quicksearchmapping"
)

@NamedNativeQuery(
        name = "Vdms.getVdmsInfoByVdmsId",
        query = "SELECT v.id AS vdms_id , v.property_name ,v.activation_status ,v.is_block ,v.creation_timestamp ,v.last_seen ,"
                + "v.status ,v.block_timestamp ,"
                + "v.customer_org_id , v.end_date ,v.plan ,v.progress ,v.primary_proxy_profile_id ,v.image_url ," +
                " v.latitude ,v.longitude ,a.address ,a.city ,a.country ,a.state ,a.zip,v.first_seen, v.deployment_type,v.activation_timestamp, v.region, " +
                " v.trial_status AS trialStatus, v.trial_start_date AS trialStartDate, v.trial_end_date AS trialEndDate,v.asset_count AS assetCount, " +
                "v.aws_region AS awsRegion, v.is_multi_tenant AS isMultiTenant "
                + " FROM vdms v LEFT JOIN address a ON v.address_id = a.id WHERE v.id = ?1",
        resultSetMapping = "vdmsInfoMapping"
)

@NamedNativeQuery(
        name = "Vdms.getAllVdmsInfoByMasterUserOrganisationId",
        query = "SELECT v.id AS vdms_id , v.property_name,v.activation_status ,v.is_block ,v.creation_timestamp ,v.last_seen ,"
                + "v.status ,v.block_timestamp ,"
                + "v.customer_org_id , v.end_date ,v.plan ,v.progress ,v.primary_proxy_profile_id,v.image_url ," +
                " v.latitude ,v.longitude , a.address ,a.city ,a.country ,a.state ,a.zip,v.first_seen,v.deployment_type,v.activation_timestamp, v.region, " +
                " v.trial_status AS trialStatus, v.trial_start_date AS trialStartDate, v.trial_end_date AS trialEndDate,v.asset_count AS assetCount, " +
                " v.aws_region AS awsRegion, v.is_multi_tenant AS isMultiTenant "
                + " FROM vdms v " +
                "LEFT JOIN address a ON v.address_id = a.id "
                + "WHERE (v.customer_org_id = ?1 "
                + "AND (?2 = 'all' "
                + "OR CONCAT_WS('',v.id,v.property_name) LIKE CONCAT('%',?2,'%'))) " +
                "ORDER BY CASE " +
                "WHEN ?3 = 'vdms_id' THEN v.id " +
                "ELSE v.property_name " +
                "END " +
                "LIMIT ?4 OFFSET ?5",
        resultSetMapping = "vdmsInfoMapping"
)


@NamedNativeQuery(
        name = "Vdms.getAllVdmsInfoByUserOrganisationIdAndUserEmail",
        query = "SELECT v.id AS vdms_id , v.property_name ,v.activation_status ,v.is_block ,v.creation_timestamp ,v.last_seen ,"
                + "v.status,v.block_timestamp ,"
                + "v.customer_org_id , v.end_date ,v.plan ,v.progress ,v.primary_proxy_profile_id ,v.image_url,a.address ," +
                " v.latitude ,v.longitude ,a.city ,a.country ,a.state ,a.zip,v.first_seen, v.deployment_type,v.activation_timestamp, v.region, " +
                " v.trial_status AS trialStatus, v.trial_start_date AS trialStartDate, v.trial_end_date AS trialEndDate,v.asset_count AS assetCount, " +
                " v.aws_region AS awsRegion, v.is_multi_tenant AS isMultiTenant "
                + " FROM vdms v LEFT JOIN address a ON v.address_id = a.id RIGHT JOIN vdms_visibility vv ON v.id = vv.vdms_id "
                + "WHERE (v.customer_org_id = ?1 "
                + "AND vv.email = ?2 "
                + "AND (?3 = 'all' "
                + "OR CONCAT_WS('',v.id,v.property_name) LIKE CONCAT('%',?3,'%'))) " +
                "ORDER BY CASE " +
                "WHEN ?4 = 'vdms_id' THEN v.id " +
                "ELSE v.property_name " +
                "END " +
                "LIMIT ?5 OFFSET ?6",
        resultSetMapping = "vdmsInfoMapping"
)

@NamedNativeQuery(
        name = "Vdms.getAllVdmsInfoByMasterVendorOrganisationId",
        query = "SELECT DISTINCT v.id AS vdms_id , v.property_name,v.activation_status ,v.is_block ,v.creation_timestamp ,v.last_seen ,"
                + "v.status,v.block_timestamp ,"
                + "v.customer_org_id ,v.end_date ,v.plan ,v.progress ,v.primary_proxy_profile_id,v.image_url ," +
                " v.latitude ,v.longitude ,a.address ,a.city ,a.country ,a.state ,a.zip,v.first_seen,v.deployment_type,v.activation_timestamp, v.region," +
                " v.aws_region AS awsRegion "
                + " FROM vdms v LEFT JOIN address a ON v.address_id = a.id LEFT JOIN docker d ON d.vdms_id = v.id "
                + " WHERE ((d.vendor_org_id = ?1 OR d.invitee_org_id = ?1) AND (invite_status = 'invited' OR invite_status IS NULL) "
                + "AND (?2 = 'all' "
                + "OR CONCAT_WS('',v.id,v.property_name) LIKE CONCAT('%',?2,'%'))) " +
                "ORDER BY CASE " +
                "WHEN ?3 = 'vdms_id' THEN v.id " +
                "ELSE v.property_name " +
                "END " +
                "LIMIT ?4 OFFSET ?5 ",
        resultSetMapping = "vdmsmapping"
)


@NamedNativeQuery(
        name = "Vdms.getAllVdmsInfoByVendorOrganisationIdAndVendorEmail",
        query = "SELECT DISTINCT v.id AS vdms_id , v.property_name ,v.activation_status ,v.is_block ,v.creation_timestamp ,v.last_seen ,"
                + "v.status ,v.block_timestamp ,"
                + "v.customer_org_id ,v.end_date ,v.plan ,v.progress ,v.primary_proxy_profile_id,v.image_url,v.latitude ,v.longitude ," +
                " a.address ,a.city ,a.country ,a.state ,a.zip,v.first_seen,v.deployment_type,v.activation_timestamp, v.region, v.aws_region AS awsRegion "
                + " FROM vdms v LEFT JOIN address a ON v.address_id = a.id LEFT JOIN docker d ON d.vdms_id = v.id "
                + "	RIGHT JOIN vdms_visibility vv ON v.id = vv.vdms_id "
                + "	WHERE (d.vendor_org_id = ?1 AND vv.email = ?2 "
                + "AND (?3 = 'all' "
                + "OR CONCAT_WS('',v.id,v.property_name) LIKE CONCAT('%',?3,'%'))) " +
                "ORDER BY CASE " +
                "WHEN ?4 = 'vdms_id' THEN v.id " +
                "ELSE v.property_name " +
                "END " +
                "LIMIT ?5 OFFSET ?6 ",
        resultSetMapping = "vdmsmapping"
)


@NamedNativeQuery(
        name = "Vdms.getAllVdmsInfo",
        query = "SELECT v.id AS vdms_id , v.property_name ,v.activation_status ,v.is_block ,v.creation_timestamp ,v.last_seen ,"
                + "v.status ,v.block_timestamp ,"
                + "v.customer_org_id ,v.end_date ,v.plan ,v.progress ,v.primary_proxy_profile_id ,v.image_url,v.latitude ,v.longitude ," +
                " a.address ,a.city ,a.country ,a.state ,a.zip,v.first_seen,v.deployment_type,v.activation_timestamp, v.region, " +
                " v.trial_status AS trialStatus, v.trial_start_date AS trialStartDate, v.trial_end_date AS trialEndDate,v.asset_count AS assetCount, " +
                " v.aws_region AS awsRegion, v.is_multi_tenant AS isMultiTenant "
                + " FROM vdms v LEFT JOIN address a ON v.address_id = a.id "
                + "WHERE (?1 = 'all' "
                + "OR CONCAT_WS('',v.id,v.property_name) LIKE CONCAT('%',?1,'%')) " +
                "ORDER BY CASE " +
                "WHEN ?2 = 'vdms_id' THEN v.id " +
                "ELSE v.property_name " +
                "END " +
                "LIMIT ?3 OFFSET ?4",
        resultSetMapping = "vdmsInfoMapping"
)

@NamedNativeQuery(
        name = "Vdms.getStatus",
        query = "SELECT id,last_seen FROM vdms ",
        resultSetMapping = "statusmapping"
)

@NamedNativeQuery(
        name = "Vdms.getVdmsByKey",
        query = "SELECT id,last_seen,property_name FROM vdms " +
                "WHERE (?1 ='all' OR CONCAT_WS('',id,property_name) LIKE CONCAT('%',?1,'%')) ",
        resultSetMapping = "vdmsstatusmapping"
)


@SqlResultSetMapping(
        name = "externalvdmsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = ExternalClientUserDTO.class,
                        columns = {
                                @ColumnResult(name = "vdmsId", type = String.class),
                                @ColumnResult(name = "propertyName", type = String.class)
                        }
                )
        }
)

@SqlResultSetMapping(
        name = "vdmsalertmapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "last_seen", type = BigInteger.class),
                                @ColumnResult(name = "property_name", type = String.class),
                                @ColumnResult(name = "email_alert", type = Integer.class)
                        }
                )
        }
)
@SqlResultSetMapping(
        name = "vdmsListMapping",
        classes = {
                @ConstructorResult(
                        targetClass = ExternalClientUserDTO.class,
                        columns = {
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "vdmsId", type = String.class),
                                @ColumnResult(name = "propertyName", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Vdms.getExternalVdmsPropertyInfoByOrgId",
        query = "SELECT v.id AS vdmsId ,v.property_name AS propertyName FROM vdms v WHERE v.customer_org_id = ?1 ",
        resultSetMapping = "externalvdmsmapping"
)

@NamedNativeQuery(
        name = "Vdms.getVdmsAlertData",
        query = "SELECT id,last_seen,property_name, email_alert FROM vdms ",
        resultSetMapping = "vdmsalertmapping"
)

@NamedNativeQuery(
        name = "Vdms.getVdmsIdsByEmailList",
        query = "SELECT CONCAT(v.property_name, ' (', v.id, ')') AS vdmsId ,u.email,v.property_name AS propertyName" +
                " FROM vdms v " +
                "LEFT JOIN customer_organisation co ON co.id = v.customer_org_id " +
                "LEFT JOIN user u ON u.customer_org_id = co.id WHERE u.email IN ?1 ",
        resultSetMapping = "vdmsListMapping"
)

@SqlResultSetMapping(
        name = "vdmsPropertySummaryMapping",
        classes = {
                @ConstructorResult(
                        targetClass = PropertySummaryDTO.class,
                        columns = {
                                @ColumnResult(name = "propertyName", type = String.class),
                                @ColumnResult(name = "deploymentType", type = String.class),
                                @ColumnResult(name = "assetCount", type = Integer.class),
                                @ColumnResult(name = "country", type = String.class),
                                @ColumnResult(name = "tier", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Vdms.getBillingInfoPropertySummaryByOrgId",
        query = """
                SELECT 
                    v.property_name AS propertyName,
                    v.deployment_type AS deploymentType,
                    v.asset_count AS assetCount,
                    a.country AS country,
                    ct.tier AS tier 
                FROM vdms v
                LEFT JOIN address a ON v.id = a.id
                LEFT JOIN country_tier ct ON a.country = ct.country 
                WHERE v.customer_org_id = :orgId 
                AND v.id IN (:selectedVdmsList)
                LIMIT :pageSize OFFSET :offset
                """,
        resultSetMapping = "vdmsPropertySummaryMapping"
)

@SqlResultSetMapping(
        name = "vdmsPropertyTierSummaryMapping",
        classes = {
                @ConstructorResult(
                        targetClass = TierCountSummaryDTO.class,
                        columns = {
                                @ColumnResult(name = "tierOneCount", type = Integer.class),
                                @ColumnResult(name = "tierTwoCount", type = Integer.class),
                                @ColumnResult(name = "tierThreeCount", type = Integer.class),
                                @ColumnResult(name = "tierTypeCount", type = Integer.class),
                                @ColumnResult(name = "totalOnboardedAssets", type = Integer.class),
                                @ColumnResult(name = "totalLicensedAssets", type = Integer.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Vdms.getPropertyTierSummaryByOrgId",
        query = """
                SELECT 
                    COUNT(CASE WHEN ct.tier = 'Tier 1' THEN 1 END) AS tierOneCount,
                    COUNT(CASE WHEN ct.tier = 'Tier 2' THEN 1 END) AS tierTwoCount,
                    COUNT(CASE WHEN ct.tier = 'Tier 3' THEN 1 END) AS tierThreeCount,
                    COUNT(DISTINCT ct.tier) AS tierTypeCount,
                    COALESCE((SELECT SUM(vd.asset_count) FROM vdms vd WHERE vd.customer_org_id = :orgId AND vd.id IN (:selectedVdmsList)), 0) AS totalOnboardedAssets,
                    b.total_licensed_assets AS totalLicensedAssets
                FROM vdms v
                LEFT JOIN address a ON v.id = a.id
                LEFT JOIN country_tier ct ON a.country = ct.country 
                LEFT JOIN billing_info b ON b.org_id = v.customer_org_id
                WHERE v.customer_org_id = :orgId AND b.billing_id = :billingId AND v.id IN (:selectedVdmsList)
                """,
        resultSetMapping = "vdmsPropertyTierSummaryMapping"
)

@NamedNativeQuery(
        name = "Vdms.getExternalClientVdmsPropertyInfoByOrganisationId",
        query = "SELECT v.id AS vdmsId ,v.property_name AS propertyName FROM vdms v WHERE v.customer_org_id = ?1 LIMIT ?2 OFFSET ?3 ",
        resultSetMapping = "externalvdmsmapping"
)

@NamedNativeQuery(
        name = "Vdms.getScleraAgentVdmsInfo",
        query = "SELECT v.id AS vdmsId ,v.property_name AS propertyName FROM vdms v WHERE (v.id IN ?1 AND (?2 = 'all' "
                + "OR CONCAT_WS('',v.id,v.property_name) LIKE CONCAT('%',?2,'%'))) LIMIT ?3 OFFSET ?4",
        resultSetMapping = "externalvdmsmapping"
)

@NamedNativeQuery(
        name = "Vdms.getScleraAgentVdmsInfoByMasterUser",
        query = "SELECT v.id AS vdmsId ,v.property_name AS propertyName FROM vdms v WHERE (v.customer_org_id = ?1 AND (?2 = 'all' "
                + "OR CONCAT_WS('',v.id,v.property_name) LIKE CONCAT('%',?2,'%'))) LIMIT ?3 OFFSET ?4",
        resultSetMapping = "externalvdmsmapping"
)

@NamedNativeQuery(
        name = "Vdms.getVdmsListByOrganisationId",
        query = "SELECT id, last_seen, property_name FROM vdms WHERE customer_org_id = ?1",
        resultSetMapping = "vdmsstatusmapping"
)

@NamedNativeQuery(
        name = "Vdms.getAllVdmsInfoByOrganisationId",
        query = "SELECT v.id AS vdms_id , v.property_name,v.activation_status ,v.is_block ,v.creation_timestamp ,v.last_seen ,"
                + "v.status ,v.block_timestamp ,"
                + "v.customer_org_id , v.end_date ,v.plan ,v.progress ,v.primary_proxy_profile_id,v.image_url ," +
                " v.latitude ,v.longitude , a.address ,a.city ,a.country ,a.state ,a.zip,v.first_seen,v.deployment_type,v.activation_timestamp, v.region, " +
                " v.trial_status AS trialStatus, v.trial_start_date AS trialStartDate, v.trial_end_date AS trialEndDate,v.asset_count AS assetCount, " +
                " v.aws_region AS awsRegion, v.is_multi_tenant AS isMultiTenant "
                + " FROM vdms v "
                + "LEFT JOIN address a ON v.address_id = a.id "
                + "WHERE v.customer_org_id = ?1 ",
        resultSetMapping = "vdmsInfoMapping"
)

public class Vdms {

    @Id
    @Column(length = 64)
    private String id;

    @Column(length = 64)
    private String property_name;

    @Column(length = 16, columnDefinition = "varchar(255) default 'Not Activated'")        // not-activated
    private String activation_status;

    @Column(columnDefinition = "boolean default false")
    private Boolean is_block;

    @Column
    private BigInteger creation_timestamp;

    @Column(columnDefinition = "bigint default 0")
    private BigInteger last_seen;

    @Column(length = 32, columnDefinition = "varchar(255) default 'Not Subscribed'")    // subscription not created
    private String status;

    @Column(length = 32)
    private String end_date;

    @Column(length = 32)
    private String plan;

    @Column(length = 8)
    private Integer progress;

    @Column
    private BigInteger block_timestamp;

    @Column(columnDefinition = "bigint default 0")
    private BigInteger last_updated;

    @ManyToOne
    private Customer_Organisation customer_org;

    @OneToMany(mappedBy = "vdms", cascade = CascadeType.ALL)
    private Set<Docker> docker;

    @OneToOne(cascade = CascadeType.ALL)
    private Address address;

    @OneToOne(cascade = CascadeType.ALL)
    private VdmsCoordinates vdmsCoordinates;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vdms")
    private Set<VdmsIntegration> vdmsIntegrations;

    @Column(columnDefinition = "integer default 1")
    private Integer user_sync;

    @Column(columnDefinition = "integer default 1")
    private Integer image_sync;

    @Column(columnDefinition = "integer default 1")
    private Integer proxy_server_host_sync;

    @Column(columnDefinition = "integer default 1")
    private Integer proxy_client_sync;

    @Column(columnDefinition = "integer default 0")
    private Integer service_value_sync;

    @Column(length = 64)
    private String primary_proxy_profile_id;

    @Column(length = 64)
    private String secondary_proxy_profile_id;

    @Column(columnDefinition = "TEXT")
    private String image_url;

    @Column(length = 64)
    private String devuid;

    @Column(length = 64)
    private String longitude;

    @Column(length = 64)
    private String latitude;

    @Column(columnDefinition = "integer default 0")
    private Integer vdms_transfer;

    @OneToMany(mappedBy = "vdms", cascade = CascadeType.ALL)
    private List<QrCode> qrCode;

    @OneToMany(mappedBy = "vdms", cascade = CascadeType.ALL)
    private List<NFC> nfc;

    private BigInteger firstSeen;

    @Column(columnDefinition = "bigint default 0")
    private BigInteger activation_timestamp;

    @Column(length = 16, columnDefinition = "varchar(16) default 'on_premises'")
    private String deployment_type;

    @Column(columnDefinition = "integer default 1")
    private Integer qr_sync;

    @Column(columnDefinition = "integer default 1")
    private Integer nfc_sync;

    @Column(columnDefinition = "integer default 1")
    private Integer barCode_sync;

    @Column(columnDefinition = "integer default 0")
    private Integer email_alert;

    @Column(columnDefinition = "integer default 0")
    private Integer assetCount;

    @Column(columnDefinition = "boolean default false")
    private Boolean trialStatus;

    @Column(columnDefinition = "bigint default 0")
    private BigInteger trialStartDate;

    @Column(columnDefinition = "bigint default 0")
    private BigInteger trialEndDate;

    @Column(length = 32)
    private String region;

    @Column(length = 64)
    private String corrigoConfigId;

    @Column(columnDefinition = "integer default 0")
    private Integer corrigoSync;

    @Column(columnDefinition = "bigint default 0")
    private BigInteger isMultiTenant;

    @Column(length = 32)
    private String awsRegion;

    public Vdms() {
    }

    public Vdms(String id) {
        super();
        this.id = id;
    }


}
