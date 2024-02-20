package com.ag777.util.file.audio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/2/20 14:31
 */
public class PcmToWavConverter {

    /**
     * 将PCM字节数据转换为WAV字节数据，允许指定参数。
     *
     * @param pcmData       PCM数据。
     * @param sampleRate    采样率（例如44100Hz）。
     * @param numChannels   声道数（1表示单声道，2表示立体声）。
     * @param bitsPerSample 每个采样的位数（例如16）。
     * @return WAV数据。
     */
    public byte[] toWav(byte[] pcmData, int sampleRate, int numChannels, int bitsPerSample) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // 写入WAV文件头
            writeWavHeader(baos, pcmData.length, sampleRate, numChannels, bitsPerSample);

            // 写入PCM数据
            baos.write(pcmData);
        } catch (IOException e) {
            // ByteArrayOutputStream不应该发生IOException
            throw new RuntimeException(e);
        }

        return baos.toByteArray();
    }


    /**
     * 写入WAV文件头到输出流。
     *
     * @param out            输出流，WAV文件头将写入这个流。
     * @param pcmDataLength  PCM数据的长度。
     * @param sampleRate     采样率。
     * @param numChannels    声道数。
     * @param bitsPerSample  每个样本的位数。
     * @throws IOException   如果发生I/O错误。
     */
    private void writeWavHeader(ByteArrayOutputStream out, int pcmDataLength, int sampleRate, int numChannels, int bitsPerSample) throws IOException {
        // 计算每个声道块的字节数
        int blockAlign = numChannels * (bitsPerSample / 8);
        // 计算数据子块的大小（即PCM数据的大小）
        int subChunk2Size = pcmDataLength * numChannels * (bitsPerSample / 8);
        // 计算整个文件的大小（不包括RIFF标识本身和大小字段）
        int chunkSize = 36 + subChunk2Size;

        // RIFF头
        out.write(new byte[]{'R', 'I', 'F', 'F'}); // ChunkID - 标识为"RIFF"，表示这是一个RIFF格式的文件
        out.write(intToByteArray(chunkSize));       // ChunkSize - 这是整个文件的大小减去8字节（ChunkID和ChunkSize的大小）
        out.write(new byte[]{'W', 'A', 'V', 'E'}); // Format - 格式标识，这里为"WAVE"，表示文件是WAV格式

        // fmt子块
        out.write(new byte[]{'f', 'm', 't', ' '}); // Subchunk1ID - 子块1的标识为"fmt "，表示格式描述头
        out.write(intToByteArray(16));             // Subchunk1Size - 子块1的大小，对于PCM格式，这里总是16字节
        out.write(shortToByteArray((short) 1));    // AudioFormat - 音频格式，PCM格式为1
        out.write(shortToByteArray((short) numChannels)); // NumChannels - 声道数，单声道为1，立体声为2
        out.write(intToByteArray(sampleRate));     // SampleRate - 采样率，例如44100Hz
        out.write(intToByteArray(sampleRate * blockAlign)); // ByteRate - 字节率 = 采样率 * 每个采样的字节数
        out.write(shortToByteArray((short) blockAlign));    // BlockAlign - 数据块对齐单位，每次采样的字节数
        out.write(shortToByteArray((short) bitsPerSample)); // BitsPerSample - 每个采样的比特数，例如16位

        // data子块
        out.write(new byte[]{'d', 'a', 't', 'a'}); // Subchunk2ID - 子块2的标识为"data"，表示接下来是音频数据
        out.write(intToByteArray(subChunk2Size));   // SubChunk2Size - 子块2的大小，即PCM数据的实际大小
    }

    private byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value & 0xff),
                (byte) ((value >> 8) & 0xff),
                (byte) ((value >> 16) & 0xff),
                (byte) ((value >> 24) & 0xff),
        };
    }

    private byte[] shortToByteArray(short value) {
        return new byte[]{
                (byte) (value & 0xff),
                (byte) ((value >> 8) & 0xff),
        };
    }
}
