package com.devlatte.devroom.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
//import org.springframework.security.crypto.password.PasswordEncoder;

@Data
@Entity
@Getter @Setter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long ID;

    @Column(name = "member_id", unique = true)
    private String memberId;

    @Column(name = "member_pw")
    private String memberPw;

    @Column(name = "member_info")
    private String memberInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_role")
    private MemberRole memberRole;

    /*
    public void encodePassword(PasswordEncoder passwordEncoder){
        this.memberPw = passwordEncoder.encode(memberPw);
    }

    */

    // MemberJoinDto를 위한 Builder 정의
    @Builder
    public Member(String memberId, String memberPw, String memberInfo, MemberRole memberRole){
        this.memberId = memberId;
        this.memberPw = memberPw;
        this.memberInfo = memberInfo;
        this.memberRole = memberRole;
    }
}
