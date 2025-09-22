package com.project.testProject.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommonResponse<T> implements BaseResponse {

    private MetaData meta;
    private T data;


    public String toJsonString() throws JsonProcessingException {
        var om = new ObjectMapper();
        om.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        return om.writeValueAsString(this);
    }
}
