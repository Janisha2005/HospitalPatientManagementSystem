package com.hospital.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDto {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private Boolean isActive;
}
