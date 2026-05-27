import { defineConfig } from '@rsbuild/core';
import { pluginReact } from '@rsbuild/plugin-react';
import { pluginStyledComponents } from '@rsbuild/plugin-styled-components';
import { pluginNodePolyfill } from '@rsbuild/plugin-node-polyfill';
import config from './src/config'

export default defineConfig({
  plugins: [
    pluginReact(),
    pluginStyledComponents(),
    pluginNodePolyfill(),
  ],
  html: {
    title: config.name,
    template: "src/index.html",
    templateParameters: {
      title: config.name,
      basePath: config.basePath,
    },
  },
  tools: {
    rspack: {
      node: {
        __dirname: 'mock',
      },
    },
  },
  server: {
    base: config.basePath,
    proxy: {
      '/api': {
        target: process.env.PROXY_TARGET || 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api',
        },
      },
      '/jgszbi-api/api': {
        target: process.env.PROXY_TARGET || 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/jgszbi-api/api': '/jgszbi-api/api',
        },
      },
    },
  },
});
