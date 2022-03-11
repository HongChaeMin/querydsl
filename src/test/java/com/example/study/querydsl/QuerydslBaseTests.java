package com.example.study.querydsl;

import com.example.study.querydsl.entity.Member;
import com.example.study.querydsl.entity.QMember;
import com.example.study.querydsl.entity.QTeam;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;

import java.util.List;

import static com.example.study.querydsl.entity.QMember.member;
import static com.example.study.querydsl.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class QuerydslBaseTests {

    @Autowired
    private EntityManager em;

    @Autowired
    private JPAQueryFactory queryFactory;

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

    @Test
    public void aggregation() {
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구하여라.
     * */
    @Test
    public void group() {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team)
                .groupBy(team.name)
                .fetch();
    }

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    public void join() throws Exception {
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("userName")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인(연관관계가 없는 필드로 조인)
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() throws Exception {
        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.userName.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("userName")
                .containsExactly("teamA", "teamB");
    }

    /**
     * 예 ) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL : select m, t from Member m left join m.team t on t.name = 'teamA'
     * SQL : select m.*, t.* from Member m left join Team t on m.team_id = t.id and t.name = 'teamA'
     **/
    @Test
    public void join_on_filtering() {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple t : result) {
            System.out.println(t);
        }
    }

    /**
     * 2. 연관관계 없는 엔티티 외부 조인
     * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
     */
    @Test
    public void join_on_no_relation() throws Exception {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.userName.eq(team.name))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println(tuple);
        }
    }

    @Test
    public void fetchJoin() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.userName.eq("member1"))
                .fetch();

        System.out.println(result);
    }

    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    /**
     * 나이가 평균 나이 이상인 회원
     */
    @Test
    public void subQueryGoe() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(30,40);
    }

    /**
     * 서브쿼리 여러 건 처리, in 사용
     * 좋은 쿼리가 아닌데 예제로 만든거임
     */
    @Test
    public void subQueryIn() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);
    }

    /**
     * select sub query
     * **/
    @Test
    public void selectSubQuery() {
        QMember memberSub = new QMember("memberSub");
        List<Tuple> fetch = queryFactory
                .select(member.userName,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ).from(member)
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("username = " + tuple.get(member.userName));
            System.out.println("age = " +
                    tuple.get(JPAExpressions.select(memberSub.age.avg())
                            .from(memberSub)));
        }
    }

    @Test
    public void basicCase() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s : " + s);
        }
    }

    @Test
    public void complexCase() {
        List<String> result = queryFactory
                .select(new CaseBuilder() // caseBuilder 추가
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s : " + s);
        }
    }

    /**
     * 예를 들어서 다음과 같은 임의의 순서로 회원을 출력하고 싶다면?
     * 1. 0 ~ 30살이 아닌 회원을 가장 먼저 출력
     * 2. 0 ~ 20살 회원 출력
     * 3. 21 ~ 30살 회원 출력
     * **/
    @Test
    public void addCase() {
        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(member.age.between(0, 20)).then(2)
                .when(member.age.between(21, 30)).then(1)
                .otherwise(3);

        List<Tuple> result = queryFactory
                .select(member.userName, member.age, rankPath)
                .from(member)
                .orderBy(rankPath.desc())
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.userName);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);
            System.out.println("username = " + username + " age = " + age + " rank = "
                    + rank);
        }
    }

}
