import React, {
  useState,
  useEffect,
  useRef,
  useCallback,
  useMemo,
} from "react";
import MarkdownIt from "markdown-it";
// @ts-ignore
import markdownItAbbr from "markdown-it-abbr/dist/markdown-it-abbr.min.js";
// @ts-ignore
import markdownDefList from "markdown-it-deflist";
// @ts-ignore
import { full as markdownItEmoji } from "markdown-it-emoji";
// @ts-ignore
import markdownItFootnote from "markdown-it-footnote";
// @ts-ignore
import markdownItIns from "markdown-it-ins";
// @ts-ignore
import markdownItMark from "markdown-it-mark";
// @ts-ignore
import markdownItSub from "markdown-it-sub";
// @ts-ignore
import markdownItSup from "markdown-it-sup";
// @ts-ignore
import markdownItTaskLists from "markdown-it-task-lists";
// @ts-ignore
import htmlDocx from "html-docx-js/dist/html-docx";
import { Popover, Button, Select, Radio, Slider, message } from "antd";
import {
  UnorderedListOutlined,
  SettingOutlined,
  MoreOutlined,
  AlignLeftOutlined,
  AlignCenterOutlined,
  AlignRightOutlined,
  MenuUnfoldOutlined,
  ColumnHeightOutlined,
  FileWordOutlined,
} from "@ant-design/icons";

import { PluginManager } from "./plugins/PluginManager";
import { BasePlugin } from "./plugins/BasePlugin";
// @ts-ignore
import styled from "styled-components";

const Wrapper = styled.div`
  position: relative;
  width: 100%;
  height: 100%;

  .md-container {
    width: 100%;
    height: 100%;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 20px;
    box-sizing: border-box;
  }

  .floating-btns {
    z-index: 100;
    gap: 10px;
    display: flex;
    position: fixed;
    bottom: 20px;
    right: 20px;
    flex-direction: column;
  }

  .floating-export-btn {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background-color: #1890ff;
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    transition: all 0.3s;
  }

  .floating-export-btn:hover {
    background-color: #40a9ff;
    transform: scale(1.1);
  }

  /* Anchor Popover Styles */
  .anchor-popover .ant-popover-inner-content {
    padding: 0;
  }

  /* Style Settings Popover */
  .control-section {
    margin-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;
    padding-bottom: 12px;
  }

  .control-section:last-child {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
  }

  .control-section h4 {
    margin: 0 0 8px 0;
    font-size: 14px;
    color: #333;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .control-group {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
  }

  /* Markdown Content Styles */
  .md-content {
    font-family:
      "FangSong", serif; /* Default, will be overridden by inline styles */
  }

  .md-content img {
    max-width: 100%;
  }

  /* Fix for Ant Design Popover being global */
  .anchor-popover {
    z-index: 1050;
  }
`;

const AnchorList = styled.ul`
  list-style: none;
  padding: 0;
  margin: 0;
  max-height: 400px;
  overflow-y: auto;
  width: 250px;

  .anchor-item {
    padding: 4px 16px;
    cursor: pointer;
    transition: background-color 0.3s;
  }

  .anchor-item:hover {
    background-color: #f5f5f5;
  }

  .anchor-item.h1 {
    font-weight: bold;
  }

  .anchor-item.h2 {
    padding-left: 24px;
  }

  .anchor-item.h3 {
    padding-left: 32px;
  }

  .anchor-link {
    color: #333;
    text-decoration: none;
    display: block;
    width: 100%;
  }

  .anchor-link:hover {
    color: #1890ff;
  }
`;

const { Option } = Select;

export interface BaseMdViewerProps {
  markdown?: string;
  plugins?: BasePlugin[];
  isStyleModule?: boolean;
  mathExportScale?: number;
  scrollEl?: HTMLElement | null;
}

interface AnchorItem {
  id: string;
  text: string;
  level: string;
}

const BaseMdViewer: React.FC<BaseMdViewerProps> = ({
  markdown = "",
  plugins = [],
  isStyleModule = true,
  scrollEl,
}) => {
  const [renderedHtml, setRenderedHtml] = useState<string>("");
  const [anchorList, setAnchorList] = useState<AnchorItem[]>([]);
  const pluginManagerRef = useRef<PluginManager | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const markdownContainerRef = useRef<HTMLDivElement>(null);

  // Style states
  const [mdTitleFontFamily, setMdTitleFontFamily] = useState(
    "'黑体', 'SimHei', sans-serif",
  );
  const [smallTitleFontFamily, setSmallTitleFontFamily] = useState(
    "'黑体', 'SimHei', sans-serif",
  );
  const [bodyFontFamily, setBodyFontFamily] = useState(
    "'仿宋', 'FangSong', serif",
  );
  const [mdTitleFontSize, setMdTitleFontSize] = useState<string>("22");
  const [smallTitleFontSize, setSmallTitleFontSize] = useState<string>("14");
  const [bodyFontSize, setBodyFontSize] = useState<number>(14);
  const [mdTitleColor, setMdTitleColor] = useState("#000000");
  const [smallTitleColor, setSmallTitleColor] = useState("#000000");
  const [bodyColor, setBodyColor] = useState("#333333");
  const [mdTitleAlignment, setMdTitleAlignment] = useState("center");
  const [smallTitleAlignment, setSmallTitleAlignment] = useState("left");
  const [bodyIndent, setBodyIndent] = useState(2);
  const [lineHeight, setLineHeight] = useState(29);

  const md = useMemo(() => {
    const m = new MarkdownIt();
    m.use(markdownItAbbr);
    m.use(markdownDefList);
    m.use(markdownItEmoji);
    m.use(markdownItFootnote);
    m.use(markdownItIns);
    m.use(markdownItMark);
    m.use(markdownItSub);
    m.use(markdownItSup);
    m.use(markdownItTaskLists);
    return m;
  }, []);

  const initializePlugins = useCallback(() => {
    if (pluginManagerRef.current) {
      pluginManagerRef.current.clear();
    }

    const pm = new PluginManager();
    // In React, context is tricky. We pass a proxy object mimicking Vue context if needed
    // or pass the component functions.
    // Here we pass a simplified context.
    const context = {
      $nextTick: (cb: () => void) => setTimeout(cb, 0),
      refs: {
        container: containerRef.current,
        markdownContainer: markdownContainerRef.current,
      },
      // Expose render method if needed
    };
    pm.setContext(context);

    plugins.forEach((plugin) => {
      pm.register(plugin);
    });

    pm.callHook("mounted");
    pluginManagerRef.current = pm;
  }, [plugins]);

  // Effect to initialize plugins when plugins prop changes
  useEffect(() => {
    initializePlugins();
    return () => {
      if (pluginManagerRef.current) {
        pluginManagerRef.current.clear();
        pluginManagerRef.current = null;
      }
    };
  }, [initializePlugins]);

  const renderMarkdown = useCallback(() => {
    if (!pluginManagerRef.current) {
      initializePlugins();
    }

    pluginManagerRef.current?.callHook("beforeRender");

    let html = md.render(markdown);

    // Apply styles
    html = html.replace(/<h([6])>(.*?)<\/h[6]>/g, (_, level, content) => {
      return `<h${level} class="mdTitle" style="font-family: ${mdTitleFontFamily}; font-size: ${mdTitleFontSize}pt; font-weight: bold; line-height: ${lineHeight}px; text-align: ${mdTitleAlignment}; color: ${mdTitleColor};">${content}</h${level}>`;
    });

    html = html.replace(/<p>(.*?)<\/p>/g, (_, content) => {
      return `<p style="font-family: ${bodyFontFamily}; font-size: ${bodyFontSize}pt; line-height: ${lineHeight}px; text-indent: ${bodyIndent}em; color: ${bodyColor};">${content}</p>`;
    });

    html = html.replace(/<h([1-3])>(.*?)<\/h[1-3]>/g, (_, level, content) => {
      return `<h${level} style="font-family: ${smallTitleFontFamily}; font-size: ${smallTitleFontSize}pt; font-weight: bold; line-height: ${lineHeight}px; text-align: ${(level + "") === '1' ? "center" : smallTitleAlignment}; color: ${smallTitleColor};">${content}</h${level}>`;
    });

    const extra = {};
    html =
      pluginManagerRef.current?.callHook("afterRender", html, extra) || html;
    setRenderedHtml(html);
  }, [
    md,
    markdown,
    mdTitleFontFamily,
    mdTitleFontSize,
    lineHeight,
    mdTitleAlignment,
    mdTitleColor,
    bodyFontFamily,
    bodyFontSize,
    bodyIndent,
    bodyColor,
    smallTitleFontFamily,
    smallTitleFontSize,
    smallTitleAlignment,
    smallTitleColor,
    initializePlugins,
  ]);

  // Effect to render markdown when dependencies change
  useEffect(() => {
    // Debounce or just run? Vue had $nextTick.
    // We can use a small timeout or just run it.
    const timer = setTimeout(() => {
      renderMarkdown();
    }, 0);
    return () => clearTimeout(timer);
  }, [renderMarkdown]);

  // Effect for afterDomUpdate
  useEffect(() => {
    if (containerRef.current && pluginManagerRef.current) {
      pluginManagerRef.current.callHook("afterDomUpdate", containerRef.current);
    }
    // Parse anchors from rendered HTML
    if (containerRef.current) {
      const headers = containerRef.current.querySelectorAll("h1, h2, h3");
      const list: AnchorItem[] = Array.from(headers).map((header, index) => {
        if (!header.id) {
          header.id = `header-${index}`;
        }
        return {
          id: header.id,
          text: header.textContent || "",
          level: header.tagName.toLowerCase(),
        };
      });
      setAnchorList(list);
    }
  }, [renderedHtml]);

  const scrollToAnchor = (id: string) => {
    const containerEl = markdownContainerRef.current;
    const target =
      containerRef.current && containerRef.current.querySelector(`#${id}`);
    if (!containerEl || !target) {
      return;
    }
    const containerTop = containerEl.getBoundingClientRect().top;
    const targetTop = target.getBoundingClientRect().top;
    const currentScroll = containerEl.scrollTop;
    const offset = targetTop - containerTop + currentScroll - 8;
    (scrollEl || containerEl).scrollTo({ top: offset, behavior: "smooth" });
  };

  const onExport = async () => {
    try {
      let html = renderedHtml;
      // Plugin hook for export
      if (pluginManagerRef.current) {
        html =
          (await pluginManagerRef.current.callHook("beforeExport", html)) ||
          html;
      }
      
      function flattenListParagraphs(src: string): string {
        let out = src;
        out = out.replace(
          /<li>\s*<p\b[^>]*>([\s\S]*?)<\/p>\s*<\/li>/g,
          (_m, txt) => `<li>${txt}</li>`,
        );
        return out;
      }
      html = flattenListParagraphs(html);
      
      const mdTitleFontFamily = "'黑体', 'SimHei', sans-serif";
      const smallTitleFontFamily = "'黑体', 'SimHei', sans-serif";
      const bodyFontFamily = "'仿宋', 'FangSong', serif";
      const mdTitleFontSize = '22';
      const smallTitleFontSize = '14';
      const bodyFontSize = 14;
      const mdTitleColor = "#000000";
      const smallTitleColor = "#000000";
      const bodyColor = "#333333";
      const mdTitleAlignment = "center";
      const smallTitleAlignment = "left";
      const bodyIndent = 2;
      const lineHeight = 29;
      const style = `
        <style type="text/css">
        @page {
          size: A4;
          margin:69.85pt 72.0pt 69.85pt 72.0pt;
          mso-page-border-surround-header:no;
          mso-page-border-surround-footer:no;
        }
        /* 表格默认样式 */
        table {
          width: 100%;
          border-collapse: collapse;
          border: 1px solid #ddd;
        }

        table th,
        table td {
          padding: 8px 12px;
          text-align: left;
          border: 1px solid #ddd;
        }

        table th {
          background-color: #f5f5f5;
          font-weight: bold;
        }

        table tr:nth-child(even) {
          background-color: #f9f9f9;
        }

        /* 图片默认样式 */
        img {
          max-width: 100%;
          height: auto;
          display: block;
        }

        /* 代码块样式 */
        pre {
          background: #f8f9fa;
          border: 1px solid #e9ecef;
          border-radius: 4px;
          padding: 16px;
          overflow-x: auto;
        }

        code {
          padding: 2px 4px;
          border-radius: 3px;
          font-family: "Courier New", monospace;
        }

        /* 标题样式 */
        h1,
        h2,
        h3,
        h4,
        h5{
          font-weight: 600;
          line-height: 1.25;
        }

        h1 {
          // font-size: 2em;
          border-bottom: 1px solid #eaecef;
          padding-bottom: 8px;
          text-align: center !important;
        }

        h2 {
          // font-size: 1.5em;
          border-bottom: 1px solid #eaecef;
          padding-bottom: 8px;
        }

        /* 段落样式 */
        p {
        }

        /* 列表样式 */
        ul,
        ol {
          padding-left: 32px;
        }

        li {
          display: list-item;
          list-style-position: outside;
          margin: 0;
        }

        /* 确保列表项在导出时序号和内容在同一行 */
        ul > li {
          list-style-type: disc;
        }

        ol > li {
          list-style-type: decimal;
        }

        ul > li p {
          display: inline;
          margin: 0;
        }

        /* 针对导出时的特殊处理 */
        ul li::marker,
        ol li::marker {
          display: inline;
        }

        /* 引用样式 */
        blockquote {
          padding: 0 16px;
          color: #6a737d;
          border-left: 4px solid #dfe2e5;
        }

        /* 分割线样式 */
        hr {
          height: 0;
          border-top: none;
          border-left: none;
          border-right: none;
          border-bottom: 1px solid #e1e4e8;
        }
          
        h6 {
          font-family: ${mdTitleFontFamily};
          font-size: ${mdTitleFontSize}pt ;/* 二号字 */
          font-weight: normal;
          line-height: ${lineHeight}px; /* 固定行距22磅 */
          text-align: ${mdTitleAlignment};
          margin: 32px 0 16px 0;
          padding: 0;
          color: ${mdTitleColor};
        }
          
         ul, ol  {
          font-family: ${bodyFontFamily};
          font-size: ${bodyFontSize}pt; /* 四号字 */
          line-height: ${lineHeight}px; /* 固定行距22磅 */
        }

        /* 二级标题：黑体四号字 */
        h1, h2, h3 {
          font-family: ${smallTitleFontFamily};
          font-size:  ${smallTitleFontSize}pt ;/* 四号字 */
          font-weight: normal;
          line-height: ${lineHeight}px; /* 固定行距22磅 */
          margin: 20px 0 12px 0;
          padding: 0;
          color: ${smallTitleColor};
          text-indent: 0; /* 标题不缩进 */
        }

        /* 三级标题：黑体四号字 */
        h4, h5 {
          font-family: ${smallTitleFontFamily};
          font-size: ${smallTitleFontSize}pt;/* 四号字 */
          font-weight: normal;
          line-height: ${lineHeight}px; /* 固定行距22磅 */
          margin: 16px 0 12px 0;
          padding: 0;
          text-indent: 0; /* 标题不缩进 */
        }
        /* 正文：仿宋四号字，首行缩进2个字 (约56px) */
        p {
          font-family: ${bodyFontFamily};
          font-size: ${bodyFontSize}pt; /* 四号字 */
          line-height: ${lineHeight}px; /* 固定行距22磅 */
          text-indent: ${bodyIndent}em;
          margin: 0 0 16px 0;
          padding: 0;
          color: ${bodyColor};
        }
        table {
          font-family: ${bodyFontFamily};
          font-size: 12pt !important; /* 小四号字 */
          line-height: ${lineHeight}px; /* 固定行距22磅 */
        }
        li p {
          text-indent: 0 !important; /* 列表项不缩进 */
          margin: 0;
        }
        </style>
        `
      
      const content = `<!DOCTYPE html><html><head><meta charset="utf-8">${style}</head><body>${html}</body></html>`;
      const converted = htmlDocx.asBlob(content);
      const url = window.URL.createObjectURL(converted);
      const link = document.createElement("a");
      link.href = url;
      link.download = "document.docx";
      link.click();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error("Export failed:", error);
      message.error("导出失败");
    }
  };

  return (
    <Wrapper className="md-viewer">
      <div className="floating-btns">
        <Popover
          placement="topRight"
          title="目录导航"
          trigger="click"
          classNames={{ root: "anchor-popover" }}
          content={
            <AnchorList className="anchor-list">
              {anchorList.map((item) => (
                <li key={item.id} className={`anchor-item ${item.level}`}>
                  <a
                    className="anchor-link"
                    onClick={(e) => {
                      e.preventDefault();
                      scrollToAnchor(item.id);
                    }}
                    href={`#${item.id}`}
                  >
                    {item.text}
                  </a>
                </li>
              ))}
            </AnchorList>
          }
        >
          <button
            className="floating-export-btn"
            title="目录导航"
            style={{ padding: "10px", fontSize: "24px" }}
          >
            <UnorderedListOutlined
              style={{ fontSize: "24px", color: "#fff" }}
            />
          </button>
        </Popover>

        {isStyleModule && (
          <Popover
            placement="topRight"
            title="样式设置"
            trigger="click"
            classNames={{ root: "anchor-popover" }}
            content={
              <div style={{ width: 300 }}>
                {/* mdTitle样式控制 */}
                <div className="control-section">
                  <h4>
                    <MoreOutlined /> 主标题
                  </h4>
                  <div className="control-group">
                    <Select
                      size="small"
                      value={mdTitleFontFamily}
                      onChange={setMdTitleFontFamily}
                      style={{ width: 70 }}
                    >
                      <Option value="'黑体', 'SimHei', sans-serif">黑体</Option>
                      <Option value="'宋体', 'SimSun', serif">宋体</Option>
                      <Option value="'仿宋', 'FangSong', serif">仿宋</Option>
                      <Option value="'楷体', 'KaiTi', serif">楷体</Option>
                      <Option value="Arial, sans-serif">Arial</Option>
                    </Select>
                    <Select
                      size="small"
                      value={mdTitleFontSize}
                      onChange={setMdTitleFontSize}
                      style={{ width: 70 }}
                    >
                      {[12, 14, 16, 20, 22, 24, 32, 48].map((size) => (
                        <Option key={size} value={String(size)}>
                          {size}pt
                        </Option>
                      ))}
                    </Select>
                    <input
                      type="color"
                      value={mdTitleColor}
                      onChange={(e) => setMdTitleColor(e.target.value)}
                    />
                    <Radio.Group
                      size="small"
                      value={mdTitleAlignment}
                      onChange={(e) => setMdTitleAlignment(e.target.value)}
                      buttonStyle="solid"
                    >
                      <Radio.Button value="left">
                        <AlignLeftOutlined />
                      </Radio.Button>
                      <Radio.Button value="center">
                        <AlignCenterOutlined />
                      </Radio.Button>
                      <Radio.Button value="right">
                        <AlignRightOutlined />
                      </Radio.Button>
                    </Radio.Group>
                  </div>
                </div>

                {/* 小标题控制 */}
                <div className="control-section">
                  <h4>
                    <MoreOutlined /> 小标题
                  </h4>
                  <div className="control-group">
                    <Select
                      size="small"
                      value={smallTitleFontFamily}
                      onChange={setSmallTitleFontFamily}
                      style={{ width: 70 }}
                    >
                      <Option value="'黑体', 'SimHei', sans-serif">黑体</Option>
                      <Option value="'宋体', 'SimSun', serif">宋体</Option>
                      <Option value="'仿宋', 'FangSong', serif">仿宋</Option>
                      <Option value="'楷体', 'KaiTi', serif">楷体</Option>
                      <Option value="Arial, sans-serif">Arial</Option>
                    </Select>
                    <Select
                      size="small"
                      value={smallTitleFontSize}
                      onChange={setSmallTitleFontSize}
                      style={{ width: 70 }}
                    >
                      {[12, 14, 16, 20, 22, 24, 32, 48].map((size) => (
                        <Option key={size} value={String(size)}>
                          {size}pt
                        </Option>
                      ))}
                    </Select>
                    <input
                      type="color"
                      value={smallTitleColor}
                      onChange={(e) => setSmallTitleColor(e.target.value)}
                    />
                    <Radio.Group
                      size="small"
                      value={smallTitleAlignment}
                      onChange={(e) => setSmallTitleAlignment(e.target.value)}
                      buttonStyle="solid"
                    >
                      <Radio.Button value="left">
                        <AlignLeftOutlined />
                      </Radio.Button>
                      <Radio.Button value="center">
                        <AlignCenterOutlined />
                      </Radio.Button>
                      <Radio.Button value="right">
                        <AlignRightOutlined />
                      </Radio.Button>
                    </Radio.Group>
                  </div>
                </div>

                {/* 正文样式控制 */}
                <div className="control-section">
                  <h4>
                    <MoreOutlined /> 正文设置
                  </h4>
                  <div className="control-group">
                    <Select
                      size="small"
                      value={bodyFontFamily}
                      onChange={setBodyFontFamily}
                      style={{ width: 70 }}
                    >
                      <Option value="'仿宋', 'FangSong', serif">仿宋</Option>
                      <Option value="'宋体', 'SimSun', serif">宋体</Option>
                      <Option value="'黑体', 'SimHei', sans-serif">黑体</Option>
                      <Option value="'楷体', 'KaiTi', serif">楷体</Option>
                      <Option value="Arial, sans-serif">Arial</Option>
                    </Select>
                    <Select
                      size="small"
                      value={bodyFontSize}
                      onChange={(v) => setBodyFontSize(Number(v))}
                      style={{ width: 70 }}
                    >
                      {[12, 14, 16, 20, 22, 24, 32, 48].map((size) => (
                        <Option key={size} value={size}>
                          {size}pt
                        </Option>
                      ))}
                    </Select>
                    <input
                      type="color"
                      value={bodyColor}
                      onChange={(e) => setBodyColor(e.target.value)}
                    />
                    <Popover
                      title="首行缩进（em）"
                      trigger="hover"
                      content={
                        <Slider
                          min={0}
                          max={4}
                          step={0.5}
                          value={bodyIndent}
                          onChange={setBodyIndent}
                          style={{ width: 100 }}
                        />
                      }
                    >
                      <Button size="small">
                        <MenuUnfoldOutlined />
                      </Button>
                    </Popover>
                    <Popover
                      title="行间距（px）"
                      trigger="hover"
                      content={
                        <Slider
                          min={20}
                          max={40}
                          step={1}
                          value={lineHeight}
                          onChange={setLineHeight}
                          style={{ width: 100 }}
                        />
                      }
                    >
                      <Button size="small">
                        <ColumnHeightOutlined />
                      </Button>
                    </Popover>
                  </div>
                </div>
              </div>
            }
          >
            <button
              className="floating-export-btn"
              title="样式设置"
              style={{ padding: "10px" }}
            >
              <SettingOutlined style={{ fontSize: "24px", color: "#fff" }} />
            </button>
          </Popover>
        )}

        <button
          onClick={onExport}
          className="floating-export-btn"
          title="导出为Word文档"
          style={{ padding: "10px" }}
        >
          <FileWordOutlined style={{ fontSize: "24px", color: "#fff" }} />
        </button>
      </div>

      <div className="md-container" ref={markdownContainerRef}>
        <div
          dangerouslySetInnerHTML={{ __html: renderedHtml }}
          className="md-content"
          ref={containerRef}
        />
      </div>
    </Wrapper>
  );
};

export default BaseMdViewer;
