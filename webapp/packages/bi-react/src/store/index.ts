import { configureStore } from '@reduxjs/toolkit';
import appReducer from './appSlice';
import accountSlice from './accountSlice';
import handleSlice from './handleSlice';
import settingSlice from './settingSlice';

export const store = configureStore({
  reducer: {
    app: appReducer,
    account: accountSlice,
    handle: handleSlice,
    setting: settingSlice,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
