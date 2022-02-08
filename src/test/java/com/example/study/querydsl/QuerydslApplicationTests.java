package com.example.study.querydsl;

import com.example.study.querydsl.entity.Hello;
import com.example.study.querydsl.entity.QHello;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

	@Autowired
	private EntityManager entityManager;

	@Test
	void contextLoads() {

		Hello hello = new Hello();
		entityManager.persist(hello);

		JPAQueryFactory query = new JPAQueryFactory(entityManager);
		QHello qHello = new QHello("h");

		Hello result = query.selectFrom(qHello).fetchOne();

		assertThat(result).isEqualTo(hello);
		assertThat(result.getId()).isEqualTo(hello.getId());
	}

}
