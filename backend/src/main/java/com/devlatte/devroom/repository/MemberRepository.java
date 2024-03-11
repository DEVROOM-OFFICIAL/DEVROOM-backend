package com.devlatte.devroom.repository;

import com.devlatte.devroom.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findOneByMemberId(String memberId);
    Optional<Member> findOneByMemberInfo(String memberInfo);
}
