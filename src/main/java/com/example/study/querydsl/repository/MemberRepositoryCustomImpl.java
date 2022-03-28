package com.example.study.querydsl.repository;

import com.example.study.querydsl.dto.MemberSearchCondition;
import com.example.study.querydsl.dto.MemberTeamDTO;
import com.example.study.querydsl.dto.QMemberTeamDTO;
import com.example.study.querydsl.entity.Member;
import com.example.study.querydsl.repository.support.Querydsl4RepositorySupport;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;

import javax.persistence.EntityManager;
import java.util.List;

import static com.example.study.querydsl.entity.QMember.member;
import static com.example.study.querydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

public class MemberRepositoryCustomImpl extends QuerydslRepositorySupport implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryCustomImpl(EntityManager em) {
        super(Member.class);
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDTO> search(MemberSearchCondition condition) {
        from(member)
                .leftJoin(member.team, team)
                .where(
                        userNameEq(condition.getUserName()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .select(new QMemberTeamDTO(
                        member.id,
                        member.userName,
                        member.age,
                        team.id,
                        team.name
                ))
                .fetch();

        return queryFactory
                .select(new QMemberTeamDTO(
                        member.id,
                        member.userName,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        userNameEq(condition.getUserName()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();
    }

    @Override
    public Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDTO> results = queryFactory
                .select(new QMemberTeamDTO(
                        member.id,
                        member.userName,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        userNameEq(condition.getUserName()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch(); // fetchResults 안됨
        long total = results.size();

        return new PageImpl<>(results, pageable, total);
    }

    public Page<MemberTeamDTO> searchPageSimple2(MemberSearchCondition condition, Pageable pageable) {

        JPQLQuery<MemberTeamDTO> dtoList = from(member)
                .leftJoin(member.team, team)
                .where(
                    userNameEq(condition.getUserName()),
                    teamNameEq(condition.getTeamName()),
                    ageGoe(condition.getAgeGoe()),
                    ageLoe(condition.getAgeLoe())
                )
                .select(new QMemberTeamDTO(
                        member.id,
                        member.userName,
                        member.age,
                        team.id,
                        team.name
                ));

        JPQLQuery<MemberTeamDTO> memberTeamDTOJPQLQuery = getQuerydsl().applyPagination(pageable, dtoList);

        memberTeamDTOJPQLQuery.fetch();
        memberTeamDTOJPQLQuery.fetchJoin();

        return null;
    }

    @Override
    public Page searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDTO> results = queryFactory
                .select(new QMemberTeamDTO(
                        member.id,
                        member.userName,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        userNameEq(condition.getUserName()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Member> countQuery = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                    userNameEq(condition.getUserName()),
                    teamNameEq(condition.getTeamName()),
                    ageGoe(condition.getAgeGoe()),
                    ageLoe(condition.getAgeLoe())
                );

        // - 스프링 데이터 라이브러리가 제공
        // - count 쿼리가 생략 가능한 경우 생략해서 처리
        //    + 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
        //    + 마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈 구함)
        return PageableExecutionUtils.getPage(results, pageable, countQuery.stream()::count);
        // return new PageImpl<>(results, pageable, total);
    }

    private BooleanExpression userNameEq(String userName) {
        return hasText(userName) ? member.userName.eq(userName) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

}
