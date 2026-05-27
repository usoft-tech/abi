import http from "./request";

export type ConversationQueryRequest = {
  prevId?: string;
  limit?: number;
  name?: string;
  bizType?: string;
  bizId?: string;
  keyword?: string;
};

export type ConversationResponse = {
  id: string;
  bizType: string;
  bizId: string;
  name: string;
  createdAt: string;
};

export type ConversationMessageQueryRequest = {
  prevId?: string;
  limit?: number;
  conversationId?: string;
  keyword?: string;
};

export type ChatMessageFile = {
  id: string;
  name: string;
  extension: string;
};

export type ChatMessageAgent = {
  id: string;
  name: string;
};

export type ConversationMessageResponse = {
  id: string;
  conversationId: string;
  question: string;
  prompt: string;
  files: ChatMessageFile[];
  agents: ChatMessageAgent[];
  atItems: string | null;
  extra: string;
  answer: string;
  createdAt: string;
};

export const listConversations = (params: ConversationQueryRequest) => {
  return http.get<ConversationResponse[]>("/ai/conversations", params);
};

export const deleteConversation = (id: string) => {
  return http.delete<boolean>(`/ai/conversations/${id}`);
};

export const activateConversation = (id: string) => {
  return http.post<boolean>(`/ai/conversations/${id}/activate`);
};

export const listConversationMessages = (
  params: ConversationMessageQueryRequest
) => {
  return http.get<ConversationMessageResponse[]>("/ai/messages", params);
};

export const deleteConversationMessage = (id: string) => {
  return http.delete<boolean>(`/ai/messages/${id}`);
};