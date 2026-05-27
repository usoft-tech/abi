import { BasePlugin, PluginOptions } from './BasePlugin';
import '../utils/mathjax';
import 'mathjax/es5/tex-mml-svg';

export class LatexPlugin extends BasePlugin {
  constructor(options: PluginOptions = {}) {
    super({
      name: options.name || 'LatexPlugin',
      version: options.version || '1.0.0',
      ...options,
    });
  }

  /**
   * 计算 1ex 对应的像素值，基于参考元素的字体上下文
   */
  getPxPerEx(refEl: Element | null): number {
    try {
      const probe = document.createElement('span');
      probe.style.position = 'absolute';
      probe.style.visibility = 'hidden';
      probe.style.height = '0';
      probe.style.width = '1ex';
      const attach =
        refEl && refEl.parentElement ? refEl.parentElement : document.body;
      attach.appendChild(probe);
      const rect = probe.getBoundingClientRect();
      probe.remove();
      const px = Math.max(1, Math.round(rect.width));
      return px || 8;
    } catch (_) {
      return 8;
    }
  }

  /**
   * 将指定 SVG 属性中的 ex 单位转换为 px 字符串
   */
  convertExAttrToPx(
    svg: Element | null,
    attr: string,
    pxPerEx: number
  ): string | null {
    try {
      if (!svg) return null;
      const val = svg.getAttribute(attr);
      if (!val || !val.includes('ex')) {
        return null;
      }
      const num = parseFloat(val.replace('ex', ''));
      if (Number.isFinite(num)) {
        const px = Math.max(1, Math.round(num * pxPerEx));
        return px + 'px';
      }
      return val.replace('ex', 'px');
    } catch (_) {
      return null;
    }
  }

  async afterDomUpdate(el?: HTMLElement): Promise<void> {
    /**
     * 在 DOM 更新后，将 MathJax 渲染的 SVG 公式按精确像素尺寸导出为内嵌 SVG 图片。
     * 1. 先 typesetPromise 以确保公式渲染完成。
     * 2. 将 SVG 的 width/height 的 ex 单位转换为 px，保证尺寸稳定。
     * 3. 直接使用 XMLSerializer 序列化生成 data:image/svg+xml，避免 html2canvas 克隆 iframe 查找失败。
     * 4. 同步设置 <img> 的样式与 HTML 属性，提升 Word 识别稳定性。
     */
    // 重新渲染 MathJax 公式
    if (el && window.MathJax) {
      await window.MathJax.typesetPromise([el]);
    }
    
    if (!el) return;

    const containers = el.querySelectorAll('mjx-container');
    for (const container of Array.from(containers)) {
      const display = container.getAttribute('display') === 'true';
      const svg = container.querySelector('svg');
      const pxPerEx = this.getPxPerEx(container);
      const widthPx = this.convertExAttrToPx(svg, 'width', pxPerEx);
      const heightPx = this.convertExAttrToPx(svg, 'height', pxPerEx);
      if (svg) {
        if (widthPx) svg.setAttribute('width', widthPx);
        if (heightPx) svg.setAttribute('height', heightPx);
        const svgStr = new XMLSerializer().serializeToString(svg);
        const dataUrl =
          'data:image/svg+xml;base64,' +
          btoa(unescape(encodeURIComponent(svgStr)));
        const img = document.createElement('img');
        img.src = dataUrl;
        img.style.display = display ? 'block' : 'inline-block';
        img.style.verticalAlign = 'middle';
        // 同时设置行内样式与 HTML 属性，提升 Word 对尺寸的识别稳定性
        if (widthPx) {
            img.style.width = widthPx;
            img.setAttribute('width', String(widthPx));
        }
        if (heightPx) {
            img.style.height = heightPx;
            img.setAttribute('height', String(heightPx));
        }
        img.style.maxWidth = 'none';
        img.style.maxHeight = 'none';
        container.replaceWith(img);
      }
    }
  }
}

export default LatexPlugin;
