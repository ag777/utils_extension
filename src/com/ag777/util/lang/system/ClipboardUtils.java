package com.ag777.util.lang.system;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * 剪贴板工具类
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/3/06 18:00
 */
public class ClipboardUtils implements ClipboardOwner {
    private static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();

    /**
     * 将文本复制到剪贴板。
     *
     * @param text 要复制的文本
     * @throws IllegalStateException 如果无法获取系统剪贴板
     */
    public static void copyTextToClipboard(String text) throws IllegalStateException {
        StringSelection stringSelection = new StringSelection(text);
        copyToClipboard(stringSelection);
    }

    /**
     * 从剪贴板获取文本。
     *
     * @return 剪贴板中的文本，如果剪贴板为空或包含的不是文本则返回null
     * @throws UnsupportedFlavorException 如果请求的数据类型不可用
     * @throws IOException                如果请求的数据不再可用
     */
    public static String getTextFromClipboard() throws UnsupportedFlavorException, IOException {
        return (String) getFromClipboard(DataFlavor.stringFlavor);
    }

    /**
     * 从剪贴板中获取HTML片段。
     * 此方法尝试从系统剪贴板中提取用户选择的HTML片段，而不是完整的HTML文档。
     * 它适用于需要处理用户选定文本或元素的情况，例如复制的一小段HTML代码。
     *
     * @return 剪贴板中的HTML片段字符串。如果剪贴板中没有相应格式的数据，则可能返回null或抛出异常。
     * @throws UnsupportedFlavorException 如果剪贴板中没有HTML片段格式的数据。
     * @throws IOException 如果从剪贴板获取数据时发生输入输出异常。
     */
    public static String getFragmentHtmlFromClipboard() throws UnsupportedFlavorException, IOException {
        return (String) getFromClipboard(DataFlavor.fragmentHtmlFlavor);
    }

    /**
     * 从剪贴板中获取选择的完整HTML内容。
     * 此方法尝试从系统剪贴板中提取用户选择的完整HTML内容，包括HTML文档的结构和样式信息。
     * 它适用于需要获取包含样式和元数据的完整HTML文档的情况。
     *
     * @return 剪贴板中的完整HTML内容字符串。如果剪贴板中没有相应格式的数据，则可能返回null或抛出异常。
     * @throws UnsupportedFlavorException 如果剪贴板中没有选择的HTML内容格式的数据。
     * @throws IOException 如果从剪贴板获取数据时发生输入输出异常。
     */
    public static String getAllHtmlFromClipboard() throws UnsupportedFlavorException, IOException {
        return (String) getFromClipboard(DataFlavor.selectionHtmlFlavor);
    }

    /**
     * 将图片复制到剪贴板。
     *
     * @param image 要复制的图片对象
     * @throws IllegalStateException 如果无法获取系统剪贴板
     */
    public static void copyImageToClipboard(Image image) throws IllegalStateException {
        ImageTransferable imageTransferable = new ImageTransferable(image);
        copyToClipboard(imageTransferable);
    }

    /**
     * 从剪贴板获取图片。
     *
     * @return 剪贴板中的图片，如果剪贴板为空或包含的不是图片则返回null
     * @throws UnsupportedFlavorException 如果请求的数据类型不可用
     * @throws IOException                如果请求的数据不再可用
     */
    public static Image getImageFromClipboard() throws UnsupportedFlavorException, IOException {
        return (Image) getFromClipboard(DataFlavor.imageFlavor);
    }

    /**
     * 从剪贴板获取Java文件列表。
     *
     * @return 剪贴板中的Java文件列表。
     * @throws UnsupportedFlavorException 如果请求的数据 flavor 不受支持。
     * @throws IOException 如果请求的数据不可用，或者数据不再有效。
     */
    public static List<File> getJavaFileListFromClipboard() throws UnsupportedFlavorException, IOException {
        return (List<File>) getFromClipboard(DataFlavor.javaFileListFlavor);
    }

    /**
     * 从剪贴板获取序列化的对象。
     *
     * @return 剪贴板中的序列化Java对象。
     * @throws UnsupportedFlavorException 如果请求的数据 flavor 不受支持。
     * @throws IOException 如果请求的数据不可用，或者数据不再有效。
     */
    public static Serializable getSerializedObjectFromClipboard() throws UnsupportedFlavorException, IOException {
        DataFlavor serializedObjFlavor = new DataFlavor(DataFlavor.javaSerializedObjectMimeType, "Java Serialized Object");
        return (Serializable) getFromClipboard(serializedObjFlavor);
    }

    /**
     * 将传输数据复制到剪贴板。
     *
     * @param transferable 要复制到剪贴板的传输数据
     * @throws IllegalStateException 如果无法获取系统剪贴板
     */
    public static void copyToClipboard(Transferable transferable) throws IllegalStateException {
        CLIPBOARD.setContents(transferable, null);
    }

    /**
     * 从剪贴板获取传输数据。
     *
     * @param flavor 指定要获取的数据类型
     * @return 剪贴板中的数据对象
     * @throws UnsupportedFlavorException 如果请求的数据类型不可用
     * @throws IOException                如果请求的数据不再可用
     */
    public static Object getFromClipboard(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return CLIPBOARD.getData(flavor);
    }

    /**
     * 当剪贴板的内容改变时调用。
     *
     * @param clipboard 剪贴板
     * @param contents  剪贴板内容
     */
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // 当前不需要处理所有权丢失的情况
    }

    /**
     * 用于转换图片为传输数据的内部类。
     */
    private static class ImageTransferable implements Transferable {
        private Image image;

        public ImageTransferable(Image image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!DataFlavor.imageFlavor.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }
    }
}
