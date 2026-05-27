export interface PluginOptions {
  name?: string;
  version?: string;
  [key: string]: any;
}

/**
 * 基础插件类，定义插件的标准接口和生命周期
 */
export class BasePlugin {
  name: string;
  version: string;
  options: PluginOptions;
  context: any; // 组件上下文

  constructor(options: PluginOptions = {}) {
    this.name = options.name || 'BasePlugin';
    this.version = options.version || '1.0.0';
    this.options = options;
    this.context = null;
  }

  /**
   * 插件初始化
   * @param {Object} context - 组件上下文
   */
  install(context: any): void {
    this.context = context;
    this.onInstall();
  }

  /**
   * 插件安装时调用
   */
  onInstall(): void {
    // 子类可重写此方法
  }

  /**
   * 组件挂载前调用
   */
  beforeMount(): void {
    // 子类可重写此方法
  }

  /**
   * 组件挂载后调用
   */
  mounted(): void {
    // 子类可重写此方法
  }

  /**
   * Markdown 渲染前调用
   * @param {string} markdown - 原始 Markdown 内容
   * @returns {string} 处理后的 Markdown 内容
   */
  beforeRender(markdown: string): string {
    return markdown;
  }

  /**
   * HTML 渲染后调用
   * @param {string} html - 渲染后的 HTML 内容
   * @param {any} extra - 额外参数
   * @returns {string} 处理后的 HTML 内容
   */
  afterRender(html: string, extra?: any): string {
    return html;
  }

  /**
   * DOM 更新后调用
   * @param {HTMLElement} el - 容器元素
   */
  afterDomUpdate(el?: HTMLElement): void {
    // 子类可重写此方法
  }

  /**
   * 导出前调用
   * @param {string} html - 要导出的 HTML 内容
   * @returns {string} 处理后的 HTML 内容
   */
  beforeExport(html: string): string | Promise<string> {
    return html;
  }

  /**
   * 组件销毁前调用
   */
  beforeDestroy(): void {
    // 子类可重写此方法
  }

  /**
   * 插件卸载
   */
  uninstall(): void {
    this.beforeDestroy();
    this.context = null;
  }

  /**
   * 获取插件信息
   */
  getInfo(): { name: string; version: string; options: PluginOptions } {
    return {
      name: this.name,
      version: this.version,
      options: this.options,
    };
  }
}
