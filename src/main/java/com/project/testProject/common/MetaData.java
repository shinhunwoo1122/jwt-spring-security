package com.project.testProject.common;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MetaData {
    private Integer totalCount;
    private boolean result;
    private String code;

    @Builder.Default
    private String message = "";
    private String action;
    private String mqIdx;
}