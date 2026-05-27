import http from "./request";

export interface UserProfileUpdateRequest {
  displayName?: string;
  avatar?: string;
  oldPassword?: string;
  newPassword?: string;
}

export const updateProfile = (data: UserProfileUpdateRequest) => {
  return http.put<any>("/system/users/profile", data);
};
