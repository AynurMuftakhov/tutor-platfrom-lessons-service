package com.mytutorplatform.lessonsservice.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageAssetItem {
    private String url;
    private String fileName;
    private String mimeType;
    private long sizeBytes;
    private Integer width; // optional
    private Integer height; // optional
    private String createdAt; // ISO string
}
