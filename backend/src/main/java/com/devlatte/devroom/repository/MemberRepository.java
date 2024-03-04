package com.devlatte.devroom.repository;

import com.devlatte.devroom.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findOneByMemberId(String memberId);
    Optional<Member> findOneByMemberInfo(String memberInfo);
}
