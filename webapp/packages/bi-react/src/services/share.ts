import { Dayjs } from "dayjs";
import { AuthItem, AuthorizationBizType } from "./authorization";


export enum ShareLinkExpireType {
  Permanent = "PERMANENT",
  OneDay = "ONE_DAY",
  SevenDay = "SEVEN_DAY",
  ThirtyDay = "THIRTY_DAY",
  Custom = "CUSTOM",
}

export interface ShareLink {
  bizType: AuthorizationBizType;
  bizId: string;
  expireType: ShareLinkExpireType;
  expireAt?: string | Dayjs;
  authorizations: AuthItem[];
};

export type ShareLinkCreateRequest = ShareLink;

export interface ShareLinkResponse extends ShareLink {
  shareKey: string;
}