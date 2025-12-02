package com.rudraksha.shopsphere.media.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMediaRequest {

    private String altText;
    private Boolean isPrimary;
}
