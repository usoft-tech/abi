import React, { useMemo } from 'react';
import BaseMdViewer from './Base';
import {
  CodePlugin,
  EChartsPlugin,
  ThinkPlugin,
  LatexPlugin,
  BasePlugin,
} from './plugins';

interface FullMdViewerProps {
  markdown?: string;
  plugins?: BasePlugin[];
  isStyleModule?: boolean;
  scrollEl?: HTMLElement | null;
}

const FullMdViewer: React.FC<FullMdViewerProps> = ({
  markdown = '',
  plugins = [],
  isStyleModule = true,
  scrollEl,
}) => {
  const defaultPlugins = useMemo(() => {
    return [
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
    ];
  }, []);

  const mergedPlugins = useMemo(() => {
    return [...defaultPlugins, ...(plugins ?? [])];
  }, [defaultPlugins, plugins]);

  return (
    <BaseMdViewer
      markdown={markdown}
      isStyleModule={isStyleModule}
      scrollEl={scrollEl}
      plugins={mergedPlugins}
    />
  );
};

export default FullMdViewer;
