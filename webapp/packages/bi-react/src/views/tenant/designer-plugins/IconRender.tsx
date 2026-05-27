import React, { MouseEventHandler, useMemo } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { IconName, library } from "@fortawesome/fontawesome-svg-core";
import { fas } from "@fortawesome/free-solid-svg-icons";
import { far } from "@fortawesome/free-regular-svg-icons";
import { fab } from "@fortawesome/free-brands-svg-icons";
import IconFont from "@/components/icon/IconFont";
import { useEvent } from "bi-sdk-react";

library.add(fas, far, fab);

export type IconRenderProps = {
  id?: string;
  type?: "font-awesome" | "icon-font";
  icon?: string;
  item?: any;
  customStyle?: React.CSSProperties;
  style?: React.CSSProperties;
  className?: string;
  classNames?: string[];
};

export const IconRender: React.FC<IconRenderProps> = ({
  id,
  type = "font-awesome",
  icon,
  item,
  customStyle = {},
  style = {},
  className,
  classNames = [],
}) => {
  const { handleEvent } = useEvent(item);
  const actualStyle = useMemo(
    () => ({
      ...(item?.style || {}),
      ...(customStyle || {}),
      ...(style || {}),
    }),
    [item, customStyle, style],
  );
  const actualClassName = useMemo(
    () => (classNames || []).join(" ") + (" " + className || ""),
    [classNames, className],
  );

  const click: MouseEventHandler = () => {
    handleEvent("click");
  };

  if (!icon)
    return (
      <IconFont
        type="icon-delete-square"
        id={id}
        style={actualStyle}
        className={actualClassName}
        onClick={click}
      />
    );
  if (type === "font-awesome") {
    return (
      <FontAwesomeIcon
        icon={icon as IconName}
        id={id}
        style={actualStyle}
        className={actualClassName}
        onClick={click}
      />
    );
  }
  return (
    <IconFont
      type={icon}
      id={id}
      style={actualStyle}
      className={actualClassName}
      onClick={click}
    />
  );
};

export default IconRender;
