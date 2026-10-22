package com.mz.blog.security;

import com.mz.blog.common.api.ApiResponse;
import com.mz.blog.common.exception.BusinessException;
import com.mz.blog.system.user.SysUser;
import com.mz.blog.system.user.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final LoginAttemptService loginAttemptService;
    private final UserMapper userMapper;

    public record LoginRequest(@NotBlank(message = "请输入用户名") String username,
                               @NotBlank(message = "请输入密码") String password) {
    }

    record MeResponse(String id, String username, String nickname, String avatarUrl,
                      List<String> roles, List<String> permissions) {
    }

    @GetMapping("/csrf")
    public ApiResponse<Map<String, String>> csrf(CsrfToken token) {
        return ApiResponse.ok(Map.of("token", token.getToken(), "headerName", token.getHeaderName()));
    }

    @PostMapping("/login")
    public ApiResponse<MeResponse> login(@Valid @RequestBody LoginRequest body,
                                         HttpServletRequest request) {
        String key = body.username() + ":" + request.getRemoteAddr();
        if (loginAttemptService.isBlocked(key)) {
            throw new BusinessException(42900, HttpStatus.TOO_MANY_REQUESTS, "登录失败次数过多，请稍后再试");
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(body.username(), body.password()));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, null);
            AdminUserDetails user = (AdminUserDetails) authentication.getPrincipal();
            SysUser update = new SysUser();
            update.setId(user.getId());
            update.setLastLoginAt(LocalDateTime.now());
            update.setLastLoginIp(request.getRemoteAddr());
            userMapper.updateById(update);
            loginAttemptService.success(key);
            return ApiResponse.ok(toMe(user));
        } catch (BadCredentialsException ex) {
            loginAttemptService.failed(key);
            throw new BusinessException(40101, HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me() {
        AdminUserDetails user = (AdminUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return ApiResponse.ok(toMe(user));
    }

    private MeResponse toMe(AdminUserDetails user) {
        return new MeResponse(String.valueOf(user.getId()), user.getUsername(), user.getNickname(),
                user.getAvatarUrl(), user.getRoles().stream().sorted().toList(),
                user.getPermissions().stream().sorted().toList());
    }
}
