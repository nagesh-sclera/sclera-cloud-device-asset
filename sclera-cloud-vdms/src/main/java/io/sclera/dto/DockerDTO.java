package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigInteger;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DockerDTO {

    private String name;
    private String network_name;
    private String vdms_id;
    private String gateway;
    private String subnet;
    private Boolean host;
    private String public_ip_address;
    private String mac_address;
    private String system_type;
    private Boolean internet_status;
    private Boolean internet_required;
    private BigInteger internet_timestamp;
    private Boolean is_block;
    private BigInteger block_timestamp;
//    private String remote_access_id;
//    private Integer remote_access_permission;
//    private String remote_access_state;
//    private String remote_access_support;
//    private String remote_access_otp;
//    private String isp_id;
//    private String isp_account_number;
//    private String isp_name;
//    private String email;
//    private String phone;
//    private String phone_type;
//    private String value;
    private Integer cidr;
    private String external_ip_address;
    private String interface_in;
    private String interface_out;
    private String internal_ip_address;
    private String macvlan_name;
    private String primary_dns;
    private String secondary_dns;
    private String vlan_id;
    private Boolean is_static;
    private Boolean is_tagged;
    private String approval_status;
    private String vendor_org_id;
    private String invitee_org_id;
    private String invite_status;
    private String primary_proxy_profile_id;

    // Alert fields
    private String vendor_name;
    private Integer otp;

    // Vendor Invite
    private String vendor_email;

//    public DockerDTO(String name,String network_name ,String vdms_id, String gateway, Boolean host, String public_ip_address,
//                     String mac_address, String system_type, Boolean internet_status, Boolean internet_required,
//                     BigInteger internet_timestamp, Boolean is_block, BigInteger block_timestamp,
//                     String invitee_org_id, String invite_status, String remote_access_id,
//                     Integer remote_access_permission, String remote_access_state, String remote_access_support,
//                     String remote_access_otp, String isp_account_number, String isp_name, String email, String phone,
//                     String phone_type, String value) {
//        super();
//        this.name = name;
//        this.network_name = network_name;
//        this.vdms_id = vdms_id;
//        this.gateway = gateway;
//        this.host = host;
//        this.public_ip_address = public_ip_address;
//        this.mac_address = mac_address;
//        this.system_type = system_type;
//        this.internet_status = internet_status;
//        this.internet_required = internet_required;
//        this.internet_timestamp = internet_timestamp;
//        this.is_block = is_block;
//        this.block_timestamp = block_timestamp;
//        this.invitee_org_id = invitee_org_id;
//        this.invite_status = invite_status;
//        this.remote_access_id = remote_access_id;
//        this.remote_access_permission = remote_access_permission;
//        this.remote_access_state = remote_access_state;
//        this.remote_access_support = remote_access_support;
//        this.remote_access_otp = remote_access_otp;
//        this.isp_account_number = isp_account_number;
//        this.isp_name = isp_name;
//        this.email = email;
//        this.phone = phone;
//        this.phone_type = phone_type;
//        this.value = value;
//    }

    public DockerDTO(String name,String network_name ,String vdms_id, String gateway, Boolean host, String public_ip_address,
                     String mac_address, String system_type, Boolean internet_status, Boolean internet_required,
                     BigInteger internet_timestamp, Boolean is_block, BigInteger block_timestamp,
                     String invitee_org_id, String invite_status) {
        super();
        this.name = name;
        this.network_name = network_name;
        this.vdms_id = vdms_id;
        this.gateway = gateway;
        this.host = host;
        this.public_ip_address = public_ip_address;
        this.mac_address = mac_address;
        this.system_type = system_type;
        this.internet_status = internet_status;
        this.internet_required = internet_required;
        this.internet_timestamp = internet_timestamp;
        this.is_block = is_block;
        this.block_timestamp = block_timestamp;
        this.invitee_org_id = invitee_org_id;
        this.invite_status = invite_status;
    }

    public DockerDTO(String name, String network_name ,String vdms_id, String system_type, String invitee_org_id, String invite_status) {
        this.name = name;
        this.network_name = network_name;
        this.vdms_id = vdms_id;
        this.system_type = system_type;
        this.invitee_org_id = invitee_org_id;
        this.invite_status = invite_status;
    }
}
