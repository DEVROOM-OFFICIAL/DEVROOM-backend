package com.devlatte.devroom.service;

import com.devlatte.devroom.dto.MemberJoinRequestDto;
import com.devlatte.devroom.entity.MemberRole;
import com.devlatte.devroom.entity.Member;
import com.devlatte.devroom.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    public Member register(MemberJoinRequestDto memberJoinRequestDto){
        Member member = memberJoinRequestDto.makeEntity();
        log.info("Registering member : {}", member);
        chkDuplicateMember(member);
        return memberRepository.save(member);
    }

    // 하나의 회원은 오직 하나의 계정만 만들 수 있도록 제한한다.
    private void chkDuplicateMember(Member member){
        MemberRole memberRole = member.getMemberRole();
        Optional<Member> findMember;


        if(memberRole == MemberRole.STUDENT){
            String memberInfo = member.getMemberInfo();

            findMember = memberRepository.findOneByMemberInfo(memberInfo);
        } else if(memberRole == MemberRole.PROFESSOR){
            String memberID = member.getMemberId();

            findMember = memberRepository.findOneByMemberId(memberID);
        } else{
            findMember = Optional.empty();
        }

        if(findMember.isPresent()){
            throw new IllegalStateException("이 ID는 이미 가입되어 있습니다.");
        }
    }
}
