package com.mz.blog.system.menu;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MenuMapper extends BaseMapper<SysMenu> {

    @Select("""
            SELECT DISTINCT m.*
            FROM sys_menu m
            JOIN sys_role_menu rm ON rm.menu_id = m.id
            JOIN sys_role r ON r.id = rm.role_id
            JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
              AND r.status = 1 AND r.deleted = 0
              AND m.status = 1 AND m.deleted = 0
            ORDER BY m.sort, m.id
            """)
    List<SysMenu> selectByUserId(Long userId);
}
