package com.example.clinic.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


@Service
public class ImageUploadService {
    @Value("$defaultuploader.secret.key")
    private String secretKey;
    @Value("$defualtuploader.upload.link")
    private String url;
    private RestTemplate restTemplate = new RestTemplate();

    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()){
            return null;
        }
        HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(),createHeader());
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST,entity,Map.class);
        Map body = response.getBody();
        return body != null ? (String) body.get("url") : null;
    }
    private HttpHeaders createHeader(){
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        header.set("Authorization",secretKey);
        return header;
    }
}
