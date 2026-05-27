import MarkdownIt from 'markdown-it';
// @ts-ignore
import markdownItAbbr from 'markdown-it-abbr/dist/markdown-it-abbr.min.js';
// @ts-ignore
import markdownDefList from 'markdown-it-deflist';
// @ts-ignore
import { full as markdownItEmoji } from 'markdown-it-emoji';
// @ts-ignore
import markdownItFootnote from 'markdown-it-footnote';
// @ts-ignore
import markdownItIns from 'markdown-it-ins';
// @ts-ignore
import markdownItMark from 'markdown-it-mark';
// @ts-ignore
import markdownItSub from 'markdown-it-sub';
// @ts-ignore
import markdownItSup from 'markdown-it-sup';
// @ts-ignore
import markdownItTaskLists from 'markdown-it-task-lists';
import { PluginManager } from './plugins/PluginManager';
import {
  EChartsPlugin,
  CodePlugin,
  ThinkPlugin,
  LatexPlugin,
  BasePlugin,
} from './plugins';

export interface Md2HtmlOptions {
  markdown?: string;
  plugins?: BasePlugin[];
  mdTitleFontFamily?: string;
  smallTitleFontFamily?: string;
  bodyFontFamily?: string;
  mdTitleFontSize?: string | number;
  smallTitleFontSize?: string | number;
  bodyFontSize?: number;
  mdTitleColor?: string;
  smallTitleColor?: string;
  bodyColor?: string;
  mdTitleAlignment?: string;
  smallTitleAlignment?: string;
  bodyIndent?: number;
  lineHeight?: number;
  context?: any;
}

export interface MdStyles {
  mdTitleFontFamily?: string;
  smallTitleFontFamily?: string;
  bodyFontFamily?: string;
  mdTitleFontSize?: string | number;
  smallTitleFontSize?: string | number;
  bodyFontSize?: number;
  mdTitleColor?: string;
  smallTitleColor?: string;
  bodyColor?: string;
  mdTitleAlignment?: string;
  smallTitleAlignment?: string;
  bodyIndent?: number;
  lineHeight?: number;
}

export default class Md2Html {
  md: MarkdownIt;
  renderedHtml: string;
  pluginManager: PluginManager | null;
  markdown: string;
  plugins: BasePlugin[];
  mdTitleFontFamily: string;
  smallTitleFontFamily: string;
  bodyFontFamily: string;
  mdTitleFontSize: string | number;
  smallTitleFontSize: string | number;
  bodyFontSize: number;
  mdTitleColor: string;
  smallTitleColor: string;
  bodyColor: string;
  mdTitleAlignment: string;
  smallTitleAlignment: string;
  bodyIndent: number;
  lineHeight: number;
  context: any;

  constructor(options: Md2HtmlOptions = {}) {
    this.md = new MarkdownIt();
    this.renderedHtml = '';
    this.pluginManager = null;
    this.markdown = options.markdown || '';
    this.plugins = Array.isArray(options.plugins) ? options.plugins : [];
    this.mdTitleFontFamily =
      options.mdTitleFontFamily || "'黑体', 'SimHei', sans-serif";
    this.smallTitleFontFamily =
      options.smallTitleFontFamily || "'黑体', 'SimHei', sans-serif";
    this.bodyFontFamily =
      options.bodyFontFamily || "'仿宋', 'FangSong', serif";
    this.mdTitleFontSize = options.mdTitleFontSize || '22';
    this.smallTitleFontSize = options.smallTitleFontSize || '14';
    this.bodyFontSize = options.bodyFontSize || 14;
    this.mdTitleColor = options.mdTitleColor || '#000000';
    this.smallTitleColor = options.smallTitleColor || '#000000';
    this.bodyColor = options.bodyColor || '#333333';
    this.mdTitleAlignment = options.mdTitleAlignment || 'center';
    this.smallTitleAlignment = options.smallTitleAlignment || 'left';
    this.bodyIndent = options.bodyIndent || 2;
    this.lineHeight = options.lineHeight || 29;
    this.context = options.context || {};

    this.init();
  }

  init() {
    this.md.use(markdownItAbbr);
    this.md.use(markdownDefList);
    this.md.use(markdownItEmoji);
    this.md.use(markdownItFootnote);
    this.md.use(markdownItIns);
    this.md.use(markdownItMark);
    this.md.use(markdownItSub);
    this.md.use(markdownItSup);
    this.md.use(markdownItTaskLists);
    this.initializePlugins();
  }

  initializePlugins() {
    if (this.pluginManager) {
      this.pluginManager.clear();
    }
    this.pluginManager = new PluginManager();
    this.pluginManager.setContext(this.context);
    this.plugins.forEach((plugin) => {
      this.pluginManager!.register(plugin);
    });
    this.pluginManager.callHook('mounted');
  }

  renderMarkdown() {
    if (!this.pluginManager) {
      this.initializePlugins();
    }
    this.pluginManager!.callHook('beforeRender');
    let html = this.md.render(this.markdown);
    html = html.replace(/<h([6])>(.*?)<\/h[6]>/, (match, level, content) => {
      return `<h${level} class="mdTitle" style="font-family: ${this.mdTitleFontFamily}; font-size: ${this.mdTitleFontSize}pt; font-weight: bold; line-height: ${this.lineHeight}px; text-align: ${this.mdTitleAlignment}; color: ${this.mdTitleColor};">${content}</h${level}>`;
    });
    html = html.replace(/<p>(.*?)<\/p>/g, (match, content) => {
      return `<p style="font-family: ${this.bodyFontFamily}; font-size: ${this.bodyFontSize}pt; line-height: ${this.lineHeight}px; text-indent: ${this.bodyIndent}em; color: ${this.bodyColor};">${content}</p>`;
    });
    html = html.replace(
      /<h([1-3])>(.*?)<\/h[1-3]>/g,
      (match, level, content) => {
        return `<h${level} style="font-family: ${this.smallTitleFontFamily}; font-size: ${this.smallTitleFontSize}pt; font-weight: bold; line-height: ${this.lineHeight}px; text-align: ${this.smallTitleAlignment}; color: ${this.smallTitleColor};">${content}</h${level}>`;
      }
    );
    const extra = {};
    html =
      this.pluginManager!.callHook('afterRender', html, extra) || html;
    this.renderedHtml = html;
    return { html, extra };
  }

  setMarkdown(markdown: string) {
    this.markdown = markdown || '';
  }

  setPlugins(plugins: BasePlugin[]) {
    this.plugins = Array.isArray(plugins) ? plugins : [];
  }

  updateStyles(styles: MdStyles = {}) {
    if (styles.mdTitleFontFamily !== undefined)
      this.mdTitleFontFamily = styles.mdTitleFontFamily;
    if (styles.smallTitleFontFamily !== undefined)
      this.smallTitleFontFamily = styles.smallTitleFontFamily;
    if (styles.bodyFontFamily !== undefined)
      this.bodyFontFamily = styles.bodyFontFamily;
    if (styles.mdTitleFontSize !== undefined)
      this.mdTitleFontSize = styles.mdTitleFontSize;
    if (styles.smallTitleFontSize !== undefined)
      this.smallTitleFontSize = styles.smallTitleFontSize;
    if (styles.bodyFontSize !== undefined)
      this.bodyFontSize = styles.bodyFontSize;
    if (styles.mdTitleColor !== undefined)
      this.mdTitleColor = styles.mdTitleColor;
    if (styles.smallTitleColor !== undefined)
      this.smallTitleColor = styles.smallTitleColor;
    if (styles.bodyColor !== undefined)
      this.bodyColor = styles.bodyColor;
    if (styles.mdTitleAlignment !== undefined)
      this.mdTitleAlignment = styles.mdTitleAlignment;
    if (styles.smallTitleAlignment !== undefined)
      this.smallTitleAlignment = styles.smallTitleAlignment;
    if (styles.bodyIndent !== undefined)
      this.bodyIndent = styles.bodyIndent;
    if (styles.lineHeight !== undefined)
      this.lineHeight = styles.lineHeight;
    return this.renderMarkdown();
  }

  getHtml() {
    return this.renderedHtml;
  }
}

export const createFullMd2Html = (context: any) => {
  return new Md2Html({
    context,
    plugins: [
      new EChartsPlugin({
        // 可以传入插件配置选项
        debug: true,
      }),
      new CodePlugin({
        // 启用自动语言检测
        autoDetect: true,
        // 支持的语言（空数组表示支持所有语言）
        languages: [],
        // 忽略未识别的语言
        ignoreIllegals: true,
      }),
      new ThinkPlugin(),
      new LatexPlugin(),
    ],
  });
};
