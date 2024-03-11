package com.devlatte.devroom.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtil {

    /*
    1. getCurrentUsername()
    SecurityContextHolder 내 Authentication 객체의 principal에 저장되어 있는 현재 로그인 중인 유저의 ID 정보를 가지고 오는 util function
    */
    public static String getCurrentUsername(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUsername();
    }
}
