
# 组件片段样例：
- 简易卡片：
{
    "id": "card-container-1",
    "type": "b-container",
    "name": "卡片样式示例一",
    "description": "卡片样式示例一",
    "props": {
    "htmlTag": "div",
    "classNames": [
        "w-full",
        "bg-white",
        "rounded-xl",
        "shadow-card",
        "p-4",
        "card-transition",
        "hover:shadow-card-hover"
    ]
    },
    "children": []
}

- 栅格样式一：
{
    "id": "grid-container-1",
    "type": "b-container",
    "name": "栅格样式示例一",
    "description": "栅格样式示例一：4列分布",
    "props": {
    "htmlTag": "div",
    "classNames": [
        "grid",
        "grid-cols-1",
        "sm:grid-cols-2",
        "lg:grid-cols-4",
        "gap-4",
        "mb-8"
    ]
    },
    "children": []
}

- 栅格样式二：
{
    "id": "grid-container-2",
    "type": "b-container",
    "name": "栅格样式示例二",
    "description": "栅格样式示例二：3列分布",
    "props": {
    "htmlTag": "div",
    "classNames": [
        "grid",
        "grid-cols-1",
        "lg:grid-cols-3",
        "gap-6",
        "mb-8"
    ]
    },
    "children": [{
        "id": "grid-container-2-1",
        "type": "b-container",
        "name": "栅格样式示例2.1",
        "description": "栅格样式示例2.1：占左2列",
        "props": {
        "htmlTag": "div",
        "classNames": ["lg:col-span-2"]
        },
        "children": []
    },
    {
        "id": "grid-container-2-2",
        "type": "b-container",
        "name": "栅格样式示例2.2",
        "description": "栅格样式示例2.2：占右1列",
        "props": {
        "htmlTag": "div",
        "classNames": []
        },
        "children": []
    }
    ]
}

- 总览卡片：
{
    "id": "overview-indicator-root-container",
    "type": "b-container",
    "name": "总览指标顶层容器",
    "description": "总览指标顶层容器",
    "props": {
    "htmlTag": "div",
    "classNames": [
        "bg-white",
        "rounded-xl",
        "shadow-card",
        "p-5",
        "card-transition",
        "hover:shadow-card-hover"
    ]
    },
    "children": [{
        "id": "overview-indicator-container",
        "type": "b-container",
        "name": "总览指标容器",
        "description": "总览指标容器",
        "props": {
        "htmlTag": "div",
        "classNames": [
            "flex",
            "justify-between",
            "items-start"
        ]
        },
        "children": [{
            "id": "overview-indicator-value-container",
            "type": "b-container",
            "name": "总览指标数值容器",
            "description": "总览指标数值容器",
            "props": {
            "htmlTag": "div",
            "classNames": []
            },
            "children": [{
                "id": "overview-indicator-value-label",
                "type": "b-text",
                "name": "总览指标数值标签",
                "description": "总览指标数值标签",
                "props": {
                "text": "最高学历分布",
                "classNames": [
                    "text-sm",
                    "text-neutral",
                    "font-medium"
                ]
                }
            },
            {
                "id": "overview-indicator-value-number",
                "type": "b-html",
                "name": "总览指标数值标签",
                "description": "总览指标数值标签",
                "props": {
                "template": "{{value}}",
                "classNames": [
                    "text-3xl",
                    "md:text-4xl",
                    "font-bold",
                    "text-gray-800"
                ]
                },
                "datasource": {
                "source": "custom",
                "custom": "{\"value\":1284}"
                }
            },
            {
                "id": "overview-indicator-value-desc",
                "type": "b-text",
                "name": "总览指标数值描述",
                "description": "总览指标数值描述",
                "props": {
                "text": "硕士及以上占比",
                "classNames": [
                    "text-xs",
                    "text-neutral",
                    "mt-1"
                ]
                }
            }
            ]
        },
        {
            "id": "overview-indicator-icon-container",
            "type": "b-container",
            "name": "总览指标图标容器",
            "description": "总览指标图标容器",
            "props": {
            "htmlTag": "div",
            "classNames": [
                "w-10",
                "h-10",
                "rounded-lg",
                "bg-tertiary-light",
                "flex",
                "items-center",
                "justify-center",
                "text-tertiary"
            ]
            },
            "children": [{
            "id": "overview-indicator-icon",
            "type": "b-icon",
            "name": "总览指标图标容器",
            "description": "总览指标图标容器",
            "props": {
                "type": "font-awesome",
                "icon": "fas fa-users",
                "classNames": [
                "w-10",
                "h-10",
                "rounded-lg",
                "bg-primary-light",
                "flex",
                "items-center",
                "justify-center",
                "text-primary"
                ]
            },
            "children": []
            }]
        }
        ]
    },
    {
        "id": "sub-item-indicator-container",
        "type": "b-container",
        "name": "分项指标容器",
        "description": "分项指标容器",
        "props": {
        "htmlTag": "div",
        "classNames": [
            "mt-4",
            "grid",
            "grid-cols-2",
            "gap-2",
            "text-xs"
        ]
        },
        "children": [{
            "id": "sub-item-indicator1-container",
            "type": "b-container",
            "name": "分项指标一容器",
            "description": "分项指标一容器",
            "props": {
            "htmlTag": "div",
            "classNames": []
            },
            "children": [{
                "id": "sub-item-indicator1-label",
                "type": "b-text",
                "name": "分项指标一标签",
                "description": "分项指标一标签",
                "props": {
                "text": "男性",
                "classNames": ["text-neutral"]
                }
            },
            {
                "id": "sub-item-indicator1-number",
                "type": "b-html",
                "name": "分项指标一数值",
                "description": "分项指标一数值",
                "props": {
                "template": "{{value}}",
                "classNames": ["font-medium"]
                },
                "datasource": {
                "source": "custom",
                "custom": "{\"value\":756}"
                }
            }
            ]
        },
        {
            "id": "sub-item-indicator2-container",
            "type": "b-container",
            "name": "分项指标二容器",
            "description": "分项指标二容器",
            "props": {
            "htmlTag": "div",
            "classNames": []
            },
            "children": [{
                "id": "sub-item-indicator2-label",
                "type": "b-text",
                "name": "分项指标二标签",
                "description": "分项指标二标签",
                "props": {
                "text": "女性",
                "classNames": ["text-neutral"]
                }
            },
            {
                "id": "sub-item-indicator2-number",
                "type": "b-html",
                "name": "分项指标二数值",
                "description": "分项指标二数值",
                "props": {
                "template": "{{value}}",
                "classNames": ["font-medium"]
                },
                "datasource": {
                "source": "custom",
                "custom": "{\"value\":528}"
                }
            }
            ]
        },
        {
            "id": "sub-item-indicator3-container",
            "type": "b-container",
            "name": "分项指标三容器",
            "description": "分项指标三容器",
            "props": {
            "htmlTag": "div",
            "classNames": []
            },
            "children": [{
                "id": "sub-item-indicator3-label",
                "type": "b-text",
                "name": "分项指标三标签",
                "description": "分项指标三标签",
                "props": {
                "text": "汉族",
                "classNames": ["text-neutral"]
                }
            },
            {
                "id": "sub-item-indicator3-number",
                "type": "b-html",
                "name": "分项指标三数值",
                "description": "分项指标三数值",
                "props": {
                "template": "{{value}}",
                "classNames": ["font-medium"]
                },
                "datasource": {
                "source": "custom",
                "custom": "{\"value\":1192}"
                }
            }
            ]
        },
        {
            "id": "sub-item-indicator4-container",
            "type": "b-container",
            "name": "分项指标四容器",
            "description": "分项指标四容器",
            "props": {
            "htmlTag": "div",
            "classNames": []
            },
            "children": [{
                "id": "sub-item-indicator4-label",
                "type": "b-text",
                "name": "分项指标四标签",
                "description": "分项指标四标签",
                "props": {
                "text": "少数民族",
                "classNames": ["text-neutral"]
                }
            },
            {
                "id": "sub-item-indicator4-number",
                "type": "b-html",
                "name": "分项指标三数值",
                "description": "分项指标三数值",
                "props": {
                "template": "{{value}}",
                "classNames": ["font-medium"]
                },
                "datasource": {
                "source": "custom",
                "custom": "{\"value\":92}"
                }
            }
            ]
        }
        ]
    }
    ]
}

# PageSchema 示例：
{
    "summary": "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
    "schema": {
    "info": { "name": "销售看板", "description": "按品牌展示日销量" },
    "items": [
        {
        "id": "container-1",
        "type": "b-container",
        "name": "主容器",
        "description": "页面主体",
        "props": { "htmlTag": "div", "classNames": ["container", "mx-auto", "p-4"] },
        "cascadeIds": [],
        "children": [
            {
            "id": "title-1",
            "type": "b-text",
            "name": "标题",
            "description": "",
            "props": { "text": "销售趋势" }
            },
            {
            "id": "chart-1",
            "type": "b-echarts",
            "name": "折线图",
            "description": "",
            "props": {
                "script": "return { xAxis: { type: 'category', data: data.map(i => i.name) }, yAxis: { type: 'value' }, series: [{ data: data.map(i => i.value), type: 'line' }] };"
            },
            "datasource": { "source": "custom", "custom": "[{\"name\":\"Mon\",\"value\":150},{\"name\":\"Tue\",\"value\":230},{\"name\":\"Wed\",\"value\":224}]" }
            },
            {
            "id": "icon-1",
            "type": "b-icon",
            "name": "图标",
            "description": "",
            "props": { "type": "font-awesome", "icon": "fa-chart-line", "classNames": ["text-blue-500"], "customStyle": {} }
            },
            {
            "id": "html-1",
            "type": "b-html",
            "name": "HTML块",
            "description": "",
            "props": { "dataSource": { "name": "Confucius" }, "template": "<div>{{name}}, welcome to the world of programming.</div>" },
            "datasource": { "source": "custom", "custom": "{\"name\":\"Confucius\"}" }
            }
        ]
        }
    ]
    }
}
