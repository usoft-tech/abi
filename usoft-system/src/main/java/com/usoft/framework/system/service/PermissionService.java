package com.usoft.framework.system.service;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.common.annotation.AuthorizeDescription;
import com.usoft.framework.common.annotation.AuthorizeDescriptionGroup;
import com.usoft.framework.common.bean.BeanMapper;
import com.usoft.framework.common.utils.IdUtils;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.system.api.PermissionCreateRequest;
import com.usoft.framework.system.api.PermissionQueryRequest;
import com.usoft.framework.system.api.PermissionResponse;
import com.usoft.framework.system.api.PermissionUpdateRequest;
import com.usoft.framework.system.api.enums.PermissionType;
import com.usoft.framework.system.entity.SysPermissionEntity;
import com.usoft.framework.system.entity.table.SysPermissionEntityTableDef;
import com.usoft.framework.system.mapper.SysPermissionMapper;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 权限服务
 */
@Service
public class PermissionService implements ApplicationContextAware, SmartLifecycle {
    private static final Logger log = LoggerFactory.getLogger(PermissionService.class);
    private final SysPermissionMapper permissionMapper;
    private ApplicationContext applicationContext;
    private boolean isRunning = false;

    // 缓存内置权限Code
    private static final Set<String> BUILT_IN_CODES = Collections.synchronizedSet(new HashSet<>());

    public PermissionService(SysPermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void start() {
        scanAndSyncPermissions();
        isRunning = true;
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 扫描并同步权限
     */
    private void scanAndSyncPermissions() {
        log.info("Start scanning @PreAuthorize annotations...");

        // 扫描 Controller 和 RestController
        Map<String, Object> beans = new HashMap<>();
        beans.putAll(applicationContext.getBeansWithAnnotation(Controller.class));
        beans.putAll(applicationContext.getBeansWithAnnotation(RestController.class));

        List<AuthorizeInfo> authorizeInfos = new ArrayList<>();

        // 支持 @ss.hasAuthority('...') 和 hasAuthority('...') 以及双引号
        Pattern pattern = Pattern.compile("(?:@ss\\.)?hasAuthority\\(['\"]([^'\"]+)['\"]\\)");

        for (Object bean : beans.values()) {
            Class<?> clazz = AopUtils.isCglibProxy(bean) ? AopUtils.getTargetClass(bean) : bean.getClass();
            AuthorizeDescriptionGroup group = clazz.getAnnotation(AuthorizeDescriptionGroup.class);
            AuthorizeDescription classDescription = clazz.getAnnotation(AuthorizeDescription.class);
            if (group != null && group.value() != null) {
                Arrays.stream(group.value()).forEach(item -> {
                    authorizeInfos.add(new AuthorizeInfo()
                            .setGroup(item.group())
                            .setDescription(item.value())
                            .setAuthorize(item.authorize()));
                });
            }
            if (classDescription != null) {
                authorizeInfos.add(new AuthorizeInfo()
                        .setGroup(classDescription.group())
                        .setDescription(classDescription.value())
                        .setAuthorize(classDescription.authorize()));
            }
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
                AuthorizeDescriptionGroup methodGroup = method.getAnnotation(AuthorizeDescriptionGroup.class);
                AuthorizeDescription description = method.getAnnotation(AuthorizeDescription.class);
                if (annotation == null) {
                    continue;
                }
                String value = annotation.value();
                Matcher matcher = pattern.matcher(value);
                String code = matcher.find() ? matcher.group(1) : null;
                if (methodGroup != null && methodGroup.value() != null) {
                    Arrays.stream(methodGroup.value()).forEach(item -> {
                        authorizeInfos.add(new AuthorizeInfo()
                                .setGroup(item.group())
                                .setDescription(item.value())
                                .setAuthorize(item.authorize())
                                .setPreAuthorize(code));
                    });
                }
                if (description != null) {
                    authorizeInfos.add(new AuthorizeInfo()
                            .setGroup(description.group())
                            .setDescription(description.value())
                            .setAuthorize(description.authorize())
                            .setPreAuthorize(code));
                }
            }
        }

        authorizeInfos.removeIf(i -> StringUtils.isBlank(i.authorizeKey()));

        if (authorizeInfos.isEmpty()) {
            log.info("No permissions found.");
            return;
        }

        // 更新缓存
        BUILT_IN_CODES.addAll(authorizeInfos.stream()
                .map(AuthorizeInfo::authorizeKey)
                .collect(Collectors.toSet()));

        // 同步到数据库
        List<SysPermissionEntity> existing = permissionMapper.selectListByQuery(QueryWrapper.create());

        Map<String, SysPermissionEntity> codeToEntity = new HashMap<>(existing.stream()
                .collect(Collectors.toMap(SysPermissionEntity::getCode, Function.identity(), (o1, o2) -> o1)));

        // 按层级深度排序，确保父节点先处理
        List<AuthorizeInfo> sortedInfos = authorizeInfos.stream()
                .sorted(Comparator.comparingInt(i -> i.authorizeKey().split(":").length))
                .peek(i -> i.setId(IdUtils.randomId()))
                .toList();

        Set<String> ids1 = sortedInfos.stream().map(AuthorizeInfo::getId).collect(Collectors.toSet());
        Set<String> ids2 = existing.stream().map(SysPermissionEntity::getId).collect(Collectors.toSet());
        for (AuthorizeInfo info : sortedInfos) {
            syncPermission(info, codeToEntity);
        }
        existing.stream()
                // not in BUILT_IN_CODES and parentId not blank
                .filter(item -> !ids1.contains(item.getId()) && StringUtils.isNotBlank(item.getParentId()))
                // parentId not exists
                .filter(item -> !ids1.contains(item.getParentId()) && !ids2.contains(item.getParentId()))
                .forEach(item -> {
                    item.setParentId(null);
                    permissionMapper.update(item);
                });
    }

    private SysPermissionEntity syncPermission(AuthorizeInfo info, Map<String, SysPermissionEntity> codeToEntity) {
        String authorizeKey = info.authorizeKey();
        SysPermissionEntity entity = codeToEntity.get(authorizeKey);
        boolean isNew = (entity == null);
        String description = StringUtils.defaultIfBlank(info.getDescription(), info.authorizeKey());
        String parentKey = StringUtils.substringBeforeLast(authorizeKey, ":");

        if (isNew) {
            entity = new SysPermissionEntity();
            entity.setId(info.getId());
            entity.setCode(authorizeKey);
            entity.setName(description);
            entity.setDescription(description);
            entity.setCreatedAt(Instant.now());
            entity.setCreatedBy("system");
        } else {
            entity.setUpdatedAt(Instant.now());
            entity.setUpdatedBy("system");
        }
        entity.setIsDeleted(false);
        entity.setType(info.isGroup() ? PermissionType.GROUP : PermissionType.PERM);

        SysPermissionEntity parentEntity = codeToEntity.get(parentKey);
        if (parentEntity != null && !StringUtils.equals(parentEntity.getId(), entity.getId())) {
            entity.setParentId(parentEntity.getId());
        } else {
            entity.setParentId(null);
        }

        if (isNew) {
            permissionMapper.insert(entity);
        } else {
            permissionMapper.update(entity);
        }
        codeToEntity.put(authorizeKey, entity);
        return entity;
    }

    /**
     * 查询列表
     */
    public List<PermissionResponse> list(PermissionQueryRequest req) {
        QueryWrapper qw = QueryWrapper.create()
                .where("(is_deleted = 0 OR is_deleted IS NULL)");
        if (StringUtils.isNotBlank(req.getKeyword())) {
            qw.and(SysPermissionEntityTableDef.SYS_PERMISSION_ENTITY.NAME.like(req.getKeyword())
                    .or(SysPermissionEntityTableDef.SYS_PERMISSION_ENTITY.CODE.like(req.getKeyword())));
        }
        qw.orderBy(SysPermissionEntityTableDef.SYS_PERMISSION_ENTITY.SORT.asc());
        return permissionMapper.selectListByQuery(qw).stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * 创建权限
     */
    @Transactional
    public PermissionResponse create(PermissionCreateRequest req) {
        validateParent(req.getParentId());

        SysPermissionEntity e = new SysPermissionEntity();
        BeanMapper.mapper(req, e);
        e.setId(UUID.randomUUID().toString());
        e.setIsDeleted(false);
        e.setCreatedAt(Instant.now());
        e.setCreatedBy(UserHolder.username());

        permissionMapper.insert(e);
        return toResponse(e);
    }

    /**
     * 更新权限
     */
    @Transactional
    public PermissionResponse update(String id, PermissionUpdateRequest req) {
        SysPermissionEntity e = permissionMapper.selectOneById(id);
        if (e == null) {
            throw new RuntimeException("Permission not found");
        }

        boolean isBuiltIn = BUILT_IN_CODES.contains(e.getCode());
        if (isBuiltIn) {
            e.setDescription(req.getDescription());
            e.setSort(req.getSort());
            e.setName(req.getName());
            e.setParentId(req.getParentId());
        } else {
            validateParent(req.getParentId());
            // 检查自身不能作为自己的父节点
            if (id.equals(req.getParentId())) {
                throw new RuntimeException("Cannot set self as parent");
            }

            BeanMapper.mapper(req, e);
        }

        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        permissionMapper.update(e);
        return toResponse(e);
    }

    /**
     * 删除权限
     */
    @Transactional
    public void delete(String id) {
        SysPermissionEntity e = permissionMapper.selectOneById(id);
        if (e == null)
            return;

        if (BUILT_IN_CODES.contains(e.getCode())) {
            throw new RuntimeException("Cannot delete built-in permission");
        }

        // 检查是否有子节点
        long count = permissionMapper.selectCountByQuery(QueryWrapper.create().where("parent_id = ?", id));
        if (count > 0) {
            throw new RuntimeException("Cannot delete permission with children");
        }

        e.setIsDeleted(true);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        permissionMapper.update(e);
    }

    private void validateParent(String parentId) {
        if (parentId != null && !parentId.isBlank()) {
            SysPermissionEntity parent = permissionMapper.selectOneById(parentId);
            if (parent == null) {
                throw new RuntimeException("Parent permission not found");
            }
            if (parent.getType() == PermissionType.PERM) {
                throw new RuntimeException("Cannot add child to 'perm' type permission");
            }
        }
    }

    private PermissionResponse toResponse(SysPermissionEntity e) {
        PermissionResponse r = new PermissionResponse();
        BeanMapper.mapper(e, r);
        r.setBuiltIn(BUILT_IN_CODES.contains(e.getCode()));
        return r;
    }

    @Data
    @Accessors(chain = true)
    private static class AuthorizeInfo {
        private String id;
        private String description;
        private String authorize;
        private String preAuthorize;

        private boolean group;
        private boolean update;

        public String authorizeKey() {
            return StringUtils.defaultIfBlank(authorize, preAuthorize);
        }
    }
}
