package com.mz.blog.system.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper extends BaseMapper<SysUser> {

    @Select("""
            SELECT DISTINCT r.code
            FROM sys_role r
            JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId} AND r.status = 1 AND r.deleted = 0
            """)
    List<String> selectRoleCodes(Long userId);

    @Select("""
            SELECT DISTINCT m.permission_code
            FROM sys_menu m
            JOIN sys_role_menu rm ON rm.menu_id = m.id
            JOIN sys_role r ON r.id = rm.role_id
            JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
              AND r.status = 1 AND r.deleted = 0
              AND m.status = 1 AND m.deleted = 0
              AND m.permission_code IS NOT NULL AND m.permission_code <> ''
            """)
    List<String> selectPermissionCodes(Long userId);
}
