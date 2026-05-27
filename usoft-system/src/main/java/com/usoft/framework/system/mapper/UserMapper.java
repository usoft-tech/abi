package com.usoft.framework.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.system.api.TenantUserResponse;
import com.usoft.framework.system.entity.UserEntity;
import com.usoft.framework.system.entity.UserTenantEntity;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * 通过用户名查询用户
     */
    @Select("SELECT u.* FROM sys_user u JOIN sys_user_tenant ut ON ut.user_id = u.id WHERE u.username = #{username} AND (u.is_deleted = 0 OR u.is_deleted IS NULL)")
    UserEntity findByUsername(String username);

    /**
     * 查询当前租户下的用户
     */
    default TenantUserResponse findByIdInTenant(String id, String tenantId) {
        QueryWrapper qw = QueryWrapper.create()
                .select("u.*")
                .from(UserEntity.class).as("u")
                .join(UserTenantEntity.class).as("ut").on("ut.user_id = u.id")
                .where("u.id = ?", id)
                .and("ut.tenant_id = ?", tenantId)
                .and("(u.is_deleted = 0 OR u.is_deleted IS NULL)");
        return selectOneByQueryAs(qw, TenantUserResponse.class);
    }

    /**
     * 查询当前租户下的所有用户
     */
    default List<TenantUserResponse> listByTenant(String tenantId) {
        QueryWrapper qw = QueryWrapper.create()
                .select("u.*, ut.auth_key")
                .from(UserEntity.class).as("u")
                .join(UserTenantEntity.class).as("ut").on("ut.user_id = u.id")
                .where("ut.tenant_id = ?", tenantId)
                .and("(u.is_deleted = 0 OR u.is_deleted IS NULL)");
        return selectListByQueryAs(qw, TenantUserResponse.class);
    }
}
