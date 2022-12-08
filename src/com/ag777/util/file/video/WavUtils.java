package com.ag777.util.file.video;

import com.ag777.util.lang.IOUtils;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 音频文件工具类
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2022/11/2 11:12
 */
public class WavUtils {

    public static long getSecondLength(File file) throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        return TimeUnit.MICROSECONDS.toSeconds(getMicrosecondLength(file));
    }

    /**
     *
     * @param file 音频文件
     * @return 音频长度， 通过TimeUnit.MICROSECONDS可以转化为秒
     * @throws LineUnavailableException if a clip object is not available due to resource restrictions
     * @throws IOException io异常
     * @throws UnsupportedAudioFileException if the system does not support at least one clip instance through any installed mixer
     */
    public static long getMicrosecondLength(File file) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        Clip clip = null;
        AudioInputStream ais = null;
        try {
            clip = AudioSystem.getClip();
            ais = AudioSystem.getAudioInputStream(file);
            clip.open(ais);
            return clip.getMicrosecondLength();
        } finally {
            IOUtils.close(clip, ais);
        }

    }
}
