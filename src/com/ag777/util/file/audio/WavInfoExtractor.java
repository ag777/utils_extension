package com.ag777.util.file.audio;

import com.ag777.util.file.audio.model.WavInfo;

import java.io.IOException;
import java.io.InputStream;

/**
 * wav信息提取工具
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/2/20 14:51
 */
public class WavInfoExtractor {
    /**
     * 从WAV文件数据中提取参数。
     *
     * @param wavData WAV文件的字节数据。
     * @return 包含WAV文件参数的WavInfo对象。
     * @throws IOException 如果提取过程中发生错误。
     */
    public static WavInfo extractInfo(byte[] wavData) throws IOException {
        if (wavData.length < 44) {
            throw new IOException("Invalid WAV file: too short to contain required WAV header.");
        }
        return extractInfoFromHeader(wavData);
    }

    /**
     * 从WAV文件输入流中提取参数。
     *
     * @param wavInputStream WAV文件的输入流。
     * @return 包含WAV文件参数的WavInfo对象。
     * @throws IOException 如果提取过程中发生错误。
     */
    public static WavInfo extractInfo(InputStream wavInputStream) throws IOException {
        // WAV头部固定长度为44字节
        byte[] header = new byte[44];
        int bytesRead = wavInputStream.read(header, 0, header.length);

        if (bytesRead < header.length) {
            throw new IOException("Invalid WAV file: too short to contain required WAV header.");
        }
        return extractInfoFromHeader(header);
    }

    /**
     * 从WAV文件头部提取信息。
     *
     * @param header WAV文件的头部字节数据。
     * @return 包含WAV文件参数的WavInfo对象。
     */
    private static WavInfo extractInfoFromHeader(byte[] header) {
        // 从字节数据中提取参数
        String chunkID = new String(header, 0, 4);
        int chunkSize = byteArrayToInt(header, 4);
        String format = new String(header, 8, 4);
        String subchunk1ID = new String(header, 12, 4);
        int subchunk1Size = byteArrayToInt(header, 16);
        short audioFormat = byteArrayToShort(header, 20);
        int numChannels = byteArrayToShort(header, 22);
        int sampleRate = byteArrayToInt(header, 24);
        int byteRate = byteArrayToInt(header, 28);
        short blockAlign = byteArrayToShort(header, 32);
        int bitsPerSample = byteArrayToShort(header, 34);
        String subchunk2ID = new String(header, 36, 4);
        int subChunk2Size = byteArrayToInt(header, 40);
        int pcmDataLength = subChunk2Size;

        return new WavInfo(chunkID, chunkSize, format, subchunk1ID, subchunk1Size, audioFormat, numChannels, sampleRate,
                byteRate, blockAlign, bitsPerSample, subchunk2ID, pcmDataLength);
    }

    private static int byteArrayToInt(byte[] data, int start) {
        return ((data[start + 3] & 0xff) << 24) |
                ((data[start + 2] & 0xff) << 16) |
                ((data[start + 1] & 0xff) << 8) |
                (data[start] & 0xff);
    }

    private static short byteArrayToShort(byte[] data, int start) {
        return (short) (((data[start + 1] & 0xff) << 8) |
                (data[start] & 0xff));
    }

}
