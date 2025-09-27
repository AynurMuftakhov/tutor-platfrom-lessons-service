package com.mytutorplatform.lessonsservice.model.response;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ImageAssetsPage {
    int total;
    List<ImageAssetItem> items;
}
