package com.usoft.framework.bi.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.bi.api.AppCreateRequest;
import com.usoft.framework.bi.api.AppResponse;
import com.usoft.framework.bi.api.AppUpdateRequest;
import com.usoft.framework.bi.api.enums.AppMenuType;
import com.usoft.framework.bi.api.enums.AuthorizationScope;
import com.usoft.framework.bi.api.enums.BizType;
import com.usoft.framework.bi.entity.AppEntity;
import com.usoft.framework.bi.entity.AuthorizationEntity;
import com.usoft.framework.bi.mapper.AppMapper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.system.api.enums.TenantAuthKey;
import com.usoft.framework.system.service.TenantService;
import com.usoft.framework.common.bean.BeanMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppService {
    private final TenantService tenantService;
    private final AppMapper appMapper;

    /**
     * 创建应用菜单
     * 
     * @param req 创建请求
     * @return 创建后的实体
     */
    @Transactional
    public AppResponse create(AppCreateRequest req) {
        checkAppKeyUnique(req.getAppKey(), null);

        AppEntity entity = new AppEntity();
        BeanMapper.mapper(req, entity);
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantId(TenantContext.getTenantId());
        entity.setIsDeleted(false);
        entity.setCreatedAt(Instant.now());
        entity.setCreatedBy(UserHolder.username());
        entity.setIsLeaf(true);

        if (req.getParentId() != null && !req.getParentId().isBlank()) {
            AppEntity parent = appMapper.selectOneById(req.getParentId());
            if (parent != null) {
                entity.setParentId(parent.getId());
                entity.setLevel((parent.getLevel() == null ? 0 : parent.getLevel()) + 1);
                String parentAncestors = parent.getAncestorIds() == null ? "" : parent.getAncestorIds();
                entity.setAncestorIds(parentAncestors + parent.getId() + ",");

                String parentSorts = parent.getAncestorSorts() == null ? "" : parent.getAncestorSorts();
                entity.setAncestorSorts(parentSorts + (parent.getSort() == null ? 0 : parent.getSort()) + ",");

                if (Boolean.TRUE.equals(parent.getIsLeaf())) {
                    parent.setIsLeaf(false);
                    appMapper.update(parent);
                }
            } else {
                entity.setParentId(null);
                entity.setLevel(0);
                entity.setAncestorIds("");
                entity.setAncestorSorts("");
            }
        } else {
            entity.setParentId(null);
            entity.setLevel(0);
            entity.setAncestorIds("");
            entity.setAncestorSorts("");
        }

        if (entity.getParentId() == null || entity.getParentId().isBlank()) {
            if (entity.getMenuType() == null || entity.getMenuType() != AppMenuType.GROUP) {
                throw new RuntimeException("第一层应用菜单的类型只能为 group");
            }
        }

        appMapper.insert(entity);
        return toResponse(entity);
    }

    /**
     * 更新应用菜单
     * 
     * @param id  实体ID
     * @param req 更新请求
     * @return 更新后的实体
     */
    @Transactional
    public AppResponse update(String id, AppUpdateRequest req) {
        AppEntity entity = appMapper.selectOneById(id);
        if (entity == null) {
            return null;
        }

        tenantService.checkTenantUserAuthKey(entity.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);

        String oldParentId = entity.getParentId();
        Integer oldLevel = entity.getLevel();
        String oldAncestorIds = entity.getAncestorIds();

        if (req.getTitle() != null) {
            entity.setTitle(req.getTitle());
        }
        if (req.getIcon() != null) {
            entity.setIcon(req.getIcon());
        }
        if (req.getPageId() != null) {
            entity.setPageId(req.getPageId());
        }
        if (req.getAppKey() != null) {
            checkAppKeyUnique(req.getAppKey(), id);
            entity.setAppKey(req.getAppKey());
        }
        if (req.getRedirectUrl() != null) {
            entity.setRedirectUrl(req.getRedirectUrl());
        }
        if (req.getMenuType() != null) {
            entity.setMenuType(req.getMenuType());
        }
        if (req.getSort() != null) {
            entity.setSort(req.getSort());
        }
        if (req.getHasWatermark() != null) {
            entity.setHasWatermark(req.getHasWatermark());
        }

        if (req.getParentId() != null) {
            String newParentId = req.getParentId().isEmpty() ? null : req.getParentId();

            if (id.equals(newParentId)) {
                throw new RuntimeException("不能移动到自身");
            }

            if (!java.util.Objects.equals(oldParentId, newParentId)) {
                String newAncestorIds = "";
                String newAncestorSorts = "";
                Integer newLevel = 0;

                if (newParentId != null) {
                    AppEntity newParent = appMapper.selectOneById(newParentId);
                    if (newParent == null) {
                        throw new RuntimeException("目标应用菜单不存在");
                    }
                    if (newParent.getAncestorIds() != null && newParent.getAncestorIds().contains(id + ",")) {
                        throw new RuntimeException("不能移动到子节点下");
                    }

                    newAncestorIds = (newParent.getAncestorIds() == null ? "" : newParent.getAncestorIds())
                            + newParent.getId() + ",";
                    newAncestorSorts = (newParent.getAncestorSorts() == null ? "" : newParent.getAncestorSorts())
                            + (newParent.getSort() == null ? 0 : newParent.getSort()) + ",";
                    newLevel = (newParent.getLevel() == null ? 0 : newParent.getLevel()) + 1;

                    if (Boolean.TRUE.equals(newParent.getIsLeaf())) {
                        newParent.setIsLeaf(false);
                        appMapper.update(newParent);
                    }
                }

                entity.setParentId(newParentId);
                entity.setAncestorIds(newAncestorIds);
                entity.setAncestorSorts(newAncestorSorts);
                entity.setLevel(newLevel);

                QueryWrapper descendantsQw = QueryWrapper.create()
                        .where("ancestor_ids LIKE ?", "%" + id + ",%");
                List<AppEntity> descendants = appMapper.selectListByQuery(descendantsQw);

                String oldPrefix = (oldAncestorIds == null ? "" : oldAncestorIds) + id + ",";
                String newPrefix = newAncestorIds + id + ",";
                int levelDelta = newLevel - (oldLevel == null ? 0 : oldLevel);

                for (AppEntity desc : descendants) {
                    if (desc.getAncestorIds() != null) {
                        desc.setAncestorIds(desc.getAncestorIds().replace(oldPrefix, newPrefix));
                    }
                    if (desc.getLevel() != null) {
                        desc.setLevel(desc.getLevel() + levelDelta);
                    }
                    appMapper.update(desc);
                }

                if (oldParentId != null) {
                    long siblingsCount = appMapper.selectCountByQuery(QueryWrapper.create()
                            .where("parent_id = ?", oldParentId)
                            .and("is_deleted = 0")
                            .and("id != ?", id));

                    if (siblingsCount == 0) {
                        AppEntity oldParent = new AppEntity();
                        oldParent.setId(oldParentId);
                        oldParent.setIsLeaf(true);
                        appMapper.update(oldParent);
                    }
                }
            }
        }

        if (entity.getParentId() == null || entity.getParentId().isBlank()) {
            if (entity.getMenuType() == null || entity.getMenuType() != AppMenuType.GROUP) {
                throw new RuntimeException("第一层应用菜单的类型只能为 group");
            }
        }

        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(UserHolder.username());
        appMapper.update(entity);
        return toResponse(entity);
    }

    /**
     * 删除应用菜单
     * 
     * @param id 实体ID
     * @return 删除是否成功
     */
    @Transactional
    public boolean delete(String id) {
        AppEntity entity = appMapper.selectOneById(id);
        if (entity == null) {
            return false;
        }
        tenantService.checkTenantUserAuthKey(entity.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);

        QueryWrapper childQw = QueryWrapper.create()
                .where("parent_id = ?", id)
                .and("is_deleted = 0");
        long childCount = appMapper.selectCountByQuery(childQw);
        if (childCount > 0) {
            throw new RuntimeException("当前菜单存在下级节点，无法删除");
        }

        QueryWrapper idsQw = QueryWrapper.create()
                .select("id")
                .where("id = ?", id)
                .or("ancestor_ids LIKE ?", "%" + id + ",%");
        List<String> appIdsToDelete = appMapper.selectObjectListByQueryAs(idsQw, String.class);

        if (appIdsToDelete == null || appIdsToDelete.isEmpty()) {
            return false;
        }

        String username = UserHolder.username();
        Instant now = Instant.now();

        AppEntity update = new AppEntity();
        update.setIsDeleted(true);
        update.setUpdatedAt(now);
        update.setUpdatedBy(username);

        QueryWrapper updateQw = QueryWrapper.create()
                .in("id", appIdsToDelete);
        appMapper.updateByQuery(update, updateQw);

        if (entity.getParentId() != null) {
            long siblingsCount = appMapper.selectCountByQuery(QueryWrapper.create()
                    .where("parent_id = ?", entity.getParentId())
                    .and("is_deleted = 0"));

            if (siblingsCount == 0) {
                AppEntity parentUpdate = new AppEntity();
                parentUpdate.setId(entity.getParentId());
                parentUpdate.setIsLeaf(true);
                parentUpdate.setUpdatedAt(now);
                parentUpdate.setUpdatedBy(username);
                appMapper.update(parentUpdate);
            }
        }

        return true;
    }

    /**
     * 查询应用菜单列表
     * 
     * @return 实体列表
     */
    public List<AppResponse> list() {
        String tenantId = TenantContext.getTenantId();
        String userId = UserHolder.userId();
        boolean isAdmin = tenantService.isTenantUserAuthKey(tenantId, userId, TenantAuthKey.ADMIN);
        QueryWrapper authQw = QueryWrapper.create()
                .select("biz_id")
                .from(AuthorizationEntity.class).as("a")
                .where("is_deleted = 0")
                .eq("biz_type", BizType.APP)
                .eq("tenant_id", tenantId, StringUtils.isNotBlank(tenantId))
                .in("authorizer_scope", List.of(AuthorizationScope.PUBLIC, AuthorizationScope.TENANT))
                .unionAll(
                        QueryWrapper.create()
                                .select("biz_id")
                                .from(AuthorizationEntity.class).as("b")
                                .where("is_deleted = 0")
                                .eq("biz_type", BizType.APP)
                                .eq("tenant_id", tenantId, StringUtils.isNotBlank(tenantId))
                                .eq("authorizer_scope", AuthorizationScope.USER)
                                .eq("authorizer_id", Objects.requireNonNullElse(userId, "-1")));
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", TenantContext.getTenantId())
                .and("is_deleted = 0")
                .in("id", authQw, !isAdmin)
                .orderBy("sort", true);
        List<AppEntity> entities = appMapper.selectListByQuery(qw);
        return buildTree(entities);
    }

    /**
     * 根据ID查询应用菜单
     * 
     * @param id 实体ID
     * @return 实体
     */
    public AppResponse get(String id) {
        AppEntity entity = appMapper.selectOneById(id);
        if (entity == null) {
            return null;
        }
        return toResponse(entity);
    }

    private void checkAppKeyUnique(String appKey, String excludeId) {
        // if (appKey == null || appKey.isBlank()) {
        // return;
        // }
        // QueryWrapper qw = QueryWrapper.create()
        // .where("tenant_id = ?", TenantContext.getTenantId())
        // .and("app_key = ?", appKey)
        // .and("is_deleted = 0");

        // if (excludeId != null) {
        // qw.and("id != ?", excludeId);
        // }

        // if (appMapper.selectCountByQuery(qw) > 0) {
        // throw new RuntimeException("App Key [" + appKey + "] 已存在");
        // }
    }

    private AppResponse toResponse(AppEntity entity) {
        AppResponse r = new AppResponse();
        r.setId(entity.getId());
        r.setParentId(entity.getParentId());
        r.setTenantId(entity.getTenantId());

        // Map path (use appKey as path)
        r.setPath(entity.getAppKey());

        // Map handle
        AppResponse.Handle handle = new AppResponse.Handle();
        BeanMapper.mapper(entity, handle);
        handle.setType(entity.getMenuType());
        r.setHandle(handle);

        return r;
    }

    private List<AppResponse> buildTree(List<AppEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }

        List<AppResponse> allNodes = entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        // 使用Map加速查找
        Map<String, AppResponse> nodeMap = allNodes.stream()
                .collect(Collectors.toMap(AppResponse::getId, Function.identity()));

        List<AppResponse> rootNodes = new ArrayList<>();

        // 再次遍历 entities 以获取 parentId 信息，因为 BiAppResponse 中没有 parentId
        for (AppEntity entity : entities) {
            AppResponse node = nodeMap.get(entity.getId());
            String parentId = entity.getParentId();

            if (parentId == null || parentId.isBlank() || !nodeMap.containsKey(parentId)) {
                rootNodes.add(node);
            } else {
                AppResponse parent = nodeMap.get(parentId);
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(node);
            }
        }

        return rootNodes;
    }
}
