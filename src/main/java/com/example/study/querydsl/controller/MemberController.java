package com.example.study.querydsl.controller;

import com.example.study.querydsl.dto.MemberSearchCondition;
import com.example.study.querydsl.dto.MemberTeamDTO;
import com.example.study.querydsl.repository.MemberJpaRepository;
import com.example.study.querydsl.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MemberController {

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDTO> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.search(condition);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDTO> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageSimple(condition, pageable);
    }

    @GetMapping("/v3/members")
    public Page<MemberTeamDTO> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageComplex(condition, pageable);
    }

}
