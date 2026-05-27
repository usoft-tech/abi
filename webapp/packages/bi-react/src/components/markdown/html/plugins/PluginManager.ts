import { BasePlugin } from './BasePlugin';

/**
 * 插件管理器，负责插件的注册、生命周期管理和调用
 */
export class PluginManager {
  plugins: Map<string, BasePlugin>;
  context: any;

  constructor() {
    this.plugins = new Map(); // 存储已注册的插件
    this.context = null; // 组件上下文
  }

  /**
   * 设置组件上下文
   * @param {Object} context - 组件上下文
   */
  setContext(context: any): void {
    this.context = context;
  }

  /**
   * 注册插件
   * @param {BasePlugin} plugin - 插件实例
   */
  register(plugin: BasePlugin): void {
    if (!plugin || typeof plugin.install !== 'function') {
      throw new Error('插件必须继承自 BasePlugin 并实现 install 方法');
    }

    const pluginName = plugin.name;
    if (this.plugins.has(pluginName)) {
      console.warn(`插件 ${pluginName} 已存在，将被覆盖`);
    }

    this.plugins.set(pluginName, plugin);

    // 如果上下文已设置，立即安装插件
    if (this.context) {
      plugin.install(this.context);
    }
  }

  /**
   * 卸载插件
   * @param {string} pluginName - 插件名称
   */
  unregister(pluginName: string): void {
    const plugin = this.plugins.get(pluginName);
    if (plugin) {
      plugin.uninstall();
      this.plugins.delete(pluginName);
    }
  }

  /**
   * 获取插件
   * @param {string} pluginName - 插件名称
   * @returns {BasePlugin|null}
   */
  getPlugin(pluginName: string): BasePlugin | null {
    return this.plugins.get(pluginName) || null;
  }

  /**
   * 获取所有插件
   * @returns {Array<BasePlugin>}
   */
  getAllPlugins(): BasePlugin[] {
    return Array.from(this.plugins.values());
  }

  /**
   * 清除所有插件
   */
  clear(): void {
    this.plugins.forEach((plugin) => {
      plugin.uninstall();
    });
    this.plugins.clear();
  }

  /**
   * 安装所有插件
   */
  installAll(): void {
    if (!this.context) {
      throw new Error('必须先设置组件上下文');
    }

    this.plugins.forEach((plugin) => {
      plugin.install(this.context);
    });
  }

  /**
   * 调用所有插件的生命周期方法
   * @param {string} hookName - 生命周期方法名
   * @param {...any} args - 传递给生命周期方法的参数
   * @returns {any} 最后一个插件的返回值
   */
  callHook(hookName: keyof BasePlugin, ...args: any[]): any {
    let result = args[0]; // 默认返回第一个参数

    this.plugins.forEach((plugin) => {
      if (typeof plugin[hookName] === 'function') {
        try {
          // @ts-ignore
          const pluginResult = plugin[hookName](...args);
          // 如果插件返回了值，更新结果
          if (pluginResult !== undefined) {
            result = pluginResult;
            // 更新参数，以便下一个插件使用
            args[0] = result;
          }
        } catch (e) {
          console.error(`Plugin hook error (${plugin.name}.${hookName}):`, e);
        }
      }
    });

    return result;
  }
}
