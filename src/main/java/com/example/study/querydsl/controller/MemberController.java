package com.example.study.querydsl.controller;

import com.example.study.querydsl.dto.MemberSearchCondition;
import com.example.study.querydsl.dto.MemberTeamDTO;
import com.example.study.querydsl.repository.MemberJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MemberController {

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDTO> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.search(condition);
    }



}
