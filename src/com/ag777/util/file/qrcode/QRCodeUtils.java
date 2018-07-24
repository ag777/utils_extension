package com.ag777.util.file.qrcode;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.imageio.ImageIO;

import com.ag777.util.lang.collection.MapUtils;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.HybridBinarizer;

/**
 * 有关 <code>二维码</code> 读写工具类
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>zxing-core-x.x.x.jar</li>
 * </ul>
 * </p>
 * 
 * @author ag777
 * @version create on 2018年07月24日,last modify at 2018年07月24日
 */
public class QRCodeUtils {

	private QRCodeUtils(){}
	
	/**
	 * 生成二维码图片
	 * @param content 内容
	 * @param targetPath 目标路径
	 * @param width 图片宽度
	 * @param height 图片高度
	 * @return
	 * @throws WriterException
	 * @throws IOException
	 */
	public static File encode(String content, String targetPath, int width, int height) throws WriterException, IOException {
		return new QRCodeBuilder().build(content, width, height, targetPath);
	}
	
	/**
	 * 生成二维码图片(带logo)
	 * @param content 内容
	 * @param logoPath logo图片路径
	 * @param targetPath 目标路径
	 * @param width 图片宽度
	 * @param height 图片高度
	 * @return
	 * @throws WriterException
	 * @throws IOException
	 */
	public static File encode(String content, String logoPath, String targetPath, int width, int height) throws WriterException, IOException {
		return new QRCodeBuilder().setLogo(logoPath).build(content, width, height, targetPath);
	}
	
	/**
	 * 解析二维码图片,返回result对象
	 * @param filePath 需要解析的二维码图片路径
	 * @return Result.getText() 方法可以获取二维码中的文本内容
	 * @throws NotFoundException
	 * @throws IOException
	 */
	public static Result decode(String filePath) throws NotFoundException, IOException {
		MultiFormatReader formatReader = new MultiFormatReader();
		//读取指定的二维码文件
		File  file= new File(filePath);
		BufferedImage bufferedImage =ImageIO.read(file);
		
		BinaryBitmap binaryBitmap= new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage)));
		//定义二维码参数
		Map<DecodeHintType, Object>  hints= MapUtils.newHashMap();
		hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8);
		Result result = formatReader.decode(binaryBitmap, hints);
		bufferedImage.flush();
		return result;
	}
	
	
	public static class BufferedImageLuminanceSource extends LuminanceSource {
		private final BufferedImage image;
		private final int left;
		private final int top;

		protected BufferedImageLuminanceSource(BufferedImage image) {
			this(image, 0, 0, image.getWidth(), image.getHeight());
		}

		public BufferedImageLuminanceSource(BufferedImage image, int left, int top, int width, int height) {
			super(width, height);
			int sourceWidth = image.getWidth();
			int sourceHeight = image.getHeight();
			if (left + width > sourceWidth || top + height > sourceHeight) {
				throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
			}

			for (int y = top; y < top + height; y++) {
				for (int x = left; x < left + width; x++) {
					if ((image.getRGB(x, y) & 0xFF000000) == 0) {
						image.setRGB(x, y, 0xFFFFFFFF); // = white
					}
				}
			}

			this.image = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_BYTE_GRAY);
			this.image.getGraphics().drawImage(image, 0, 0, null);
			this.left = left;
			this.top = top;
		}

		@Override
		public byte[] getMatrix() {
			int width = getWidth();
			int height = getHeight();
			int area = width * height;
			byte[] matrix = new byte[area];
			image.getRaster().getDataElements(left, top, width, height, matrix);
			return matrix;
		}

		@Override
		public byte[] getRow(int y, byte[] row) {
			if (y < 0 || y >= getHeight()) {
				throw new IllegalArgumentException("Requested row is outside the image: " + y);
			}
			int width = getWidth();
			if (row == null || row.length < width) {
				row = new byte[width];
			}
			image.getRaster().getDataElements(left, top + y, width, 1, row);
			return row;
		}

		public boolean isCropSupported() {
			return true;
		}

		public LuminanceSource crop(int left, int top, int width, int height) {
			return new BufferedImageLuminanceSource(image, this.left + left, this.top + top, width, height);
		}

		public boolean isRotateSupported() {
			return true;
		}

		public LuminanceSource rotateCounterClockwise() {
			int sourceWidth = image.getWidth();
			int sourceHeight = image.getHeight();
			AffineTransform transform = new AffineTransform(0.0, -1.0, 1.0, 0.0, 0.0, sourceWidth);
			BufferedImage rotatedImage = new BufferedImage(sourceHeight, sourceWidth, BufferedImage.TYPE_BYTE_GRAY);
			Graphics2D g = rotatedImage.createGraphics();
			g.drawImage(image, transform, null);
			g.dispose();
			int width = getWidth();
			return new BufferedImageLuminanceSource(rotatedImage, top, sourceWidth - (left + width), getHeight(),
					width);
		}
	}
}
