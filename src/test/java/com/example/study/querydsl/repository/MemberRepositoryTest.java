package com.example.study.querydsl.repository;

import com.example.study.querydsl.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

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

}
