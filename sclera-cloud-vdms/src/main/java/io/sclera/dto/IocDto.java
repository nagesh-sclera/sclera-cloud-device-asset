package io.sclera.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IocDto {

    private String id;
    private String name;
    private String server_url;
    private String web_url;
    private String customer_org;
    private String username;
    private String password;

    public IocDto(String id, String name, String server_url,String web_url, String customer_org) {
        this.id = id;
        this.name = name;
        this.server_url = server_url;
        this.web_url=web_url;
        this.customer_org = customer_org;
    }
}
