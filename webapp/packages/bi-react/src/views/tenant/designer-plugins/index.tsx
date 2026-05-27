import { PluginType } from "bi-sdk-react";
import { IconRender } from "./IconRender";
import { IconProps } from "./IconProps";
import { ContainerRender } from "./ContainerRender";
import { ContainerProps } from "./ContainerProps";

export const IconPlugin: PluginType = {
  group: "基础组件",
  key: "b-icon",
  label: "图标",
  icon: "icon-delete-square",
  component: IconRender,
  formComponent: IconProps,
  defaultOptions: {
    props: { type: "font-awesome", icon: "", classNames: [], customStyle: {} },
  },
  events: [
    {
      handler: "click",
      name: "点击",
    },
  ],
};

export const ContainerPlugin: PluginType = {
  group: "容器组件",
  key: "b-container",
  label: "HTML容器",
  icon: "icon-container",
  component: ContainerRender,
  formComponent: ContainerProps,
  defaultOptions: {
    props: { htmlTag: "div", classNames: [] },
    children: [],
    datasource: {},
  },
  events: [
    {
      handler: "click",
      name: "点击",
    },
  ],
};
