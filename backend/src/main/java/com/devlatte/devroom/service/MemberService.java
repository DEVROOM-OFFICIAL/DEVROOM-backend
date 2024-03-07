package com.devlatte.devroom.service;

import com.devlatte.devroom.dto.MemberJoinRequestDto;
import com.devlatte.devroom.entity.MemberRole;
import com.devlatte.devroom.entity.Member;
import com.devlatte.devroom.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findOneByMemberId(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username has not been found."));

        return User.builder()
                .username(member.getMemberId())
                .password(member.getMemberPw())
                .authorities(String.valueOf(member.getMemberRole()))
                .build();
    }


    public Member register(MemberJoinRequestDto memberJoinRequestDto){
        Member member = memberJoinRequestDto.makeEntity();
        log.info("Registering member : {}", member);
        chkDuplicateMember(member);

        member.setMemberPw(passwordEncoder.encode(member.getMemberPw()));
        return memberRepository.save(member);
    }

    // 하나의 회원은 오직 하나의 계정만 만들 수 있도록 제한한다.
    private void chkDuplicateMember(Member member){
        MemberRole memberRole = member.getMemberRole();
        Optional<Member> findMember;


        if(memberRole == MemberRole.Student){
            String memberInfo = member.getMemberInfo();

            findMember = memberRepository.findOneByMemberInfo(memberInfo);
        } else if(memberRole == MemberRole.Professor){
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

// 로그인, 즉 인증(authentication)과 인가(authorization)의 처리는 Spring Security에서 담당한다.
