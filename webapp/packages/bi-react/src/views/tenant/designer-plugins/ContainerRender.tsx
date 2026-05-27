import {
  DropContainer,
  PageContext,
  useEvent
} from "bi-sdk-react";
import { SchemaItemType } from "bi-sdk-react/dist/types/components/typing";
import React, {
  type MouseEventHandler,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";

export type ContainerRenderProps = {
  item: any;
  ancestors: SchemaItemType[];
  id?: string;
  htmlTag?: string;
  classNames?: string[];
};

export const ContainerRender: React.FC<ContainerRenderProps> = ({
  id,
  item,
  ancestors,
  htmlTag = "div",
  classNames = [],
}) => {
  const { designable, handleCallback } = useContext(PageContext);
  const [localChildren, setLocalChildren] = useState(item.children || []);
  const { handleEvent } = useEvent(item);

  const onClick: MouseEventHandler = () => {
    if (item && handleCallback) handleCallback(item);
    handleEvent("click");
  };
  const onLocalChildrenChange = (list: any[]) => {
    item.children = list;
    setLocalChildren(list);
  };
  const actualClassNames = useMemo(() => {
    if (!designable) return classNames;
    const c = [];
    if (["a", "span"].includes(htmlTag)) {
      c.push("inline-block");
    }
    return [...c, ...classNames];
  }, [htmlTag, classNames, designable]);

  useEffect(() => {
    setLocalChildren(item.children || []);
  }, [item.children]);

  return (
    <DropContainer
      rootComponent={htmlTag as any}
      rootData={{
        id,
        onClick,
      }}
      item={item}
      list={localChildren}
      ancestors={[...ancestors, item]}
      onListChange={onLocalChildrenChange}
      className={actualClassNames.join(" ")}
    />
  );
};
