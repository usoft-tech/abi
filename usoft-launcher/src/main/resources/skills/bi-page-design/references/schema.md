# PageSchema 字段说明：
  - info: 对象，包含页面基本信息（如 name、description）。
  - items: 数组，顶层页面元素列表，每个元素为一个 SchemaItem。

# SchemaItem 字段说明：
  - id: 字符串，唯一标识。
  - type: 组件类型。
  - name: 名称。
  - description: 组件相关数据的描述，可用于生成数据查询语句。
  - props: 组件属性对象。
  - cascadeIds: 字符串数组，联动 ID 列表。
  - children: 组件子元素，数组或 map（key 为插槽名，value 为元素列表），仅 b-container 支持。
  - datasource: 组件数据源定义（source、datasourceId、scriptId、custom）。

# 组件字段说明：
  - b-icon：
    props:
      - type: "font-awesome" 或 "icon-font"
      - icon: 图标名称，根据 type 取值不同，对应 font-awesome 或 icon-font 图标库中的图标名称。
      - classNames: 样式类名数组（tailwindcss 类）
  - b-container（支持 tailwindcss 的 class）：
    props:
      - htmlTag: HTML 标签（如 "div"）
      - classNames: 样式类名数组（tailwindcss 类）
    children: 数组或 map（按插槽组织）
  - b-text：
    props:
      - text: 文本内容
      - classNames: 样式类名数组（tailwindcss 类）
  - b-html：
    datasource:
      - source: "custom"
      - custom: 字符串（JSON 形式数据）
    props:
      - template: 字符串（支持 {{var}} 模板插值）
  - b-echarts：
    datasource:
      - source: "custom"
      - custom: 字符串（JSON 数组数据）
    props:
      - script: JavaScript 代码，接收数据 data，代码内部禁止再次定义变量data，返回 ECharts option 对象
