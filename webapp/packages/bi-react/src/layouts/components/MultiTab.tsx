import { addTag, config, removeTag } from "@/store/appSlice";
import { Tabs } from "antd";
import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useDispatch, useSelector } from "react-redux";
import {
  useLocation,
  useMatches,
  useNavigate,
  useRouteLoaderData,
} from "react-router-dom";
import styled from "styled-components";

// Styled Components
const StyledMultiTab = styled.div`
  background: #fff;
  padding: 6px 12px 0;

  .ant-tabs-nav {
    margin: 0 !important;
  }
`;

const MultiTab: React.FC<{ style?: React.CSSProperties }> = ({ style }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const matches = useMatches();
  const loader = useRouteLoaderData("app-page") as
    | { title?: string }
    | undefined;
  const { tags } = useSelector(config);
  const [activeKey, setActiveKey] = useState<string>(location.pathname);
  const { t } = useTranslation();

  // 监听路由变化，添加 Tag
  useEffect(() => {
    setActiveKey(location.pathname);
    // 获取当前路由的 title，从 handle 中读取
    const match = matches[matches.length - 1];
    const handle = match?.handle as
      | { title?: string; tag?: boolean }
      | undefined;
    const title = loader?.title || handle?.title;

    if (title) {
      dispatch(
        addTag({
          path: location.pathname,
          title: title,
          closable: true, // 默认都可关闭，后续可根据需求调整
        }),
      );
    }
  }, [location.pathname, matches, dispatch, loader?.title]);

  const onChange = (key: string) => {
    navigate(key);
  };

  const onEdit = (
    targetKey: React.MouseEvent | React.KeyboardEvent | string,
    action: "add" | "remove",
  ) => {
    if (action === "remove" && typeof targetKey === "string") {
      handleRemoveTag(targetKey);
    }
  };

  const handleRemoveTag = (targetKey: string) => {
    dispatch(removeTag(targetKey));

    // 如果关闭的是当前激活的 tab，需要跳转到其他 tab
    if (activeKey === targetKey) {
      const index = tags.findIndex((tag) => tag.path === targetKey);
      // 尝试跳转到前一个 tag，如果不存在则跳转到后一个，如果都不存在则跳转到首页
      // 注意：tags 在这里是旧的状态，因为 dispatch 是异步的或者 Redux 状态更新需要时间
      // 但实际上我们可以在这里直接根据当前 tags 计算
      const nextTags = tags.filter((tag) => tag.path !== targetKey);

      if (nextTags.length > 0) {
        // 如果删除的是最后一个，则跳转到新的最后一个
        // 如果删除的是中间的，index 位置会被后面的元素填补，所以取 index 或者 index - 1
        let nextPath = "";
        if (index === tags.length - 1) {
          nextPath = nextTags[nextTags.length - 1].path;
        } else {
          nextPath = nextTags[index].path;
        }
        navigate(nextPath);
      } else {
        navigate("/");
      }
    }
  };

  const items = tags.map((tag) => ({
    label: t(tag.title),
    key: tag.path,
    closable: tags.length > 1, // 只有一个标签时不可关闭
  }));

  return (
    <StyledMultiTab style={style}>
      <Tabs
        type="editable-card"
        hideAdd
        size="small"
        activeKey={activeKey}
        items={items}
        onChange={onChange}
        onEdit={onEdit}
        style={{ marginBottom: -1 }}
      />
    </StyledMultiTab>
  );
};

export default MultiTab;
