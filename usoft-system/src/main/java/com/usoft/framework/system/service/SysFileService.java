package com.usoft.framework.system.service;

import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.system.api.SysFileResponse;
import com.usoft.framework.system.entity.SysFileEntity;
import com.usoft.framework.system.mapper.SysFileMapper;
import com.usoft.framework.common.bean.BeanMapper;
import com.usoft.framework.system.config.UploadProperties;
import com.usoft.framework.core.security.UserHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.List;
import java.util.Objects;

@Service
public class SysFileService {
    private final SysFileMapper sysFileMapper;
    private final UploadProperties uploadProperties;

    public SysFileService(SysFileMapper sysFileMapper, UploadProperties uploadProperties) {
        this.sysFileMapper = sysFileMapper;
        this.uploadProperties = uploadProperties;
    }

    /**
     * 创建文件记录并保存上传内容
     * @param file 上传的文件
     * @param type 业务类型
     * @return 文件响应体
     */
    public SysFileResponse create(MultipartFile file, String type) {
        String tenantId = TenantContext.getTenantId();
        String id = UUID.randomUUID().toString();
        String originalName = file.getOriginalFilename();
        String contentType = file.getContentType();
        long size = file.getSize();

        validateFile(originalName, contentType, size);

        try {
            Path root = resolveStorageRoot();
            LocalDate today = LocalDate.now();
            Path sub = root
                    .resolve(tenantId)
                    .resolve(safeType(type))
                    .resolve(String.format("%04d", today.getYear()))
                    .resolve(String.format("%02d", today.getMonthValue()))
                    .resolve(String.format("%02d", today.getDayOfMonth()));
            Files.createDirectories(sub);
            String safeName = sanitizeFileName(originalName == null ? "file" : originalName);
            String finalName = id + "." + extractExtension(safeName);
            Path target = sub.resolve(finalName);
            file.transferTo(target.toFile());
            String storagePath = root.relativize(target).toString().replace("\\", "/");

            SysFileEntity e = new SysFileEntity();
            e.setId(id);
            e.setTenantId(tenantId);
            e.setName(safeName);
            e.setUrl(resolveVisitUrl(tenantId, type, today, finalName, target));
            e.setContentType(contentType);
            e.setSize(size);
            e.setStoragePath(storagePath);
            e.setIsDeleted(false);
            e.setCreatedAt(Instant.now());
            e.setCreatedBy(UserHolder.username());
            sysFileMapper.insert(e);

            SysFileResponse r = new SysFileResponse();
            BeanMapper.mapper(e, r);
            return r;
        } catch (IOException ex) {
            throw new IllegalStateException("文件保存失败", ex);
        }
    }

    /**
     * 根据文件ID列表查询文件记录
     * @param ids 文件ID列表
     * @return 文件响应体列表
     */
    public List<SysFileResponse> listByIds(List<String> ids) {
        List<SysFileEntity> entities = sysFileMapper.selectListByIds(ids);
        return entities.stream().map(e -> {
            SysFileResponse r = new SysFileResponse();
            BeanMapper.mapper(e, r);
            return r;
        }).toList();
    }
    /**
     * 校验文件大小与类型
     */
    private void validateFile(String filename, String contentType, long size) {
        long max = uploadProperties.getMaxFileSize();
        if (size > max) {
            throw new IllegalArgumentException("文件大小超出限制");
        }
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new IllegalArgumentException("不支持的文件类型");
        }
        String ext = extractExtension(filename);
        if (ext == null || !isAllowedExtension(ext)) {
            throw new IllegalArgumentException("不支持的文件扩展名");
        }
    }

    /**
     * 判断文件类型是否允许
     */
    private boolean isAllowedContentType(String ct) {
        String[] allowed = uploadProperties.getAllowedFileTypes();
        if (allowed == null) return true;
        for (String rule : allowed) {
            if (rule == null) continue;
            int idx = rule.indexOf('/');
            if (idx > 0 && rule.endsWith("/*")) {
                String group = rule.substring(0, idx + 1);
                if (ct.startsWith(group)) return true;
            } else {
                if (Objects.equals(ct, rule)) return true;
            }
        }
        return false;
    }

    /**
     * 判断扩展名是否允许
     */
    private boolean isAllowedExtension(String ext) {
        String[] allowed = uploadProperties.getAllowedFileExtensions();
        if (allowed == null) return true;
        for (String a : allowed) {
            if (Objects.equals(a, ext)) return true;
        }
        return false;
    }

    /**
     * 解析存储根目录
     */
    private Path resolveStorageRoot() {
        String root = uploadProperties.getStoragePath();
        if (root == null || root.isBlank()) {
            root = "uploads";
        }
        return Paths.get(root);
    }

    /**
     * 解析可访问URL
     */
    private String resolveVisitUrl(String tenantId, String type, LocalDate d, String filename, Path target) {
        String prefix = uploadProperties.getVisitPathPrefix();
        if (prefix == null || prefix.isBlank()) {
            return target.toString();
        }
        String datePath = String.format("%04d/%02d/%02d", d.getYear(), d.getMonthValue(), d.getDayOfMonth());
        return String.join("/", prefix, tenantId, safeType(type), datePath, filename);
    }

    /**
     * 清理文件名中的路径片段
     */
    private String sanitizeFileName(String name) {
        return Paths.get(name).getFileName().toString();
    }

    /**
     * 提取扩展名
     */
    private String extractExtension(String name) {
        if (name == null) return null;
        String base = sanitizeFileName(name);
        int i = base.lastIndexOf('.');
        if (i < 0 || i == base.length() - 1) return null;
        return base.substring(i + 1).toLowerCase();
    }

    /**
     * 业务类型安全化
     */
    private String safeType(String type) {
        if (type == null || type.isBlank()) return "general";
        return type.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    
}
