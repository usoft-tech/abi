import { AppResponse } from "@/services/app";
import { createSlice, PayloadAction } from "@reduxjs/toolkit";

export interface TagItem {
  path: string;
  title: string;
  closable?: boolean;
}

interface AppState {
  name: string;
  layout: "side-menu" | "top-menu" | "top-side-menu";
  theme: "dark" | "light";
  collapsed: boolean;
  tags: TagItem[];
  route: {
    multiple: boolean;
  };
  currentApp?: AppResponse | null;
}

const initialState: AppState = {
  name: "智数报表",
  layout: "top-side-menu",
  theme: "light",
  collapsed: false,
  tags: [],
  route: {
    multiple: true,
  },
  currentApp: null,
};

export const appSlice = createSlice({
  name: "app",
  initialState,
  reducers: {
    toggleLayout: (state) => {
      state.layout = state.layout === "side-menu" ? "top-menu" : "side-menu";
      localStorage.setItem("layout", state.layout);
    },
    toggleMultipleRoute: (state) => {
      state.route.multiple = !state.route.multiple;
      localStorage.setItem("layout_route", state.route.multiple.toString());
    },
    setLayout: (state, action: PayloadAction<"side-menu" | "top-menu" | "top-side-menu">) => {
      state.layout = action.payload;
      if (state.layout === "top-menu") {
        state.currentApp = null;
      }
      localStorage.setItem("layout", state.layout);
    },
    toggleTheme: (state) => {
      state.theme = state.theme === "dark" ? "light" : "dark";
      localStorage.setItem("layout_theme", state.theme);
    },
    setTheme: (state, action: PayloadAction<"dark" | "light">) => {
      state.theme = action.payload;
      localStorage.setItem("layout_theme", state.theme);
    },
    setCollapsed: (state, action: PayloadAction<boolean>) => {
      state.collapsed = action.payload;
      localStorage.setItem("layout_collapsed", state.collapsed.toString());
    },
    toggleCollapsed: (state) => {
      state.collapsed = !state.collapsed;
      localStorage.setItem("layout_collapsed", state.collapsed.toString());
    },
    addTag: (state, action: PayloadAction<TagItem>) => {
      const { path } = action.payload;
      if (!state.tags.some((tag) => tag.path === path)) {
        state.tags.push(action.payload);
      }
    },
    removeTag: (state, action: PayloadAction<string>) => {
      state.tags = state.tags.filter((tag) => tag.path !== action.payload);
    },
    setCurrentApp: (state, action: PayloadAction<AppResponse | null>) => {
      state.currentApp = action.payload;
    },
  },
  selectors: {
    config: (state): AppState => {
      return {
        name: state.name,
        layout: (localStorage.getItem("layout") || state.layout) as "side-menu" | "top-menu" | "top-side-menu",
        theme: (localStorage.getItem("layout_theme") || state.theme) as "dark" | "light",
        collapsed: localStorage.getItem("layout_collapsed") === "true" || state.collapsed,
        route: {
          multiple: localStorage.getItem("layout_route") === "true" || state.route.multiple,
        },
        currentApp: state.currentApp,
        tags: state.tags,
      }
    },
  }
});

export const {
  toggleLayout,
  toggleTheme,
  toggleMultipleRoute,
  setCollapsed,
  toggleCollapsed,
  addTag,
  removeTag,
  setTheme,
  setLayout,
  setCurrentApp,
} = appSlice.actions;

export const { config } = appSlice.selectors;

export default appSlice.reducer;
