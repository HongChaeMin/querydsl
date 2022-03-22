package com.example.study.querydsl.repository;

import com.example.study.querydsl.dto.MemberSearchCondition;
import com.example.study.querydsl.dto.MemberTeamDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {

    List<MemberTeamDTO> search(MemberSearchCondition condition);

    Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition, Pageable pageable);

    Page<MemberTeamDTO> searchPageComplex(MemberSearchCondition condition, Pageable pageable);

}
