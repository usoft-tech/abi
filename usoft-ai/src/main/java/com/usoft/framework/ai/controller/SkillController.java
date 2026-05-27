package com.usoft.framework.ai.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.usoft.framework.common.annotation.AuthorizeDescription;
import com.usoft.framework.common.annotation.AuthorizeDescriptionGroup;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.usoft.framework.ai.AgentScopeProxy;
import com.usoft.framework.common.api.ApiResponse;

import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.util.MarkdownSkillParser;
import io.agentscope.core.skill.util.MarkdownSkillParser.ParsedMarkdown;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@AuthorizeDescriptionGroup({
    @AuthorizeDescription(value = "AI管理", authorize = "ai", group = true),
    @AuthorizeDescription(value = "AI技能", authorize = "ai:skills", group = true),
})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/ai/skills", produces = MediaType.APPLICATION_JSON_VALUE)
public class SkillController {

    private final AgentScopeProxy agentScopeProxy;

    @GetMapping
    @AuthorizeDescription(value = "获取技能列表", authorize = "ai:skills:list")
    @PreAuthorize("@ss.hasAuthority('ai:skills:list')")
    public ApiResponse<List<SkillListItemResponse>> list() {
        List<AgentSkill> skills = agentScopeProxy.getSkillRepository().getAllSkills();
        List<SkillListItemResponse> skillListItemResponses = skills.stream()
                .map(skill -> SkillListItemResponse.builder()
                        .name(skill.getName())
                        .description(skill.getDescription())
                        .build())
                .toList();
        return ApiResponse.ok(skillListItemResponses);
    }

    @PostMapping
    @AuthorizeDescription(value = "创建技能", authorize = "ai:skills:create")
    @PreAuthorize("@ss.hasAuthority('ai:skills:create')")
    public ApiResponse<Boolean> create(@RequestBody AgentSkillFormValues request) {
        agentScopeProxy.getSkillRepository().save(List.of(request.toAgentSkill()), false);
        return ApiResponse.ok(true);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @AuthorizeDescription(value = "导入技能", authorize = "ai:skills:create")
    @PreAuthorize("@ss.hasAuthority('ai:skills:create')")
    public ApiResponse<Boolean> importSkill(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            String skillContent = null;
            String name = null;
            String description = null;
            Map<String, String> resources = new HashMap<>();

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                String fileName = entry.getName();
                // 简化路径，只保留文件名，因为 resources 是扁平的 map
                if (fileName.contains("/")) {
                    fileName = fileName.substring(fileName.indexOf("/") + 1);
                }
                if (fileName.contains("\\")) {
                    fileName = fileName.substring(fileName.indexOf("\\") + 1);
                }

                // 忽略隐藏文件
                if (fileName.startsWith(".")) {
                    continue;
                }

                // 读取内容
                java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }
                String content = bos.toString(StandardCharsets.UTF_8);

                if ("SKILL.md".equalsIgnoreCase(fileName)) {
                    skillContent = content;
                    // 解析 name 和 description
                    ParsedMarkdown parsed = MarkdownSkillParser.parse(skillContent);
                    Map<String, String> metadata = parsed.getMetadata();

                    name = metadata.get("name");
                    description = metadata.get("description");
                    skillContent = parsed.getContent();
                } else {
                    resources.put(fileName, content);
                }
                zis.closeEntry();
            }

            if (skillContent == null) {
                throw new IllegalArgumentException("SKILL.md not found in zip file");
            }
            if (name == null) {
                throw new IllegalArgumentException("Could not parse 'name' from SKILL.md");
            }
            if (description == null) {
                description = "";
            }

            AgentSkill skill = AgentSkill.builder()
                    .name(name)
                    .description(description)
                    .skillContent(skillContent)
                    .resources(resources)
                    .build();

            agentScopeProxy.getSkillRepository().save(List.of(skill), false);
            return ApiResponse.ok(true);
        }
    }

    @PutMapping
    @AuthorizeDescription(value = "更新技能", authorize = "ai:skills:update")
    @PreAuthorize("@ss.hasAuthority('ai:skills:update')")
    public ApiResponse<Boolean> update(@RequestBody AgentSkillFormValues request) {

        agentScopeProxy.getSkillRepository().save(List.of(request.toAgentSkill()), true);
        return ApiResponse.ok(true);
    }

    @GetMapping("/{skillName}")
    @AuthorizeDescription(value = "获取技能详情", authorize = "ai:skills:get")
    @PreAuthorize("@ss.hasAuthority('ai:skills:get')")
    public ApiResponse<AgentSkillFormValues> get(@PathVariable String skillName) {
        AgentSkill skill = agentScopeProxy.getSkillRepository().getSkill(skillName);
        return ApiResponse.ok(AgentSkillFormValues.fromAgentSkill(skill));
    }

    @DeleteMapping("/{skillName}")
    @AuthorizeDescription(value = "删除技能", authorize = "ai:skills:delete")
    @PreAuthorize("@ss.hasAuthority('ai:skills:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String skillName) {
        agentScopeProxy.getSkillRepository().delete(skillName);
        return ApiResponse.ok(true);
    }

    @Data
    @Builder
    private static class SkillListItemResponse {
        private String name;
        private String description;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class AgentSkillFormValues {
        private String name;
        private String description;
        private String skillContent;
        private Map<String, String> resources;

        public AgentSkill toAgentSkill() {
            return AgentSkill.builder()
                    .name(name)
                    .description(description)
                    .skillContent(skillContent)
                    .resources(resources)
                    .build();
        }

        public static AgentSkillFormValues fromAgentSkill(AgentSkill skill) {
            return AgentSkillFormValues.builder()
                    .name(skill.getName())
                    .description(skill.getDescription())
                    .skillContent(skill.getSkillContent())
                    .resources(skill.getResources())
                    .build();
        }
    }
}
