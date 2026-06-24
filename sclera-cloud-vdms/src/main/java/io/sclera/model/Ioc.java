package io.sclera.model;

import io.sclera.dto.IocDto;
import jakarta.persistence.*;

@Entity
@SqlResultSetMapping(
        name = "iocDataMapping",
        classes = {
                @ConstructorResult(
                        targetClass = IocDto.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "server_url", type = String.class),
                                @ColumnResult(name = "web_url", type = String.class),
                                @ColumnResult(name = "customer_org_id", type = String.class)
                        }
                )
        }
)
@NamedNativeQuery(
        name = "Ioc.getAllIocDetails",
        query = "SELECT id ,name ,server_url,web_url ,customer_org_id FROM `ioc`  ORDER BY id " ,
        resultSetMapping = "iocDataMapping"
)

@NamedNativeQuery(
        name = "Ioc.getIocDetailsByIocId",
        query = "SELECT id ,name ,server_url,web_url ,customer_org_id FROM `ioc` WHERE  id = ?1 ORDER BY id " ,
        resultSetMapping = "iocDataMapping"
)


@NamedNativeQuery(
        name = "Ioc.getAllIocDetailsByOrgId",
        query = "SELECT id ,name ,server_url,web_url ,customer_org_id FROM `ioc` WHERE customer_org_id = ?1 ORDER BY id " ,
        resultSetMapping = "iocDataMapping"
)

@NamedNativeQuery(
        name = "Ioc.getIocDetailsByIocIdAndOrgId",
        query = "SELECT id ,name ,server_url,web_url ,customer_org_id FROM `ioc` WHERE  id = ?1 AND customer_org_id = ?2 ORDER BY id " ,
        resultSetMapping = "iocDataMapping"
)


public class Ioc {

    @Id
    private String id;
    private String name;
    private String server_url;
    private String web_url;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id" , name = "customer_org_id")
    private Customer_Organisation customer_org;


}
