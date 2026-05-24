package com.example.clinic.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
public class ImageUploadService {

    @Value("${imgbb.api.key:}")
    private String apiKey;

    private static final String IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload";

    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        byte[] bytes = file.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(bytes);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("key", apiKey);
        body.add("image", base64Image);
        body.add("name", file.getOriginalFilename());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                IMGBB_UPLOAD_URL,
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("data")) {
            Map data = (Map) responseBody.get("data");
            String url = (String) data.get("url");
            System.out.println("Image uploaded: " + url);
            return url;
        }

        return null;
    }
}