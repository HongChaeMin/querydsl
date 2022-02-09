package com.example.study.querydsl;

import com.example.study.querydsl.entity.Member;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;

import java.util.List;

import static com.example.study.querydsl.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RequiredArgsConstructor
public class QuerydslBaseTests {

    @Autowired
    private EntityManager em;

    private final JPAQueryFactory queryFactory;

    @Test
    public void startJPQL() {
        String qlString = "select m from Member m where m.userName = :userName";

        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("userName", "member1")
                .getSingleResult();

        assertThat(findMember.getUserName()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.userName.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getUserName()).isEqualTo("member1");
    }

    @Test
    public void search() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.userName.eq("member1")
                    .and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUserName().equals("member1"));
    }

    @Test
    public void searchAndParam() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.userName.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        assertThat(findMember.getUserName().equals("member1"));
    }

    @Test
    public void resultFetch() {
        // 리스트 조회, 데이터 없으면 빈 리스트 반환
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        // 단 건 조회
        // - 결과가 없으면 null
        // - 결과가 둘 이상이면 com.querydsl.core.NonUniqueResultException
        Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();

        // limit(1).fetchOne()
        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();

        // 페이징 정보 포함, total count 쿼리 추가 실행 (미지원)
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        results.getTotal();
        List<Member> content = results.getResults();

        // count 쿼리로 변경해서 count 수 조회 (미지원)
        long total = queryFactory
                .selectFrom(member)
                .fetchCount();

    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.age.desc(), member.userName.asc().nullsLast())
                .fetch();

        // desc(), asc() : 일반 정렬
        // nullsLast(), nullsFirst() : null 데이터 순서 부여

    }

    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.userName.desc())
                .offset(1) // 0부터 시작 (zero index)
                .limit(2) // 최대 2건 조회
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {
        QueryResults<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.userName.desc())
                .offset(1)
                .limit(2)
                .fetchResults(); // (미지원)
    }

}
