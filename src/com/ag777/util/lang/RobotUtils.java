package com.ag777.util.lang;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

/**
 * 自动化助手类
 * 用于模拟鼠标和键盘操作，以及捕获屏幕截图。
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/2/26 09:00
 */
public class RobotUtils {

    /**
     * 模拟鼠标左键点击
     *
     * @param robot       Robot 实例
     * @param doubleClick 是否执行双击操作
     * @throws InterruptedException 如果等待中断
     */
    public static void clickLeftButton(Robot robot, boolean doubleClick) throws InterruptedException {
        mouseClick(robot, InputEvent.BUTTON1_DOWN_MASK);
        if (doubleClick) {
            mouseClick(robot, InputEvent.BUTTON1_DOWN_MASK);
        }
    }

    /**
     * 模拟鼠标中键点击
     *
     * @param robot       Robot 实例
     * @param doubleClick 是否执行双击操作
     * @throws InterruptedException 如果等待中断
     */
    public static void clickMiddleButton(Robot robot, boolean doubleClick) throws InterruptedException {
        mouseClick(robot, InputEvent.BUTTON2_DOWN_MASK);
        if (doubleClick) {
            mouseClick(robot, InputEvent.BUTTON2_DOWN_MASK);
        }
    }

    /**
     * 模拟鼠标右键点击
     *
     * @param robot       Robot 实例
     * @param doubleClick 是否执行双击操作
     * @throws InterruptedException 如果等待中断
     */
    public static void clickRightButton(Robot robot, boolean doubleClick) throws InterruptedException {
        mouseClick(robot, InputEvent.BUTTON3_DOWN_MASK);
        if (doubleClick) {
            mouseClick(robot, InputEvent.BUTTON3_DOWN_MASK);
        }
    }

    /**
     * 辅助方法，执行鼠标点击动作
     *
     * @param robot      Robot 实例
     * @param buttonMask 鼠标按钮掩码
     */
    private static void mouseClick(Robot robot, int buttonMask) {
        robot.mousePress(buttonMask);
        robot.mouseRelease(buttonMask);
    }

    /**
     * 模拟键盘组合按键按键点击
     *
     * @param robot    Robot 实例
     * @param keyCodes 键盘按键的 KeyCode 数组
     */
    public static void clickKeyboard(Robot robot, int[] keyCodes) {
        for (int keyCode : keyCodes) {
            robot.keyPress(keyCode);
        }
        // 倒着执行，比如ctrl+c，需要先放开c键
        for (int i = keyCodes.length - 1; i >= 0; i--) {
            robot.keyRelease(keyCodes[i]);
        }
    }

    /**
     * 捕获屏幕上指定区域的截图
     *
     * @param robot  Robot 实例，用于执行屏幕捕获
     * @param x      指定区域左上角的 X 坐标
     * @param y      指定区域左上角的 Y 坐标
     * @param width  指定区域的宽度
     * @param height 指定区域的高度
     * @return 返回一个 BufferedImage，包含了指定区域的屏幕截图
     */
    public static BufferedImage captureScreen(Robot robot, int x, int y, int width, int height) {
        return robot.createScreenCapture(new Rectangle(x, y, width, height));
    }

    /**
     * 捕获整个屏幕的截图
     *
     * @param robot Robot 实例，用于执行屏幕捕获
     * @return 返回一个 BufferedImage，包含了整个屏幕的截图
     */
    public static BufferedImage captureFullScreen(Robot robot) {
        // 获取屏幕的尺寸,修复高dpi下只截取了屏幕左上不分的问题
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();
        // 使用屏幕尺寸作为参数，调用 captureScreen 方法捕获全屏截图
        return captureScreen(robot, 0, 0, width, height);
    }
}
