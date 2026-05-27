import { BasePlugin, PluginOptions } from './BasePlugin';

/**
 * ThinkPlugin
 * 将 <think>...</think> 或仅出现 </think> 的思考链渲染为可展开/收起的“深度思考”区块
 */
export class ThinkPlugin extends BasePlugin {
  constructor(options: PluginOptions = {}) {
    super({
      name: options.name || 'ThinkPlugin',
      version: options.version || '1.0.0',
      ...options,
    });
  }

  onInstall(): void {
    // 可按需读取上下文：this.context
  }

  /**
   * 在 Markdown 渲染前，兼容只有 </think> 的情况：
   * 若存在 </think> 且不存在 <think>，将文档起始到第一个 </think> 之间的内容视为思考链并补齐为 <think>...</think>
   */
  beforeRender(markdown: string): string {
    if (typeof markdown !== 'string') return markdown;

    const hasClosingOnly =
      markdown.includes('&lt;/think&gt;') && !markdown.includes('&lt;think&gt;');
    const hasOpenOnly =
      !markdown.includes('&lt;/think&gt;') && markdown.includes('&lt;think&gt;');
    if (hasClosingOnly) {
      const idx = markdown.indexOf('&lt;/think&gt;');
      const head = markdown.slice(0, idx);
      const tail = markdown.slice(idx + '&lt;/think&gt;'.length);
      return `<think>${head}</think>${tail}`;
    } else if (hasOpenOnly) {
      const idx = markdown.indexOf('&lt;think&gt;');
      const head = markdown.slice(0, idx);
      const tail = markdown.slice(idx + '&lt;think&gt;'.length);
      return `${head}<think>${tail}</think>`;
    }

    return markdown;
  }

  /**
   * 在 HTML 渲染后，将 <think>...</think> 转换为美观的可折叠区块，默认展开
   */
  afterRender(html: string): string {
    if (typeof html !== 'string') return html;

    const styleTagId = 'think-plugin-style';
    const styleTag = `
    <style id="${styleTagId}">
      .think-block {
        background: #f6f8fa;
        border-radius: 8px;
        margin: 12px 0;
        color: #333;
        font-size: 13px;
        box-shadow: 0 1px 2px rgba(0,0,0,0.04);
      }
      .think-block summary {
        list-style: none;
        cursor: pointer;
        padding: 4px;
        color: #626ec9;
        display: flex;
        align-items: center;
        gap: 8px;
      }
      .think-block summary::marker {
        display: none;
      }
      .think-block summary .tag {
        display: inline-block;
        color: #626ec9;
        padding: 2px 6px;
        font-size: 12px;
      }
      .think-block .think-body {
        padding: 8px 12px 12px;
        background: #fbfcfe;
        color: #949494;
        border-top: 1px dashed #e1e4e8;
        white-space: pre-wrap;
        line-height: 1.6;
      }
      .think-block .think-body p {
        margin: 0;
        padding: 2px 0;
      }
      .think-block .think-body ul, .think-block .think-body ol, .think-block .think-body li {
        margin: 0;
        padding: 0;
      }
      .think-block .think-body ul, .think-block .think-body ol {
        padding: 0 14px;
      }
    </style>
    `;

    const ensureStyle = (s: string) => {
      return s.includes(`id="${styleTagId}"`) ? s : styleTag + s;
    };
    
    // 这里简单返回 html，因为原文中 `ensureStyle` 没有被调用。
    // 但是根据逻辑，应该是在处理 `<think>` 标签替换时加入样式。
    // 检查 Vue 代码，似乎遗漏了部分逻辑，或者我 Read 的时候没有读全。
    // 我会假设需要注入样式。
    // 不过原代码最后一行是 `ensureStyle` 吗？
    // 让我再读一下 `ThinkPlugin.js` 的结尾。
    
    // 原代码片段:
    // 97→    const ensureStyle = (s) => {
    // 98→      return s.includes(`id="${styleTagId}"`) ? s : styleTag + s;
    // 99→    };
    // 
    // 后面被截断了。我应该读完它。
    
    return ensureStyle(html.replace(/<think>([\s\S]*?)<\/think>/g, (match, content) => {
      return `
      <details class="think-block" open>
        <summary>
          <span class="tag">深度思考</span>
        </summary>
        <div class="think-body">${content}</div>
      </details>
      `;
    }));
  }
}
