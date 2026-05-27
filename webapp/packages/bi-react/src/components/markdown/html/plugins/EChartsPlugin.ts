import { BasePlugin, PluginOptions } from './BasePlugin';
import * as echarts from 'echarts';
// @ts-ignore
import domtoimage from 'dom-to-image';
import { uuid } from '@/utils';
// @ts-ignore

interface EChartsPluginOptions extends PluginOptions {
  debug?: boolean;
}

/**
 * ECharts 插件，处理 Markdown 中的 ECharts 图表
 */
export class EChartsPlugin extends BasePlugin {
  echartsInstances: echarts.ECharts[];
  resizeHandler: (() => void) | null;

  constructor(options: EChartsPluginOptions = {}) {
    super({
      name: 'EChartsPlugin',
      version: '1.0.0',
      ...options,
    });

    this.echartsInstances = []; // 存储 ECharts 实例
    this.resizeHandler = null; // 存储 resize 事件处理器
  }

  /**
   * 插件安装时调用
   */
  onInstall(): void {}

  /**
   * 组件挂载后调用
   */
  mounted(): void {
    // 统一的 resize 处理器
    this.resizeHandler = this.debounce(() => {
      this.echartsInstances.forEach((chart) => chart.resize());
    }, 300);
    window.addEventListener('resize', this.resizeHandler!);
  }

  /**
   * 组件渲染前调用
   */
  beforeRender(markdown: string): string {
    // 清理旧实例
    this.cleanup();
    return ""
  }

  /**
   * HTML 渲染后调用，处理 ECharts 代码块
   * @param {string} html - 渲染后的 HTML 内容
   * @param {object} extra - 额外参数，包含图表配置
   * @returns {string} 处理后的 HTML 内容
   */
  afterRender(html: string, extra?: any): string {
    // 查找并替换渲染后的 echarts HTML 代码块
    // 匹配 <pre><code class="language-echarts">...</code></pre> 结构
    const echartsRegex =
      /<pre><code class="language-echarts">([\s\S]*?)<\/code><\/pre>/g;
    const matches: {
      fullMatch: string;
      configStr: string;
      index: number;
    }[] = [];
    let match;

    // 先收集所有匹配项
    while ((match = echartsRegex.exec(html)) !== null) {
      matches.push({
        fullMatch: match[0],
        configStr: match[1].trim(),
        index: match.index,
      });
    }

    // 从后往前替换，避免索引位置变化影响后续替换
    for (let i = matches.length - 1; i >= 0; i--) {
      const matchInfo = matches[i];
      const chartId = `echarts-chart-${uuid()}`;

      // 替换代码块为图表容器
      const chartContainer = `<div id="${chartId}" class="echarts-chart" style="width: 100%; height: 400px; margin: 20px 0;"></div>`;
      html =
        html.substring(0, matchInfo.index) +
        chartContainer +
        html.substring(matchInfo.index + matchInfo.fullMatch.length);

      if (extra) {
        extra.echarts = extra.echarts || [];
        extra.echarts.push({
          id: chartId,
          config: matchInfo.configStr,
        });
      }
      // 存储图表配置，稍后渲染
      // React 中 context 可能不同，这里假设 context 有类似机制或直接使用 setTimeout
      if (this.context && typeof this.context.$nextTick === 'function') {
        this.context.$nextTick(() => {
          setTimeout(() => {
            this.renderEChart(chartId, matchInfo.configStr);
          }, 100);
        });
      } else {
        setTimeout(() => {
          this.renderEChart(chartId, matchInfo.configStr);
        }, 100);
      }
    }

    return html;
  }

  /**
   * DOM 更新后调用
   */
  afterDomUpdate(): void {
    // 可以在这里处理需要在 DOM 更新后执行的逻辑
  }

  /**
   * 导出前调用，将图表转换为图片
   * @param {string} html - 要导出的 HTML 内容
   * @returns {Promise<string>} 处理后的 HTML 内容
   */
  // @ts-ignore
  async beforeExport(html: string): Promise<string> {
    return await this.createExportContent(html);
  }

  /**
   * 组件销毁前调用
   */
  beforeDestroy(): void {
    this.cleanup();
  }

  /**
   * 防抖函数
   * @param {Function} func - 要防抖的函数
   * @param {number} wait - 等待时间
   * @returns {Function} 防抖后的函数
   */
  debounce(func: Function, wait: number): () => void {
    let timeout: any;
    return (...args: any[]) => {
      const later = () => {
        clearTimeout(timeout);
        func(...args);
      };
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
    };
  }

  /**
   * 统一清理资源
   */
  cleanup(): void {
    this.cleanupCharts();
    if (this.resizeHandler) {
      window.removeEventListener('resize', this.resizeHandler);
      this.resizeHandler = null;
    }
  }

  /**
   * 清理图表实例
   */
  cleanupCharts(): void {
    this.echartsInstances.forEach((instance) => instance.dispose());
    this.echartsInstances = [];
  }

  /**
   * 安全解析 ECharts 配置，支持包含函数的配置
   * @param {string} configStr - 配置字符串
   * @returns {Object} 解析后的配置对象
   */
  parseEChartsConfig(configStr: string): any {
    try {
      // 首先尝试 JSON.parse
      return JSON.parse(configStr);
    } catch (jsonError) {
      try {
        // 如果 JSON.parse 失败，使用 Function 构造器安全解析
        // 创建一个受限的执行环境
        const safeEval = new Function(
          'echarts',
          'console',
          'Math',
          'Date',
          'Array',
          'Object',
          'String',
          'Number',
          'Boolean',
          `
          "use strict";
          // 禁用一些危险的全局对象
          const window = undefined;
          const document = undefined;
          const global = undefined;
          const process = undefined;
          const require = undefined;
          const module = undefined;
          const exports = undefined;
          
          // 返回配置对象
          return (${configStr});
          `
        );

        return safeEval(
          echarts,
          console,
          Math,
          Date,
          Array,
          Object,
          String,
          Number,
          Boolean
        );
      } catch (evalError: any) {
        console.error('ECharts 配置解析失败:', evalError);
        throw new Error(`配置解析失败: ${evalError.message}`);
      }
    }
  }

  /**
   * 渲染单个 ECharts 图表
   * @param {string} chartId - 图表容器ID
   * @param {string} configStr - 图表配置字符串
   */
  renderEChart(chartId: string, configStr: string): void {
    try {
      // 解码 HTML 实体
      const decodedConfigStr = configStr
        .replace(/&lt;/g, '<')
        .replace(/&gt;/g, '>')
        .replace(/&amp;/g, '&')
        .replace(/&quot;/g, '"')
        .replace(/&#39;/g, "'");

      // 使用安全的配置解析方法
      const config = this.parseEChartsConfig(decodedConfigStr);
      const chartDom = document.getElementById(chartId);

      if (chartDom && echarts) {
        const chart = echarts.init(chartDom);
        chart.setOption(config);
        this.echartsInstances.push(chart);
      }
    } catch (error: any) {
      console.error('ECharts 配置解析错误:', error);
      // 如果解析失败，显示错误信息
      const chartDom = document.getElementById(chartId);
      if (chartDom) {
        chartDom.innerHTML = `<div style="color: red; padding: 20px;">ECharts 配置解析错误: ${error.message}</div>`;
      }
    }
  }

  /**
   * 创建包含图表图片的完整导出内容
   * @param {string} html - 原始HTML内容
   * @returns {Promise<string>} 处理后的HTML内容
   */
  async createExportContent(html: string): Promise<string> {
    // 克隆当前渲染的HTML内容
    let exportHtml = html;

    // 获取所有图表容器
    const chartContainers = document.querySelectorAll('.echarts-chart');

    if (chartContainers.length > 0) {
      // 为每个图表生成图片并替换原位置
      const chartPromises = Array.from(chartContainers).map(
        async (chartDom: any, index) => {
          if (chartDom) {
            try {
              // 确保图表已经渲染完成
              await new Promise((resolve) => setTimeout(resolve, 100));

              const imgDataUrl = await domtoimage.toPng(chartDom, {
                quality: 0.9,
                bgcolor: '#ffffff',
                width: chartDom.offsetWidth,
                height: chartDom.offsetHeight,
              });

              // 返回图表ID和对应的图片数据
              return {
                chartId: chartDom.id,
                imgDataUrl: imgDataUrl,
              };
            } catch (error: any) {
              console.error(`生成图表 ${index + 1} 图片失败:`, error);
              return {
                chartId: chartDom.id,
                error: error.message,
              };
            }
          }
          return null;
        }
      );

      const chartResults = await Promise.all(chartPromises);

      // 在HTML中替换图表容器为图片
      chartResults.forEach((result) => {
        if (result && result.chartId) {
          const chartRegex = new RegExp(
            `<div[^>]*id="${result.chartId}"[^>]*class="echarts-chart"[^>]*></div>`,
            'g'
          );

          if (result.imgDataUrl) {
            // 成功生成图片，替换为img标签
            const imgTag = `<img src="${result.imgDataUrl}" style="width: 100%; max-width: 600px; height: auto; display: block; margin: 20px 0;" alt="图表" />`;
            exportHtml = exportHtml.replace(chartRegex, imgTag);
          } else if (result.error) {
            // 生成失败，替换为错误信息
            const errorDiv = `<div style="color: red; margin: 20px 0; padding: 10px; border: 1px solid red;">图表生成失败: ${result.error}</div>`;
            exportHtml = exportHtml.replace(chartRegex, errorDiv);
          }
        }
      });
    }

    return exportHtml;
  }
}
