package com.devlatte.devroom.controller;

import com.devlatte.devroom.dto.MemberJoinRequestDto;
import com.devlatte.devroom.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    // 학생 로그인

    @PostMapping("/register")
    public ResponseEntity<String> register(@ModelAttribute MemberJoinRequestDto memberJoinRequestDto){
        //log.info("Registering member : {}", member);

        memberService.register(memberJoinRequestDto);
        return ResponseEntity.ok("register successful");
    }

}
