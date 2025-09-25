package com.project.testProject.controller;

import com.project.testProject.common.BaseResponse;
import com.project.testProject.common.CommonResponse;
import com.project.testProject.common.MetaData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/path")
public class PathController {

    @GetMapping("/admin")
    public ResponseEntity<BaseResponse> adminPathCheck(){

        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(MetaData.builder().result(true).code("200").message("관리자 체크 정상").build(), null));
    }

    @GetMapping("/manager")
    public ResponseEntity<BaseResponse> managerPathCheck(){

        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(MetaData.builder().result(true).code("200").message("매니저 체크 정상").build(), null));
    }

    @GetMapping("/user")
    public ResponseEntity<BaseResponse> userPathCheck(){

        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(MetaData.builder().result(true).code("200").message("유저 체크 정상").build(), null));
    }

}
