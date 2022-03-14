package com.example.study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDTO {

    private String userName;

    private int age;

    @QueryProjection
    public MemberDTO(String userName, int age) {
        this.userName = userName;
        this.age = age;
    }

}
