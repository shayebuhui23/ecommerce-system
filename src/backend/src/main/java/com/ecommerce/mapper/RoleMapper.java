package com.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.entity.Role;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 角色数据访问。
 */
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 按角色编码查询角色，用于注册时确定默认角色。
     */
    @Select("SELECT id, code, name, description, created_at, updated_at FROM roles WHERE code = #{code}")
    Role selectByCode(@Param("code") String code);
}
