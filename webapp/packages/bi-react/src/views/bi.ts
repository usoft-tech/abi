import { uploadFile } from "@/services/app";
import {
  activateConversation,
  deleteConversation,
  deleteConversationMessage,
  listConversationMessages,
  listConversations,
} from "@/services/chat";
import {
  ChatConversationType,
  ChatMessageType,
  FetchType,
} from "bi-sdk-react/dist/types/components/typing";
import {
  assistantChat,
  executeDataset,
  getPageExample,
  getPageTemplate,
  listPageExamples,
  listPageTemplates,
} from "./tenant/services";

// let chatId: string | null = null;

export const fetch: FetchType = {
  upload: async (file) => {
    return uploadFile("page-design-file", file).then((res) => ({
      id: res.data.id,
      name: res.data.name,
    }));
  },
  dataset(dataSetId, params, aiPrompt) {
    if (!dataSetId) {
      return Promise.reject(new Error("dataSetId is required"));
    }
    return executeDataset(dataSetId, params, aiPrompt);
  },
  exampleList: async (page, size, keyword) => {
    return listPageExamples({ page, size, keyword }).then((res) => {
      const { total, items } = res.data;
      return {
        total,
        list: items,
      };
    });
  },
  templateList: async (page, size, keyword) => {
    return listPageTemplates({ page, size, keyword }).then((res) => {
      const { total, items } = res.data;
      return {
        total,
        list: items,
      };
    });
  },
  templateDetail: async (id) => {
    return getPageTemplate(id as string).then((res) => res.data);
  },
  ai: {
    chat: async (bizType, bizId, conversationId, request) => {
      return assistantChat(bizType, bizId, conversationId, request).then(
        (res) => res.data,
      );
      // const k = chatId?.length ? "second" : "first";
      // chatId = chatId || uuid();
      // return new Promise((resolve) => {
      //   setTimeout(() => {
      //     const res: ChatResponseType = {
      //       id: uuid(),
      //       answer: {
      //         answer: example[k].summary,
      //         plans: example[k].plans,
      //         effect: {
      //           schema: example[k].schema as any,
      //         },
      //         extra: example[k].extra as any,
      //       },
      //       createdAt: new Date().toISOString(),
      //       conversation: {
      //         id: chatId || uuid(),
      //         name: request.message,
      //         createdAt: new Date().toISOString(),
      //         isActived: true,
      //       }
      //     }
      //     console.log(res);
      //     resolve(res);
      //   }, 5000);
      // });
    },
    conversationList: async (bizType, bizId) => {
      return listConversations({
        bizType,
        bizId,
      }).then((res) =>
        res.data.map(
          ({ id, name, createdAt }, index) =>
            ({
              id,
              name,
              createdAt,
              isActived: index === 0,
            }) as ChatConversationType,
        ),
      );
    },
    activateConversation: async (conversationId) => {
      return activateConversation(conversationId).then((res) => res.data);
    },
    messageList: async (conversationId, prevId) => {
      return listConversationMessages({
        conversationId: conversationId || undefined,
        prevId: prevId || undefined,
      }).then((res) => {
        return res.data.map(
          ({ id, question, extra, files, agents, atItems, createdAt }) =>
            ({
              id,
              conversationId,
              question,
              answer: JSON.parse(extra) as ChatMessageType["answer"],
              files,
              agents,
              atItems,
              createdAt,
            }) as ChatMessageType,
        );
      });
    },
    removeConversation: async (conversationId) => {
      return deleteConversation(conversationId).then((res) => res.data);
    },
    removeMessage: async (messageId) => {
      return deleteConversationMessage(messageId).then((res) => res.data);
    },
  },
};
