package io.sclera.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProxyProfileDTO {

    private String id;
    private String name;
    private String public_ip;
    private String tcp_port;
    private String udp_port;
    private String host_machine;
    private String customer_org_id;
    private Integer is_deleted;
    private Integer is_global;
    private Integer ssl_enabled;
    private String latitude;
    private String longitude;


    public ProxyProfileDTO(String id, String name, String public_ip, String tcp_port, String udp_port,
                           String host_machine, String customer_org_id ,Integer is_global ,Integer ssl_enabled) {
        this.id = id;
        this.name = name;
        this.public_ip = public_ip;
        this.tcp_port = tcp_port;
        this.udp_port = udp_port;
        this.host_machine = host_machine;
        this.customer_org_id = customer_org_id;
        this.is_global = is_global;
        this.ssl_enabled = ssl_enabled;
    }

}
