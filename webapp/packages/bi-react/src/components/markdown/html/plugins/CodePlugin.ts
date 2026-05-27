import { BasePlugin, PluginOptions } from './BasePlugin';
import hljs from 'highlight.js';

interface CodePluginOptions extends PluginOptions {
  autoDetect?: boolean;
  languages?: string[];
  theme?: string;
  ignoreIllegals?: boolean;
}

/**
 * 代码高亮插件，使用 highlight.js 为代码块添加语法高亮
 */
export class CodePlugin extends BasePlugin {
  config: CodePluginOptions;

  constructor(options: CodePluginOptions = {}) {
    super({
      name: 'CodePlugin',
      version: '1.0.0',
      ...options,
    });

    // 插件配置选项
    this.config = {
      // 是否自动检测语言
      autoDetect: options.autoDetect !== false,
      // 支持的语言列表，空数组表示支持所有语言
      languages: options.languages || [],
      // 主题样式类名
      theme: options.theme || 'default',
      // 是否忽略未识别的语言
      ignoreIllegals: options.ignoreIllegals !== false,
      ...options,
    };

    // 注入样式
    this.injectStyles();
  }

  injectStyles() {
    // React 中通常通过 CSS 引入样式，或者这里动态注入 style 标签
    // 这里保留不做操作，或者如果需要动态注入 CSS 可以实现
  }

  /**
   * 保留换行符，修复 hljs.highlight 可能丢失换行符的问题
   * @param {string} originalCode - 原始代码
   * @param {string} highlightedCode - 高亮后的代码
   * @returns {string} 修复后的高亮代码
   */
  preserveNewlines(originalCode: string, highlightedCode: string): string {
    const originalLines = originalCode.split('\n');
    const highlightedLines = highlightedCode.split('\n');

    // 如果行数不匹配，说明换行符可能丢失了
    if (originalLines.length !== highlightedLines.length) {
      console.warn('检测到换行符丢失，尝试修复...');

      // 方案1: 如果高亮代码完全没有换行符，但原始代码有多行
      if (highlightedLines.length === 1 && originalLines.length > 1) {
        // 尝试按行重新高亮
        try {
          const language = this.detectLanguage(originalCode);
          const reHighlightedLines = originalLines.map((line) => {
            if (line.trim() === '') return ''; // 保留空行
            try {
              return hljs.highlight(line, { language }).value;
            } catch (e) {
              return this.escapeHtml(line);
            }
          });
          return reHighlightedLines.join('\n');
        } catch (e) {
          console.warn('按行重新高亮失败，使用原始换行符修复');
          return this.insertNewlinesBasedOnOriginal(
            originalCode,
            highlightedCode
          );
        }
      }

      // 方案2: 基于原始代码的换行位置插入换行符
      return this.insertNewlinesBasedOnOriginal(originalCode, highlightedCode);
    }

    return highlightedCode;
  }

  escapeHtml(text: string): string {
    return text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  /**
   * 检测代码语言
   * @param {string} code - 代码内容
   * @returns {string} 检测到的语言
   */
  detectLanguage(code: string): string {
    try {
      const result = hljs.highlightAuto(code);
      return result.language || 'plaintext';
    } catch (e) {
      return 'plaintext';
    }
  }

  /**
   * 基于原始代码的换行位置在高亮代码中插入换行符
   * @param {string} originalCode - 原始代码
   * @param {string} highlightedCode - 高亮后的代码
   * @returns {string} 修复后的代码
   */
  insertNewlinesBasedOnOriginal(
    originalCode: string,
    highlightedCode: string
  ): string {
    const originalLines = originalCode.split('\n');

    // 如果只有一行，直接返回
    if (originalLines.length <= 1) return highlightedCode;

    // 简单策略：无法完美恢复，直接返回高亮代码，或者尝试更复杂的 diff 算法
    // 这里为了简化，如果行数不一致，我们只警告，或者返回高亮代码
    return highlightedCode;
  }
}
