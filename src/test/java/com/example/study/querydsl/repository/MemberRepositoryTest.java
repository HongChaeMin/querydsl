package com.example.study.querydsl.repository;

import com.example.study.querydsl.dto.MemberSearchCondition;
import com.example.study.querydsl.dto.MemberTeamDTO;
import com.example.study.querydsl.entity.Member;
import com.example.study.querydsl.entity.QMember;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static com.example.study.querydsl.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void basicTest() {

        Member member = memberRepository.findById(2022L).get();
        assertThat(member.getId()).isEqualTo(2022);

        List<Member> result1 = memberRepository.findAll();

        List<Member> result2 = memberRepository.findByUserName("member3");
        assertThat(result2.get(0).getId()).isEqualTo(2022);
    }

    @Test
    public void searchTest() {
        MemberSearchCondition condition = new MemberSearchCondition();

        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        List<MemberTeamDTO> result = memberRepository.search(condition);

        assertThat(result.get(0).getUserName()).isEqualTo("member4");

    }

    @Test
    public void searchPageComplex() {
        PageRequest of = PageRequest.of(1, 3);

        Page<MemberTeamDTO> result = memberRepository.searchPageComplex(new MemberSearchCondition(), of);

        System.out.println(result);

        assertThat(result.getTotalPages()).isEqualTo(2);

    }

    @Test
    public void querydslPredicateExecutor() {
        Iterable<Member> memberResult = memberRepository.findAll(member.age.between(20, 40).and(member.userName.eq("member1")));

        for (Member findMember : memberResult) {
            System.out.println("member : " + findMember);
        }
    }

}
