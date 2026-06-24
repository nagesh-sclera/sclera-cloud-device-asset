package io.sclera.dto;

import com.fasterxml.uuid.Generators;
import io.sclera.service.ConvertByteArrayToMultipartFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MultipartFileDTO {
    private  String id;
    private  byte[] bytes;
    private  String originalFilename;
    private  String name;
    private  String contentType;

    public MultipartFileDTO(MultipartFile multipartFile) throws IOException{
        this.id = Generators.timeBasedGenerator().generate().toString();
        this.bytes = multipartFile.getBytes();
        this.originalFilename = multipartFile.getOriginalFilename();
        this.name = multipartFile.getName();
        this.contentType = multipartFile.getContentType();
    }

    public MultipartFile toMultipartFile(){
        return new ConvertByteArrayToMultipartFile(this.bytes, this.originalFilename, this.name, this.contentType);
    }
}
