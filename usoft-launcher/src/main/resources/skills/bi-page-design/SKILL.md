---
name: bi-page-schema
description: Generate BI page schemas (PageSchema JSON) based on user requirements. Use this skill when the user asks to design, create, or update a BI page or dashboard.
---

# BI Page Design

This skill generates low-code BI page schemas (PageSchema) based on user requirements. It produces a JSON structure that defines the layout, components, and data sources for a BI dashboard.

## PageSchema Specification

### PageSchema Fields
- `info`: Object containing page basic info (e.g., `name`, `description`).
- `items`: Array of top-level page elements, each being a `SchemaItem`.

### SchemaItem Fields
- `id`: String, unique identifier.
- `type`: Component type.
- `name`: Name.
- `description`: Description of component data, used for generating data query statements.
- `props`: Component properties object.
- `cascadeIds`: String array, list of linkage IDs.
- `children`: Component child elements, array or map (key is slot name, value is element list), only supported by `b-container`.
- `datasource`: Component data source definition (`source`, `datasourceId`, `scriptId`, `custom`).

### Component Fields

#### b-icon
- **props**:
  - `type`: "font-awesome" or "icon-font"
  - `icon`: Icon name, corresponding to the icon library based on `type`.
  - `classNames`: Array of style class names (tailwindcss classes).

#### b-container (Supports tailwindcss classes)
- **props**:
  - `htmlTag`: HTML tag (e.g., "div").
  - `classNames`: Array of style class names (tailwindcss classes).
- **children**: Array or map (organized by slot).

#### b-text
- **props**:
  - `text`: Text content.
  - `classNames`: Array of style class names (tailwindcss classes).

#### b-html
- **datasource**:
  - `source`: "custom"
  - `custom`: String (JSON format data).
- **props**:
  - `template`: String (supports `{{var}}` template interpolation).

#### b-echarts
- **datasource**:
  - `source`: "custom"
  - `custom`: String (JSON array data).
- **props**:
  - `script`: JavaScript code, receives `data`, forbids defining `data` variable internally, returns ECharts option object.

## Examples

### Component Snippets

**Simple Card:**
```json
{
  "id": "card-container-1",
  "type": "b-container",
  "name": "Card Style Example 1",
  "description": "Card Style Example 1",
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
```

**Grid Style (4 Columns):**
```json
{
  "id": "grid-container-1",
  "type": "b-container",
  "name": "Grid Style Example 1",
  "description": "Grid Style Example 1: 4 columns",
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
```

### Full PageSchema Example

```json
{
  "summary": "Sales Dashboard showing daily sales by brand.",
  "schema": {
    "info": { "name": "Sales Dashboard", "description": "Daily sales by brand" },
    "items": [
      {
        "id": "container-1",
        "type": "b-container",
        "name": "Main Container",
        "description": "Page Body",
        "props": { "htmlTag": "div", "classNames": ["container", "mx-auto", "p-4"] },
        "cascadeIds": [],
        "children": [
          {
            "id": "title-1",
            "type": "b-text",
            "name": "Title",
            "description": "",
            "props": { "text": "Sales Trend" }
          },
          {
            "id": "chart-1",
            "type": "b-echarts",
            "name": "Line Chart",
            "description": "",
            "props": {
              "script": "return { xAxis: { type: 'category', data: data.map(i => i.name) }, yAxis: { type: 'value' }, series: [{ data: data.map(i => i.value), type: 'line' }] };"
            },
            "datasource": { "source": "custom", "custom": "[{\"name\":\"Mon\",\"value\":150},{\"name\":\"Tue\",\"value\":230},{\"name\":\"Wed\",\"value\":224}]" }
          }
        ]
      }
    ]
  }
}
```
