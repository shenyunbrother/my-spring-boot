package com.hg.hyy.interfaces.impl;

import com.hg.hyy.interfaces.MyAccess;
import com.hg.hyy.service.SysUserDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

@Slf4j
@Component
public class MyAccessImpl implements MyAccess {

    @Override
    public boolean hasPermit(HttpServletRequest httpServletRequest, Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof SysUserDetailService sysUserDetailService) {
            Collection<? extends GrantedAuthority> authorities = sysUserDetailService.getAuthorities();
            log.error(httpServletRequest.getRequestURI());
            return authorities.contains(new SimpleGrantedAuthority(httpServletRequest.getRequestURI()));
        }
        return false;
    }
}
