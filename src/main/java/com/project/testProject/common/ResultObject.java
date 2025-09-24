package com.project.testProject.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultObject<T> {
    private int totalCount;
    private boolean isSuccess;
    private boolean isEnd;
    private String code;
    private String message;
    private T data;
}
