package com.ag777.util.lang;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * 自动化助手类
 * 用于模拟鼠标和键盘操作，以及捕获屏幕截图。
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/6/25 10:47
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

    public static Robot getRobot() {
        return ROBOT;
    }

    /**
     * 将鼠标移动到指定的屏幕坐标。
     *
     * @param x 指定的横坐标。
     * @param y 指定的纵坐标。
     * @return 如果鼠标成功移动到指定位置，则返回true；否则返回false。
     */
    public static boolean mouseMove(int x, int y) {
        return mouseMove(ROBOT, x, y); // 使用预定义的ROBOT对象，将鼠标移动到指定的(x, y)坐标。
    }

    /**
     * 使用预定义的Robot对象将鼠标移动到指定的屏幕坐标。
     *
     * @param x 鼠标要移动到的X轴坐标。
     * @param y 鼠标要移动到的Y轴坐标。
     * @param retryTimes 如果首次移动失败，尝试移动的次数。
     * @return boolean 如果鼠标成功移动到指定位置，则返回true；否则返回false。
     */
    public static boolean mouseMove(int x, int y, int retryTimes) {
        return mouseMove(ROBOT, x, y, retryTimes);
    }

    /**
     * 使用Robot类移动鼠标指针到指定的屏幕坐标。
     *
     * @param robot Robot对象，用于控制鼠标移动。
     * @param x 鼠标要移动到的X轴坐标。
     * @param y 鼠标要移动到的Y轴坐标。
     * @return boolean 返回移动鼠标操作是否成功。
     */
    public static boolean mouseMove(Robot robot, int x, int y) {
        return mouseMove(robot, x, y, 10);
    }

    /**
     * 平滑移动鼠标到指定坐标，尝试触发hover效果。
     * 使用while循环每50毫秒移动一次，直到到达目标或超时。
     * @param x 目标X坐标
     * @param y 目标Y坐标
     * @param moveFraction 每次移动剩余距离的比例，默认为0.1（即1/10）
     * @param timeout 超时时间，单位：毫秒
     */
    public static void mouseMoveSmooth(int x, int y, float moveFraction, long timeout) throws InterruptedException {
        mouseMoveSmooth(ROBOT, x, y, moveFraction, timeout);
    }

    /**
     * 平滑移动鼠标到指定坐标，尝试触发hover效果。
     * 使用while循环每50毫秒移动一次，直到到达目标或超时。
     * @param robot Robot实例
     * @param x 目标X坐标
     * @param y 目标Y坐标
     * @param moveFraction 每次移动剩余距离的比例，默认为0.1（即1/10）
     * @param timeout 超时时间，单位：毫秒
     */
    public static void mouseMoveSmooth(Robot robot, int x, int y, float moveFraction, long timeout) throws InterruptedException {
        if (robot == null) return;

        Instant startTime = Instant.now();

        while (Duration.between(startTime, Instant.now()).toMillis() < timeout) {
            // 计算剩余移动距离并按比例分配
            Point currentPos = MouseInfo.getPointerInfo().getLocation();
            int remainingX = x - currentPos.x;
            int remainingY = y - currentPos.y;
            double remainingDistance = Math.sqrt(remainingX * remainingX + remainingY * remainingY);

            // 如果剩余距离小于等于1（考虑到浮点运算误差），认为已到达目标
            if (remainingDistance <= 1) {
                mouseMove(x, y);
                return;
            }

            // 向目标移动剩余距离的1/10
            int moveX = (int) (currentPos.x + remainingX * moveFraction);
            int moveY = (int) (currentPos.y + remainingY * moveFraction);
            robot.mouseMove(moveX, moveY);

            TimeUnit.MILLISECONDS.sleep(50); // 每次移动后等待50毫秒

        }

        // 时间耗尽，强制移动到目标
        mouseMove(x, y);
    }

    /**
     * 使用Robot类移动鼠标到指定位置，并尝试多次以确保准确移动。
     *
     * @param robot Robot对象，用于控制鼠标移动。
     * @param x 目标位置的x坐标。
     * @param y 目标位置的y坐标。
     * @param retryTimes 尝试移动到目标位置的次数。
     * @return 如果鼠标成功移动到目标位置，则返回true；否则返回false。
     */
    public static boolean mouseMove(Robot robot, int x, int y, int retryTimes) {
        int n = 0;
        // 尝试指定次数以确保鼠标移动到目标位置
        while(n<retryTimes) {
            robot.mouseMove(x, y); // 移动鼠标到目标位置
            Point location = MouseInfo.getPointerInfo().getLocation(); // 获取鼠标当前位置
            // 判断鼠标是否已经移动到目标位置
            if (location.getX() == x && location.getY() == y) {
                return true;
            }
            n++; // 尝试次数递增
        }
        // 如果未能成功移动到目标位置，则返回false
        return false;
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
     * 使用键盘模拟点击指定的按键。
     * @param keyCodes 按键码数组，代表需要模拟点击的按键。每个按键码对应于Java AWT中的KeyEvent定义的常量。
     */
    public static void clickKeyboard(int[] keyCodes) {
        // 通过预定义的ROBOT对象，模拟点击指定的按键序列
        clickKeyboard(ROBOT, keyCodes);
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
