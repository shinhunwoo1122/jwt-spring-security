package com.project.testProject.controller;

import com.project.testProject.common.BaseResponse;
import com.project.testProject.common.CommonResponse;
import com.project.testProject.common.MetaData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<BaseResponse> test(){

        var name = "신헌우";

        return ResponseEntity.ok(new CommonResponse<>(MetaData.builder().result(true).message("hello").build(), null));
    }

}
