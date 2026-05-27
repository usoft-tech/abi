import { Upload, Image } from "antd";
import ImgCrop, { ImgCropProps } from "antd-img-crop";
import type { GetProp, UploadFile, UploadProps } from "antd/es";
import React, { useState } from "react";
import config from "@/config";

import {
  getTenantId,
  getToken,
  TENANT_ID_HEADER,
  TOKEN_PREFIX,
  TOKEN_HEADER,
} from "@/utils/token";
import { uuid } from "@/utils";

type FileType = Parameters<GetProp<UploadProps, "beforeUpload">>[0];

type SimpleFileType = {
  name?: string;
  url?: string;
};

interface CropUploadProps
  extends Pick<
    UploadProps,
    | "disabled"
    | "accept"
    | "multiple"
    | "maxCount"
    | "showUploadList"
    | "styles"
    | "classNames"
  > {
  bizType?: string;
  cropShape?: ImgCropProps["cropShape"];
  aspect?: ImgCropProps["aspect"];
  aspectSlider?: ImgCropProps["aspectSlider"];
  fileList?: SimpleFileType[] | string[] | null;
  onChange?: (fileList: SimpleFileType[]) => void;
}

const getBase64 = (file: FileType): Promise<string> =>
  new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = (error) => reject(error);
  });

const CropUpload: React.FC<CropUploadProps> = ({
  bizType,
  cropShape = "rect",
  aspect = 1,
  aspectSlider = true,
  accept = "image/png,image/jpeg",
  multiple = false,
  maxCount = 1,
  fileList: initialList,
  onChange,
  ...rest
}) => {
  const list: UploadFile[] = (initialList || []).map((file) => {
    let name: string, url: string;
    if (typeof file === "string") {
      url = file;
      name = file.substring(file.lastIndexOf("/") + 1);
    } else {
      url = file.url as string;
      name = file.name as string;
    }
    return {
      uid: uuid(),
      name,
      url,
      status: "done",
    };
  });
  const [previewOpen, setPreviewOpen] = useState(false);
  const [previewImage, setPreviewImage] = useState("");
  const [fileList, setFileList] = useState<UploadFile[]>(list);
  const token = getToken();
  const tenantId = getTenantId();

  const handleChange: UploadProps["onChange"] = ({ fileList: newFileList }) => {
    setFileList(newFileList);
    if (onChange && newFileList.length && newFileList[0].status === "done") {
      const file = newFileList[0];
      onChange([
        {
          name: file.name,
          url: file.response.data.url,
        },
      ]);
    }
  };

  const handlePreview = async (file: UploadFile) => {
    if (!file.url && !file.preview) {
      file.preview = await getBase64(file.originFileObj as FileType);
    }

    setPreviewImage(file.url || (file.preview as string));
    setPreviewOpen(true);
  };

  const headers: Record<string, string> = {};
  if (token) {
    headers[TOKEN_HEADER] = TOKEN_PREFIX + token;
  }
  if (tenantId) {
    headers[TENANT_ID_HEADER] = tenantId;
  }

  return (
    <>
      <ImgCrop
        cropShape={cropShape}
        aspect={aspect}
        rotationSlider={true}
        aspectSlider={aspectSlider}
        showReset={true}
      >
        <Upload
          {...rest}
          name="file"
          accept={accept || "image/png,image/jpeg"}
          multiple={multiple || false}
          maxCount={maxCount || 1}
          action={config.apiBasePath + "/files/upload"}
          listType="picture-card"
          headers={headers}
          data={{
            type: bizType,
          }}
          fileList={fileList}
          onChange={handleChange}
          onPreview={handlePreview}
        >
          {fileList.length < 5 && "+ 上传"}
        </Upload>
      </ImgCrop>
      {previewImage && (
        <Image
          styles={{ root: { display: "none" } }}
          preview={{
            open: previewOpen,
            onOpenChange: (visible) => setPreviewOpen(visible),
            afterOpenChange: (visible) => !visible && setPreviewImage(""),
          }}
          src={previewImage}
        />
      )}
    </>
  );
};

export default CropUpload;
