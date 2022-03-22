package com.example.study.querydsl.repository;

import com.example.study.querydsl.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    List<Member> findByUserName(String userName);

    // 특정 api 종속되어 있거나 너무 특화되어 있으면 별도로 조회용 레파지토리를 만드는 것도 방법이다!
    // (기본은 커스텀 레파지토리가 맞음)

}
