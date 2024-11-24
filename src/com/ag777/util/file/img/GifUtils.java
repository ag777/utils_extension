package com.ag777.util.file.img;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.FileImageOutputStream;

import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.ImageUtils;
import com.ag777.util.lang.StringUtils;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;
import com.sun.imageio.plugins.png.PNGImageWriter;
import com.sun.imageio.plugins.png.PNGImageWriterSpi;

/**
 * 对animated-gif-lib的二次封装
 * <p>
 * 需要jar包:
 * <ul>
 * <li>animated-gif-lib-xxx.jar</li>
 * </ul>
 * 代码参考git项目:https://github.com/guangxush/GenerateGIF
 * </p>
 * @author ag777
 * @version create on 2019年01月10日,last modify at 2019年01月10日
 */
public class GifUtils {

	private GifUtils() {}
	
	/**
	 * 拆分gif到目标文件夹
	 * @param gifPath 源gif路径
	 * @param targetDir 目标文件夹路径,末尾必须带一个文件分隔符\或/
	 * @throws IOException
	 */
	public static void split(String gifPath, String targetDir) throws IOException {
		GifDecoder decoder = new GifDecoder();
		//这里有个坑,e:/a.gif它不认，只认e:\a.gif,所以这里做一层转换
		gifPath = new File(gifPath).getAbsolutePath();
		int status = decoder.read(gifPath);// imagePath源文件路径
		if (status != GifDecoder.STATUS_OK) {
			switch(status) {
				case GifDecoder.STATUS_FORMAT_ERROR:
					throw new IOException("image " + gifPath + " 格式错误!");
				case GifDecoder.STATUS_OPEN_ERROR:
					throw new IOException("image " + gifPath + " 打开失败!");
				default:
					throw new IOException("read image " + gifPath + " error!");
			}
		   
		}
		
		new File(targetDir).mkdirs();
		int frameCount = decoder.getFrameCount();// 获取GIF有多少个frame
		FileImageOutputStream out = null;
		ImageWriterSpi writerSpi = new PNGImageWriterSpi();
	    PNGImageWriter writer = (PNGImageWriter) writerSpi.createWriterInstance();
		for (int i = 0; i < frameCount; i++) {
		    String targetPath = StringUtils.concat(targetDir, i, ".png");
			try {
				out = new FileImageOutputStream(new File(targetPath));
				writer.setOutput(out);
				// 读取读取帧的图片
				writer.write(decoder.getFrame(i));
			} finally {
				IOUtils.close(out);
			}
		}
	}
	
	/**
	 * 生成GIF图片
	 * @param files 原始PNG图片
	 * @param gifPath 是否被压缩，默认true
	 * @return
	 * @throws IOException
	 */
	public static File build(File[] files, String gifPath) throws IOException {
		BufferedImage[] images = parse(files);
		Path path = Paths.get(gifPath);

		AnimatedGifEncoder encoder = new AnimatedGifEncoder();
		// 设置循环模式，0为无限循环 这里没有使用源文件的播放次数
		encoder.setRepeat(0);

		encoder.start(new FileOutputStream(path.toFile()));
		int count = 1;
		// 采样频率，数字越大，文件越小，丢失的帧越多，设置为1可保持原帧
		int frequency = 1;
		for (BufferedImage image : images) {
			if ((++count) % frequency == 0) {
				encoder.setDelay(100 * frequency);
				encoder.addFrame(image);
			}
		}
		encoder.finish();
		return path.toFile();

	}
    
    /**
     * 把PNG素材原图转成BufferedImage
     *
     * @param files png文件
     * @return BufferedImage[]
     * @throws IOException 
     */
    private static BufferedImage[] parse(File[] files) throws IOException {
        BufferedImage[] bi = new BufferedImage[files.length];
        for (int index = 0; index < files.length; index++) {
            bi[index] = ImageIO.read(files[index]);
        }
        return bi;
    }
    
    public static void main(String[] args) throws IOException {
//    	split("f:/临时/a.gif", "f:/临时/a/b/");
		build(new File("F:\\临时\\a\\b\\").listFiles(), "F:\\临时\\b.gif");
	}
}
