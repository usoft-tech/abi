import { createSlice } from "@reduxjs/toolkit";

interface HandleState {
  icon: number;
}

const initialState: HandleState = {
  icon: 0,
};

export const handleSlice = createSlice({
  name: "handle",
  initialState,
  reducers: {
    updateIcon: (state) => {
      state.icon = state.icon + 1;
    },
  },
});

export const {
  updateIcon,
} = handleSlice.actions;

export default handleSlice.reducer;
