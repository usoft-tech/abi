import React, { useImperativeHandle, useRef } from "react";
import MdEditor from "react-markdown-editor-lite";
// import style manually
import "react-markdown-editor-lite/lib/index.css";

import MdViewer from "./html";

// Register plugins if required
// MdEditor.use(YOUR_PLUGINS_HERE);

// Initialize a markdown parser
// const mdParser = new MarkdownIt(/* Markdown-it options */);

type Props = {
  value?: string;
  height?: string;
  menu?: boolean;
  md?: boolean;
  html?: boolean;
  readOnly?: boolean;
  onChange?: (value: string) => void;
};

const MarkdownEditor = React.forwardRef<MdEditor, Props>(
  (
    { value, height = "500px", menu = true, md = true, html = false, readOnly = false, onChange },
    forwardRef,
  ) => {
    const ref = useRef<MdEditor>(null);
    const handleEditorChange = ({ text }: { text: string }) => {
      onChange?.(text);
    };
    useImperativeHandle(forwardRef, () => ref.current!);
    return (
      <MdEditor
        ref={ref}
        style={{ height }}
        value={value || ""}
        // renderHTML={(text) => mdParser.render(text)}
        onChange={handleEditorChange}
        config={{
          view: {
            menu,
            md,
            html,
          },
        }}
        readOnly={readOnly}
        renderHTML={(text) => <MdViewer markdown={text} scrollEl={ref.current?.getHtmlElement?.()} />}
      />
    );
  },
);
export default MarkdownEditor;
