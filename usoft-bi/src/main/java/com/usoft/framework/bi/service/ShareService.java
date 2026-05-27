package com.usoft.framework.bi.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.bi.api.BatchUpdateAuthRequest;
import com.usoft.framework.bi.api.BatchUpdateAuthRequest.AuthItem;
import com.usoft.framework.bi.api.ShareCreateRequest;
import com.usoft.framework.bi.api.ShareResponse;
import com.usoft.framework.bi.api.enums.BizType;
import com.usoft.framework.bi.api.enums.ExpireType;
import com.usoft.framework.bi.entity.AuthorizationEntity;
import com.usoft.framework.bi.entity.ShareEntity;
import com.usoft.framework.bi.entity.table.ShareEntityTableDef;
import com.usoft.framework.bi.mapper.ShareMapper;
import com.usoft.framework.common.bean.BeanMapper;
import com.usoft.framework.core.security.UserHolder;

@Service
public class ShareService {
    private static final String KEY_SOURCE = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final ShareMapper shareMapper;
    private final AuthorizationService authorizationService;

    public ShareService(ShareMapper shareMapper, AuthorizationService authorizationService) {
        this.shareMapper = shareMapper;
        this.authorizationService = authorizationService;
    }

    @Transactional(rollbackFor = Exception.class)
    public ShareResponse create(ShareCreateRequest req) {
        // 先查询是否存在有效的分享
        QueryWrapper query = QueryWrapper.create()
                .from(ShareEntityTableDef.SHARE_ENTITY)
                .where(ShareEntityTableDef.SHARE_ENTITY.BIZ_TYPE.eq(req.getBizType()))
                .and(ShareEntityTableDef.SHARE_ENTITY.BIZ_ID.eq(req.getBizId()))
                .and(ShareEntityTableDef.SHARE_ENTITY.IS_DELETED.eq(false));

        if (req.getExpireType() == null) {
            req.setExpireType(ExpireType.PERMANENT);
        }

        switch (req.getExpireType()) {
            case ONE_DAY:
                req.setExpireAt(Instant.now().plus(1, java.time.temporal.ChronoUnit.DAYS));
                break;
            case SEVEN_DAY:
                req.setExpireAt(Instant.now().plus(7, java.time.temporal.ChronoUnit.DAYS));
                break;
            case THIRTY_DAY:
                req.setExpireAt(Instant.now().plus(30, java.time.temporal.ChronoUnit.DAYS));
                break;
            case PERMANENT:
                req.setExpireAt(null);
                break;
            default:
                break;
        }

        ShareResponse r = new ShareResponse();
        ShareEntity exist = shareMapper.selectOneByQuery(query);
        if (exist != null) {
            // 如果存在，更新过期时间
            exist.setExpireType(req.getExpireType());
            exist.setExpireAt(req.getExpireAt());
            exist.setUpdatedAt(Instant.now());
            exist.setUpdatedBy(UserHolder.username());
            shareMapper.update(exist);
            BeanMapper.mapper(exist, r);
        } else {
            // 创建新的分享
            ShareEntity entity = new ShareEntity();
            entity.setId(UUID.randomUUID().toString().replace("-", "")); // 简单UUID作为ID
            entity.setBizType(req.getBizType());
            entity.setBizId(req.getBizId());
            // 生成ShareKey，长度8位
            entity.setShareKey(generateShareKey());
            entity.setExpireType(req.getExpireType());
            entity.setExpireAt(req.getExpireAt());
            entity.setIsDeleted(false);
            entity.setCreatedAt(Instant.now());
            entity.setCreatedBy(UserHolder.username());
            entity.setUpdatedAt(Instant.now());
            entity.setUpdatedBy(UserHolder.username());

            shareMapper.insert(entity);
            BeanMapper.mapper(entity, r);
        }
        BatchUpdateAuthRequest authReq = new BatchUpdateAuthRequest();
        authReq.setBizType(BizType.SHARE);
        authReq.setBizId(r.getShareKey());
        authReq.setAuthorizations(req.getAuthorizations());
        authorizationService.update(authReq);

        return r;
    }

    public ShareResponse getByShareKey(BizType bizType, String shareKey) {
        ShareEntity entity = shareMapper.selectOneByQuery(QueryWrapper.create()
                .from(ShareEntityTableDef.SHARE_ENTITY)
                .where(ShareEntityTableDef.SHARE_ENTITY.BIZ_TYPE.eq(bizType))
                .and(ShareEntityTableDef.SHARE_ENTITY.SHARE_KEY.eq(shareKey))
                .and(ShareEntityTableDef.SHARE_ENTITY.IS_DELETED.eq(false)));
        ShareResponse r = new ShareResponse();
        BeanMapper.mapper(entity, r);
        return r;
    }

    public ShareResponse getByBizId(BizType bizType, String bizId) {
        ShareEntity entity = shareMapper.selectOneByQuery(QueryWrapper.create()
                .from(ShareEntityTableDef.SHARE_ENTITY)
                .where(ShareEntityTableDef.SHARE_ENTITY.BIZ_TYPE.eq(bizType))
                .and(ShareEntityTableDef.SHARE_ENTITY.BIZ_ID.eq(bizId))
                .and(ShareEntityTableDef.SHARE_ENTITY.IS_DELETED.eq(false)));
        if (entity == null) {
            return null;
        }
        List<AuthorizationEntity> auths = authorizationService.listByBizId(BizType.SHARE, entity.getShareKey());
        ShareResponse r = new ShareResponse();
        BeanMapper.mapper(entity, r);
        r.setAuthorizations(auths.stream()
                .map(a -> {
                    AuthItem item = new AuthItem();
                    item.setScope(a.getAuthorizerScope());
                    item.setAuthorizerId(a.getAuthorizerId());
                    return item;
                })
                .toList());
        return r;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean delete(BizType bizType, String bizId) {
        ShareEntity entity = shareMapper.selectOneByQuery(QueryWrapper.create()
                .from(ShareEntityTableDef.SHARE_ENTITY)
                .where(ShareEntityTableDef.SHARE_ENTITY.BIZ_TYPE.eq(bizType))
                .and(ShareEntityTableDef.SHARE_ENTITY.BIZ_ID.eq(bizId))
                .and(ShareEntityTableDef.SHARE_ENTITY.IS_DELETED.eq(false)));
        if (entity == null) {
            return false;
        }
        entity.setIsDeleted(true);
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(UserHolder.username());
        shareMapper.update(entity);
        authorizationService.delete(BizType.SHARE, entity.getShareKey());
        return true;
    }

    /**
     * 生成8位随机ShareKey
     */
    private String generateShareKey() {
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                int number = java.util.concurrent.ThreadLocalRandom.current().nextInt(62);
                sb.append(KEY_SOURCE.charAt(number));
            }
            if (shareMapper
                    .selectCountByQuery(QueryWrapper.create().eq(ShareEntity::getShareKey, sb.toString())) == 0) {
                return sb.toString();
            }
        } while (true);

    }
}
