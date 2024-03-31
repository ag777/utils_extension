package com.ag777.util.lang;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

/**
 * 自动化助手类
 * 用于模拟鼠标和键盘操作，以及捕获屏幕截图。
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/3/31 12:59
 */
public class RobotUtils {

    private static final Robot ROBOT;

    static {
        try {
            ROBOT = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException("create robot err", e);
        }
    }

    /**
     * 点击鼠标左键。
     * 该方法模拟用户点击鼠标左键的行为，如果需要，还可以实现双击操作。
     *
     * @param doubleClick 指示是否执行双击操作的布尔值。如果为true，则执行双击动作；如果为false，则执行单击动作。
     */
    public static void clickLeftButton(boolean doubleClick) {
        clickLeftButton(ROBOT, doubleClick);
    }

    /**
     * 模拟鼠标左键点击
     *
     * @param robot       Robot 实例
     * @param doubleClick 是否执行双击操作
     */
    public static void clickLeftButton(Robot robot, boolean doubleClick) {
        mouseClick(robot, InputEvent.BUTTON1_DOWN_MASK);
        if (doubleClick) {
            mouseClick(robot, InputEvent.BUTTON1_DOWN_MASK);
        }
    }

    /**
     * 模拟鼠标中键点击。
     * 该方法是一个静态方法，调用者不需要实例化便可直接使用。
     * 它通过调用另一个具有更多参数的同名方法来实现点击操作。
     *
     * @param doubleClick 指示是否为双击操作的布尔值。如果为 true，则表示执行双击操作；如果为 false，则表示执行单击操作。
     */
    public static void clickMiddleButton(boolean doubleClick) {
        clickMiddleButton(ROBOT, doubleClick);
    }

    /**
     * 模拟鼠标中键点击
     *
     * @param robot       Robot 实例
     * @param doubleClick 是否执行双击操作
     */
    public static void clickMiddleButton(Robot robot, boolean doubleClick) {
        mouseClick(robot, InputEvent.BUTTON2_DOWN_MASK);
        if (doubleClick) {
            mouseClick(robot, InputEvent.BUTTON2_DOWN_MASK);
        }
    }

    /**
     * 模拟右键点击的操作，可以设置为单击或双击。
     *
     * @param doubleClick 如果为true，则表示执行双击操作；如果为false，则表示执行单击操作。
     */
    public static void clickRightButton(boolean doubleClick) {
        // 调用另一个具有相同名称但参数更多的方法，实现点击操作
        clickRightButton(ROBOT, doubleClick);
    }

    /**
     * 模拟鼠标右键点击
     *
     * @param robot       Robot 实例
     * @param doubleClick 是否执行双击操作
     */
    public static void clickRightButton(Robot robot, boolean doubleClick) {
        mouseClick(robot, InputEvent.BUTTON3_DOWN_MASK);
        if (doubleClick) {
            mouseClick(robot, InputEvent.BUTTON3_DOWN_MASK);
        }
    }

    /**
     * 实现鼠标点击操作。
     *
     * @param buttonMask 鼠标按键掩码，用于指定点击的鼠标按钮。不同的掩码值代表不同的鼠标按钮。
     *                   该参数详细说明可能依赖于具体实现或ROBOT类的定义。
     */
    public static void mouseClick(int buttonMask) {
        mouseClick(ROBOT, buttonMask);
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
     * 截取指定区域的屏幕图像。
     *
     * @param x      截取区域的左上角x坐标。
     * @param y      截取区域的左上角y坐标。
     * @param width  截取区域的宽度。
     * @param height 截取区域的高度。
     * @return 返回截取到的屏幕图像（BufferedImage对象）。
     */
    public static BufferedImage captureScreen(int x, int y, int width, int height) {
        return captureScreen(ROBOT, x, y, width, height);
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
        return captureScreen(robot, new Rectangle(x, y, width, height));
    }

    /**
     * 抓取屏幕指定区域的图像。
     *
     * @param screenRect 指定抓取的屏幕区域，如果为null，则抓取整个屏幕。
     * @return 返回一个BufferedImage对象，包含指定屏幕区域的图像。
     */
    public static BufferedImage captureScreen(Rectangle screenRect) {
        return captureScreen(ROBOT, screenRect);
    }

    /**
     * 使用Robot对象抓取屏幕指定区域的图像。
     *
     * @param robot      用于抓取屏幕的Robot对象。
     * @param screenRect 指定抓取的屏幕区域，如果为null，则抓取整个屏幕。
     * @return 返回一个BufferedImage对象，包含指定屏幕区域的图像。
     */
    public static BufferedImage captureScreen(Robot robot, Rectangle screenRect) {
        return robot.createScreenCapture(screenRect);
    }

    /**
     * 抓取全屏幕的图像。
     *
     * @return BufferedImage 返回一个包含整个屏幕图像的BufferedImage对象。
     */
    public static BufferedImage captureFullScreen() {
        return captureFullScreen(ROBOT);
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
