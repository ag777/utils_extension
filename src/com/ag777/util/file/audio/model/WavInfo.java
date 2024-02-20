package com.ag777.util.file.audio.model;

/**
 * 用于存储WAV文件参数的简单类
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/2/20 15:00
 */
public class WavInfo {
    private final String chunkID;          // "RIFF" 标记，表示资源交换文件格式
    private final int chunkSize;           // 文件大小减去8字节（即除去chunkID和chunkSize）
    private final String format;           // "WAVE" 标记，表示WAV文件格式
    private final String subchunk1ID;      // "fmt " 标记，表示格式子块
    private final int subchunk1Size;       // 格式子块的大小（对于PCM，通常是16字节）
    private final short audioFormat;       // 音频格式，PCM为1，其他值表示压缩格式
    private final int numChannels;         // 通道数，例如1表示单声道，2表示立体声
    private final int sampleRate;          // 采样率，每秒采样次数
    private final int byteRate;            // 每秒数据字节数（= SampleRate * NumChannels * BitsPerSample / 8）
    private final short blockAlign;        // 数据块对齐单位（每个采样的字节数 = NumChannels * BitsPerSample / 8）
    private final int bitsPerSample;       // 每个采样的位数
    private final String subchunk2ID;      // "data" 标记，表示数据子块
    private final int pcmDataLength;       // PCM数据的长度（即音频数据的大小）

    public WavInfo(String chunkID, int chunkSize, String format, String subchunk1ID, int subchunk1Size,
                   short audioFormat, int numChannels, int sampleRate, int byteRate, short blockAlign,
                   int bitsPerSample, String subchunk2ID, int pcmDataLength) {
        this.chunkID = chunkID;
        this.chunkSize = chunkSize;
        this.format = format;
        this.subchunk1ID = subchunk1ID;
        this.subchunk1Size = subchunk1Size;
        this.audioFormat = audioFormat;
        this.numChannels = numChannels;
        this.sampleRate = sampleRate;
        this.byteRate = byteRate;
        this.blockAlign = blockAlign;
        this.bitsPerSample = bitsPerSample;
        this.subchunk2ID = subchunk2ID;
        this.pcmDataLength = pcmDataLength;
    }

    public String getChunkID() {
        return chunkID;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public String getFormat() {
        return format;
    }

    public String getSubchunk1ID() {
        return subchunk1ID;
    }

    public int getSubchunk1Size() {
        return subchunk1Size;
    }

    public short getAudioFormat() {
        return audioFormat;
    }

    public int getNumChannels() {
        return numChannels;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getByteRate() {
        return byteRate;
    }

    public short getBlockAlign() {
        return blockAlign;
    }

    public int getBitsPerSample() {
        return bitsPerSample;
    }

    public String getSubchunk2ID() {
        return subchunk2ID;
    }

    public int getPcmDataLength() {
        return pcmDataLength;
    }
}
