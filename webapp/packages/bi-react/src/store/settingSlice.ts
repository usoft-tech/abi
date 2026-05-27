import { listConfigs } from "@/views/sa/services";
import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";

const initialState: Record<string, string> = {};

export const reloadConfigAsync = createAsyncThunk(
  "setting/reloadConfig",
  async () => {
    const { data } = await listConfigs({ page: 1, size: 1000 });
    return data.items || [];
  },
);

export const settingSlice = createSlice({
  name: "setting",
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder.addCase(reloadConfigAsync.fulfilled, (state, action) => {
      action.payload.forEach((item) => {
        state[item.configKey] = item.configValue;
      });
    });
  },
  selectors: {
    selectBoolean: (state, configKey: string, defaultValue = false) => {
      const value = state[configKey];
      if (typeof value !== "string") {
        return defaultValue;
      }
      return ["true", "1", "y"].includes(value.toLowerCase());
    },
    selectString: (state, configKey: string, defaultValue?: string) => {
      const value = state[configKey];
      if (typeof value !== "string") {
        return defaultValue;
      }

      const trimmedValue = value.trim();
      return trimmedValue?.length > 0 ? trimmedValue : defaultValue;
    },
  },
});

export const { selectBoolean, selectString } = settingSlice.selectors;


export default settingSlice.reducer;
