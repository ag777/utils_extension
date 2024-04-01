package com.ag777.util.lang;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/3/14 15:30
 */
public class ScreenUtils {

    /**
     * 获取当前屏幕的DPI（每英寸点数）。
     * @return 当前屏幕的DPI值。
     */
    public static int getDpi() {
        // 获取默认工具包
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.getScreenResolution();
    }

    /**
     * 计算并返回当前屏幕的DPI缩放比例。
     * 以96dpi为基准，这是一般的默认DPI值。
     * @return 当前屏幕的DPI缩放比例。
     */
    public static double getDpiScale() {
        // 计算并输出缩放比例，这里以96dpi（一般的默认DPI值）为基准
        return getDpi() / 96.0;
    }

    /**
     * 获取当前屏幕的尺寸大小。
     * @return 当前屏幕的尺寸，包括宽度和高度。
     */
    public static Dimension getScreenSize() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.getScreenSize();
    }

    /**
     * 获取主显示器的物理分辨率。
     * <p>
     * 此方法返回主显示器的实际分辨率，考虑了可能的DPI缩放设置。
     * 与 {@code Toolkit.getDefaultToolkit().getScreenSize()} 方法不同，
     * 它返回的是操作系统报告的屏幕实际使用分辨率，而非Java程序可能看到的逻辑分辨率。
     * </p>
     * <p>
     * 注意：如果系统连接了多个显示器，此方法只返回主显示器的分辨率。
     * </p>
     * <p>
     * 在大多数情况下，和User32.INSTANCE.GetSystemMetrics获取的屏幕分辨率会是相同的，特别是在单显示器配置中。然而，它们在概念上是不同的：
     * User32.INSTANCE.GetSystemMetrics 方法更多地关注操作系统层面的设置，适用于需要根据实际用户设置来调整应用程序窗口大小或布局的场景。
     * GraphicsEnvironment 和相关类提供的方法更接近于硬件层面，可以用于获取更详细的显示设备信息，包括但不限于分辨率。这对于需要深入了解连接显示设备特性的应用程序（如多显示器管理软件）来说非常有用。
     * </p>
     * @return 主显示器的物理分辨率。
     * @throws IllegalArgumentException 如果没有找到显示屏。
     */
    public static Dimension getPhysicalScreenResolution() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();

        for (GraphicsDevice curGs : gs) {
            DisplayMode dm = curGs.getDisplayMode();
            int screenWidth = dm.getWidth();
            int screenHeight = dm.getHeight();
            return new Dimension(screenWidth, screenHeight);
        }
        throw new IllegalArgumentException("没有找到显示屏");
    }

    /**
     * 将基于DPI的坐标转换为屏幕实际像素坐标。
     * @param dpiX 基于DPI的X坐标。
     * @param dpiY 基于DPI的Y坐标。
     * @return 屏幕的实际像素坐标。
     */
    public static Point2D.Double convertDpiToPixel(double dpiX, double dpiY) {
        double scaleFactor = getDpiScale();
        double pixelX = dpiX * scaleFactor;
        double pixelY = dpiY * scaleFactor;
        return new Point2D.Double(pixelX, pixelY);
    }

    /**
     * 将屏幕实际像素坐标转换为基于DPI的坐标。
     * @param pixelX 屏幕实际的X像素坐标。
     * @param pixelY 屏幕实际的Y像素坐标。
     * @return 基于DPI的坐标。
     */
    public static Point2D.Double convertPixelToDpi(double pixelX, double pixelY) {
        double scaleFactor = getDpiScale();
        double dpiX = pixelX / scaleFactor;
        double dpiY = pixelY / scaleFactor;
        return new Point2D.Double(dpiX, dpiY);
    }
}
