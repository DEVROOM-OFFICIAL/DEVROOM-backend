package com.devlatte.devroom.dto;

import com.devlatte.devroom.entity.Member;
import com.devlatte.devroom.entity.MemberRole;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
public class MemberJoinRequestDto {
    private String memberId;
    private String memberPw;
    private String memberInfo;
    private MemberRole memberRole;

    @Builder
    public MemberJoinRequestDto(String memberId, String memberPw, String memberInfo, MemberRole memberRole){
        this.memberId = memberId;
        this.memberPw = memberPw;
        this.memberInfo = memberInfo;
        this.memberRole = memberRole;
    }

    public Member makeEntity(){
        Member.MemberBuilder builder =  Member.builder()
                .memberId(memberId)
                .memberPw(memberPw)
                .memberInfo(memberInfo)
                .memberRole(memberRole);

        /*
        if (memberInfo != null) {
            builder.memberInfo(memberInfo);
        }

        if (memberRole != null) {
            builder.memberRole(memberRole);
        }
        */

        return builder.build();
    }
}
