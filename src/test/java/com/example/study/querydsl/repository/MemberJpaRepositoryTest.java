package com.example.study.querydsl.repository;

import com.example.study.querydsl.dto.MemberSearchCondition;
import com.example.study.querydsl.dto.MemberTeamDTO;
import com.example.study.querydsl.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MemberJpaRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Test
    public void basicTest() {
        Member member = memberJpaRepository.findById(2022).get();
        assertThat(member.getId()).isEqualTo(2022);

        List<Member> result1 = memberJpaRepository.findAll();

        List<Member> result2 = memberJpaRepository.findByUserName("member3");
        assertThat(result2.get(0).getId()).isEqualTo(2022);
    }

    @Test
    public void querydslTest() {
        Member member = memberJpaRepository.findById(2022).get();
        assertThat(member.getId()).isEqualTo(2022);

        List<Member> result1 = memberJpaRepository.findAll_Querydsl();

        List<Member> result2 = memberJpaRepository.findByUserName_Querydsl("member3");
        assertThat(result2.get(0).getId()).isEqualTo(2022);
    }

    @Test
    public void searchTest() {
        MemberSearchCondition condition = new MemberSearchCondition();

        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        List<MemberTeamDTO> list = memberJpaRepository.searchByBuilder(condition);

        assertThat(list.get(0).getUserName()).isEqualTo("member4");

        List<MemberTeamDTO> result = memberJpaRepository.search(condition);

        assertThat(result.get(0).getUserName()).isEqualTo("member4");

    }

}