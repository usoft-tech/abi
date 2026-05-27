package com.usoft.framework.system.service;

import static com.usoft.framework.system.entity.table.SysIconEntityTableDef.SYS_ICON_ENTITY;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.core.mybatis.PageHelper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.system.api.SysIconQueryRequest;
import com.usoft.framework.system.api.SysIconResponse;
import com.usoft.framework.system.entity.SysIconEntity;
import com.usoft.framework.system.mapper.SysIconMapper;
import com.usoft.framework.common.api.PageResponse;
import com.usoft.framework.common.bean.BeanMapper;

/**
 * 系统图标服务
 */
@Service
public class SysIconService {
    private final SysIconMapper sysIconMapper;

    /**
     * 构造图标服务
     */
    public SysIconService(SysIconMapper sysIconMapper) {
        this.sysIconMapper = sysIconMapper;
    }

    /**
     * 上传并创建图标
     * 
     * @param file        SVG文件
     * @param keepFill    是否保留填充色
     * @param name        图标名称
     * @param description 图标描述
     * @return 图标响应体
     */
    public SysIconResponse upload(MultipartFile file, boolean keepFill, String name, String description) {
        String tenantId = TenantContext.getTenantId();
        String id = UUID.randomUUID().toString();
        String originalName = file.getOriginalFilename();
        String contentType = file.getContentType();
        if (!isSvgFile(originalName, contentType)) {
            throw new IllegalArgumentException("仅支持SVG文件");
        }
        try {
            String svg = new String(file.getBytes(), StandardCharsets.UTF_8);
            String finalSvg = keepFill ? svg : stripFillColors(svg);
            SysIconEntity e = new SysIconEntity();
            e.setId(id);
            e.setTenantId(tenantId);
            e.setName(name != null && !name.isBlank() ? name : safeName(originalName));
            e.setSvg(finalSvg);
            e.setDescription(description);
            e.setIsDeleted(false);
            e.setCreatedAt(Instant.now());
            e.setCreatedBy(UserHolder.username());
            sysIconMapper.insert(e);
            SysIconResponse r = new SysIconResponse();
            r.setId(e.getId());
            r.setName(e.getName());
            r.setDescription(e.getDescription());
            return r;
        } catch (IOException ex) {
            throw new IllegalStateException("SVG读取失败", ex);
        }
    }

    /**
     * 删除图标
     */
    public boolean delete(String id) {
        String tenantId = TenantContext.getTenantId();
        String username = UserHolder.username();
        SysIconEntity e = sysIconMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId()) || !username.equals(e.getCreatedBy())) {
            return false;
        }
        e.setIsDeleted(true);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(username);
        return sysIconMapper.update(e) > 0;
    }

    /**
     * 列出图标
     */
    public List<SysIconResponse> listAll(SysIconQueryRequest req) {
        String tenantId = TenantContext.getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            return List.of();
        }
        QueryWrapper qw = QueryWrapper.create()
                .select(SYS_ICON_ENTITY.ID, SYS_ICON_ENTITY.NAME, SYS_ICON_ENTITY.DESCRIPTION)
                .where(SYS_ICON_ENTITY.TENANT_ID.eq(tenantId))
                .and(SYS_ICON_ENTITY.IS_DELETED.eq(false).or(SYS_ICON_ENTITY.IS_DELETED.isNull()));
        if (req.getName() != null && !req.getName().isBlank()) {
            qw.and(SYS_ICON_ENTITY.NAME.like(req.getName()));
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            qw.and(SYS_ICON_ENTITY.NAME.like(req.getKeyword()).or(SYS_ICON_ENTITY.DESCRIPTION.like(req.getKeyword())));
        }
        List<SysIconEntity> list = sysIconMapper.selectListByQuery(qw);
        return list.stream().map(e -> {
            SysIconResponse r = new SysIconResponse();
            BeanMapper.mapper(e, r);
            return r;
        }).toList();
    }

    /**
     * 列出图标
     */
    public PageResponse<SysIconResponse> list(SysIconQueryRequest req) {
        String tenantId = TenantContext.getTenantId();
        int page = req.getPage();
        int size = req.getSize();
        if (StringUtils.isBlank(tenantId)) {
            return new PageResponse<>(page, size, 0, List.of());
        }
        QueryWrapper qw = QueryWrapper.create()
                .select(SYS_ICON_ENTITY.ID, SYS_ICON_ENTITY.NAME, SYS_ICON_ENTITY.DESCRIPTION)
                .where(SYS_ICON_ENTITY.TENANT_ID.eq(tenantId))
                .and(SYS_ICON_ENTITY.IS_DELETED.eq(false).or(SYS_ICON_ENTITY.IS_DELETED.isNull()));
        if (req.getName() != null && !req.getName().isBlank()) {
            qw.and(SYS_ICON_ENTITY.NAME.like(req.getName()));
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            qw.and(SYS_ICON_ENTITY.NAME.like(req.getKeyword()).or(SYS_ICON_ENTITY.DESCRIPTION.like(req.getKeyword())));
        }
        return PageHelper.apply(sysIconMapper, qw, req, e -> {
            SysIconResponse r = new SysIconResponse();
            r.setId(e.getId());
            r.setName(e.getName());
            r.setDescription(e.getDescription());
            return r;
        });
    }

    /**
     * 生成 Iconfont 风格的聚合字符串
     * 
     * @param tenantId 租户ID
     * @return 形如 window._iconfont_svg_string_{tenantId}='...';
     */
    public String iconfontString(String tenantId) {
        String key = StringUtils.isBlank(tenantId) ? "default" : tenantId;
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");

        List<SysIconEntity> rows = sysIconMapper.selectListByQuery(qw);
        StringBuilder symbols = new StringBuilder();
        for (SysIconEntity e : rows) {
            String svg = e.getSvg();
            String viewBox = extractViewBox(svg);
            String inner = extractInnerSvgContent(svg);
            symbols.append("<symbol id=\"icon-")
                    .append(e.getId())
                    .append("\" viewBox=\"")
                    .append(viewBox)
                    .append("\">")
                    .append(inner)
                    .append("</symbol>");
        }
        String aggregated = "<svg>" + symbols + "</svg>";
        String escaped = escapeJsString(aggregated);
        return "window._iconfont_svg_string_" + key + "='" + escaped + "',"
                + """
                        (c => {
                            var h = (v = (v = document.getElementsByTagName("script"))[v.length - 1]).getAttribute("data-injectcss")
                              , v = v.getAttribute("data-disable-injectsvg");
                            if (!v) {
                                var a, m, z, l, o, t = function(h, v) {
                                    v.parentNode.insertBefore(h, v)
                                };
                                if (h && !c.__iconfont__svg__cssinject__) {
                                    c.__iconfont__svg__cssinject__ = !0;
                                    try {
                                        document.write("<style>.svgfont {display: inline-block;width: 1em;height: 1em;fill: currentColor;vertical-align: -0.1em;font-size:16px;}</style>")
                                    } catch (h) {
                                        console && console.log(h)
                                    }
                                }
                                a = function() {
                                    var id = "_iconfont_svg_string_%s";
                                    var exists = document.getElementById(id);
                                    if (exists) {
                                        exists.remove();
                                    }
                                    var h, v = document.createElement("div");
                                    v.innerHTML = c._iconfont_svg_string_%s,
                                    (v = v.getElementsByTagName("svg")[0]) && (v.setAttribute("aria-hidden", "true"),
                                    v.setAttribute("id", id),
                                    v.style.position = "absolute",
                                    v.style.width = 0,
                                    v.style.height = 0,
                                    v.style.overflow = "hidden",
                                    v = v,
                                    (h = document.body).firstChild ? t(v, h.firstChild) : h.appendChild(v))
                                }
                                ,
                                document.addEventListener ? ~["complete", "loaded", "interactive"].indexOf(document.readyState) ? setTimeout(a, 0) : (m = function() {
                                    document.removeEventListener("DOMContentLoaded", m, !1),
                                    a()
                                }
                                ,
                                document.addEventListener("DOMContentLoaded", m, !1)) : document.attachEvent && (z = a,
                                l = c.document,
                                o = !1,
                                s(),
                                l.onreadystatechange = function() {
                                    "complete" == l.readyState && (l.onreadystatechange = null,
                                    i())
                                }
                                )
                            }
                            function i() {
                                o || (o = !0,
                                z())
                            }
                            function s() {
                                try {
                                    l.documentElement.doScroll("left")
                                } catch (h) {
                                    return void setTimeout(s, 50)
                                }
                                i()
                            }
                        }
                        )(window);
                        """
                        .formatted(tenantId, tenantId);
    }

    /**
     * 判断文件是否为SVG
     */
    private boolean isSvgFile(String filename, String contentType) {
        if (contentType != null && contentType.equalsIgnoreCase("image/svg+xml")) {
            return true;
        }
        if (filename == null)
            return false;
        String base = filename.replace("\\", "/");
        int i = base.lastIndexOf('.');
        if (i < 0)
            return false;
        String ext = base.substring(i + 1).toLowerCase();
        return "svg".equals(ext);
    }

    /**
     * 去除填充色
     */
    private String stripFillColors(String svg) {
        String s = svg.replaceAll("(?i)\\sfill\\s*=\\s*\"[^\"]*\"", " fill=\"currentColor\"");
        s = s.replaceAll("(?i)fill\\s*:[^;\"']+;?", "");
        return s;
    }

    /**
     * 提取 viewBox 属性值
     * 
     * @param svg SVG源码
     * @return viewBox 值，默认 0 0 1024 1024
     */
    private String extractViewBox(String svg) {
        if (svg == null)
            return "0 0 1024 1024";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?is)viewBox\\s*=\\s*\"([^\"]+)\"").matcher(svg);
        if (m.find()) {
            String vb = m.group(1).trim();
            return vb.isEmpty() ? "0 0 1024 1024" : vb;
        }
        return "0 0 1024 1024";
    }

    /**
     * 提取 <svg> 内部内容
     * 
     * @param svg SVG源码
     * @return 去除外层 <svg> 的内部片段
     */
    private String extractInnerSvgContent(String svg) {
        if (svg == null)
            return "";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?is)<svg[^>]*>([\\s\\S]*?)</svg>").matcher(svg);
        if (m.find()) {
            return m.group(1);
        }
        return svg;
    }

    /**
     * 将字符串转义为可用的 JS 单引号字符串
     * 
     * @param s 原始字符串
     * @return 已转义字符串
     */
    private String escapeJsString(String s) {
        if (s == null)
            return "";
        String r = s.replace("\\", "\\\\");
        r = r.replace("'", "\\'");
        r = r.replace("\r", "");
        r = r.replace("\n", "");
        return r;
    }

    /**
     * 安全名称
     */
    private String safeName(String name) {
        if (name == null || name.isBlank())
            return "icon";
        String base = name.replace("\\", "/");
        int p = base.lastIndexOf('/');
        if (p >= 0)
            base = base.substring(p + 1);
        int dot = base.lastIndexOf('.');
        if (dot > 0)
            base = base.substring(0, dot);
        return base;
    }

}
