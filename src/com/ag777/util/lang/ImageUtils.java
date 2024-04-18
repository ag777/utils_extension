package com.ag777.util.lang;

import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.exception.Assert;
import com.ag777.util.lang.exception.model.ImageNotSupportException;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * 图片处理工具类
 * <p>
 * 对于原生<code>ImageIO</code>的封装
 * <a href="https://blog.csdn.net/u012454773/article/details/50735266">参考资料</a>
 * </p>
 *
 * @author ag777
 * @version create on 2018年05月08日,last modify at 2024年04月18日
 */
public class ImageUtils {
	
	public static final String PNG = "png";
	public static final String JEPG = "JPEG";	//其实就是jpg啦

	/**
     * 缓存支持的图片格式列表，以避免重复遍历
     */
	private static final String[] SUPPORTED_FORMATS;

	static {
		// 初始化缓存
		SUPPORTED_FORMATS = ImageIO.getReaderFormatNames();
	}

	private ImageUtils() {}

	/**
	 * 从指定文件路径读取图像。
	 *
	 * @param filePath 图像文件的路径。
	 * @return 返回一个BufferedImage对象，表示读取到的图像。
	 * @throws IllegalArgumentException 如果文件路径为null，抛出此异常。
	 * @throws ImageNotSupportException 如果图像格式不被支持，抛出此异常。
	 * @throws IOException 如果读取文件时发生IO错误，抛出此异常。
	 */
	public static BufferedImage read(String filePath) throws IllegalArgumentException, ImageNotSupportException, IOException {
		File file = new File(filePath);
		// 检查文件是否存在
		Assert.notExisted(file, "文件[" + filePath + "]不存在");
		return ImageIO.read(file);
	}

	/**
	 * 将渲染后的图像写入指定格式并保存到文件中。
	 *
	 * @param image 要保存的渲染后的图像对象。
	 * @param formatName 图像的格式名称（如："jpg", "png"等）。
	 * @param filePath 图像要保存的文件路径。
	 * @return 返回保存图像的文件对象。
	 * @throws IllegalArgumentException 当参数不合法时抛出该异常。
	 * @throws ImageNotSupportException 当图像格式不受支持时抛出该异常。
	 * @throws IOException 当读写发生错误时抛出该异常。
	 */
	public static File write(RenderedImage image, String formatName, String filePath) throws IllegalArgumentException, ImageNotSupportException, IOException {
	    File file = new File(filePath); // 创建文件对象
	    // 将图像写入指定格式到文件，使用FileUtils提供的方式获取输出流以确保文件正确创建和写入
	    ImageIO.write(image, formatName, FileUtils.getOutputStream(file));
	    return file; // 返回文件对象
	}

	/**
	 * 获取图片输入流的类型。
	 *
	 * @param in 图片输入流，用于识别图片格式。
	 * @return 图片格式的名称，例如"JPEG"、"GIF"等。
	 * @throws IllegalArgumentException 当输入参数不合法时抛出。
	 * @throws ImageNotSupportException 当找不到支持的图片格式读取器时抛出。
	 * @throws IOException 当读取或关闭图片输入流时发生IO异常。
	 */
	public static String getType(ImageInputStream in)  throws IllegalArgumentException, ImageNotSupportException, IOException {
	    try {
	        // 获取所有识别该图片格式的已注册读取器
	        Iterator<ImageReader> iter = ImageIO.getImageReaders(in);

	        if (!iter.hasNext()) {
	            // 如果没有找到支持的读取器，抛出图片不支持异常
	            throw new ImageNotSupportException("不支持的图片格式");
	        }

	        // 获取第一个读取器，并返回其格式名称
	        ImageReader reader = iter.next();
	        return reader.getFormatName();
	    } finally {
	        // 关闭图片输入流
	        IOUtils.close(in);
	    }
	}
	

	/**
	 * 获取指定文件的类型。
	 *
	 * @param filePath 文件的路径。
	 * @return 返回文件的类型。
	 * @throws IllegalArgumentException 当文件路径为null时抛出。
	 * @throws ImageNotSupportException 当文件类型不被支持时抛出。
	 * @throws IOException 当读取文件发生IO错误时抛出。
	 */
	public static String getType(String filePath) throws IllegalArgumentException, ImageNotSupportException, IOException {
	    ImageInputStream iis = null;
        File file = new File(filePath);
        // 检查文件是否存在
        Assert.notExisted(file, "文件[" + filePath + "]不存在");
        // 从指定的文件创建一个图像输入流
        iis = ImageIO.createImageInputStream(file);

        return getType(iis);

    }

	/**
	 * 获取BufferedImage对象的宽度和高度。
	 * @param bi BufferedImage对象，不可为null。
	 * @return 包含宽度和高度的整型数组，数组第一个元素为宽度，第二个元素为高度。
	 * @throws IllegalArgumentException 当传入的BufferedImage对象为null时抛出。
	 * @throws ImageNotSupportException 当图片格式不被支持时抛出。
	 * @throws IOException 当读取图片发生IO错误时抛出。
	 */
	public static int[] getWidthAndHeight(RenderedImage bi) throws IllegalArgumentException, ImageNotSupportException, IOException {
	    // 获取图像的宽度和高度
	    int width = bi.getWidth();
	    int height = bi.getHeight();

	    // 将宽度和高度封装成数组返回
	    return new int[]{width, height};
	}

	/**
	 * 缩放给定的BufferedImage对象。
	 *
	 * @param image 需要被缩放的BufferedImage对象。
	 * @param scale 缩放比例，为一个double类型的值。
	 * @return 缩放后的BufferedImage对象。
	 * @throws IllegalArgumentException 如果image为null或scale小于等于0。
	 */
	public static BufferedImage scale(BufferedImage image, double scale) throws IllegalArgumentException {
		// 参数校验
		if (image == null) {
			throw new IllegalArgumentException("Image cannot be null.");
		}
		if (scale <= 0) {
			throw new IllegalArgumentException("Scale must be greater than 0.");
		}

		// 计算缩放后的宽度和高度
		int newWidth = (int) (image.getWidth() * scale);
		int newHeight = (int) (image.getHeight() * scale);

		// 创建一个包含缩放变换的AffineTransform对象
		AffineTransform transform = new AffineTransform();
		transform.scale(scale, scale);

		// 创建一个应用缩放变换的AffineTransformOp对象，使用双线性插值
		AffineTransformOp scaleOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);

		// 应用缩放操作，生成新的缩放后的图像
		BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, image.getType());
		scaleOp.filter(image, scaledImage);

		return scaledImage;
	}

	/**
	 * 缩放图像，保持纵横比不变。(未测试)
	 * @param bufImg 要缩放的BufferedImage对象
	 * @param w 目标宽度,传0代表动态宽度
	 * @param h 目标高度,传0代表动态高度
	 * @return 缩放后的BufferedImage对象
	 * @throws IllegalArgumentException 当目标宽度或高度为非正值时抛出
	 * @throws ImageNotSupportException 当处理图像过程中发现图像格式不支持时抛出
	 * @throws IOException 当读取或处理图像过程中发生IO异常时抛出
	 */
	public static BufferedImage scale(BufferedImage bufImg, int w, int h)  throws IllegalArgumentException, ImageNotSupportException, IOException {
		// 检查输入参数合法性
		if(w < 0 || h < 0) {
			throw new IllegalArgumentException("目标宽度和高度必须为正数。");
		}

		// 检查原始图像的宽高，防止除以0
		if(bufImg.getWidth() == 0 || bufImg.getHeight() == 0) {
			throw new ImageNotSupportException("原始图像宽高为0，无法进行缩放。");
		}

		// 如果宽度或高度为0，则根据另一个维度和原图比例计算新的尺寸
		if(w == 0) {
			w = Math.round((bufImg.getWidth() * h * 1f) / bufImg.getHeight());
		}
		if(h == 0) {
			h = Math.round((bufImg.getHeight() * w * 1f) / bufImg.getWidth());
		}

		// 计算缩放比例
		double wr = w * 1.0 / bufImg.getWidth();
		double hr = h * 1.0 / bufImg.getHeight();

		// 使用AffineTransformOp进行图像缩放，保持纵横比不变
		AffineTransform at = AffineTransform.getScaleInstance(wr, hr);
		AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC); // 使用BICUBIC插值算法提高缩放质量

		// 执行缩放操作
		BufferedImage scaledImg = new BufferedImage(w, h, bufImg.getType());
		return ato.filter(bufImg, scaledImg);
	}

	/**
	 * 压缩图片质量并返回新的 BufferedImage
	 *
	 * @param bufferedImage 原始 BufferedImage
	 * @param quality 压缩质量，0~1，1为最高质量
	 * @return 压缩后的 BufferedImage
	 * @throws IOException 如果处理图像时发生错误
	 */
	public static BufferedImage zoomBufferedImageByQuality(RenderedImage bufferedImage, float quality) throws IOException {
		if (quality < 0 || quality > 1) {
			throw new IllegalArgumentException("Quality must be between 0.0 and 1.0");
		}

		// 获取 JPEG 格式的图片写入器
		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
		if (!iter.hasNext()) {
			throw new IOException("No JPEG writer available");
		}
		ImageWriter writer = iter.next();

		// 设置图片写入参数
		ImageWriteParam iwp = writer.getDefaultWriteParam();
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); // 设置压缩模式
		iwp.setCompressionQuality(quality); // 设置压缩质量
		iwp.setProgressiveMode(ImageWriteParam.MODE_DISABLED); // 禁用渐进式加载

		// 指定压缩时使用的色彩模式
		ColorModel colorModel = ColorModel.getRGBdefault();
		iwp.setDestinationType(new ImageTypeSpecifier(colorModel, colorModel.createCompatibleSampleModel(16, 16)));

		// 将压缩后的图片写入内存流
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			 ImageOutputStream ios = ImageIO.createImageOutputStream(byteArrayOutputStream)) {
			writer.setOutput(ios);
			writer.write(null, new IIOImage(bufferedImage, null, null), iwp);
			writer.dispose();

			// 从内存流中获取压缩后的图像数据
			try (ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
				return ImageIO.read(inputStream);
			}
		} finally {
			writer.dispose();
		}
	}

	/**
	 * 将 BufferedImage 对象转换为 PNG 格式的字节数据。
	 *
	 * @param img 要转换的 BufferedImage 对象。
	 * @return PNG 格式的图像字节数据。
	 * @throws IOException 当转换过程中发生 I/O 错误时抛出。
	 */
	public static byte[] toPngBytes(RenderedImage img) throws IOException {
		return toBytes(img, PNG);
	}

	/**
	 * 将 BufferedImage 对象转换为 JPEG 格式的字节数据。
	 *
	 * @param img 要转换的 BufferedImage 对象。
	 * @return JPEG 格式的图像字节数据。
	 * @throws IOException 当转换过程中发生 I/O 错误时抛出。
	 */
	public static byte[] toJpegBytes(RenderedImage img) throws IOException {
		return toBytes(img, JEPG);
	}

	/**
	 * 将BufferedImage图像转换为指定格式的字节流。
	 *
	 * @param img 要转换的BufferedImage图像对象。
	 * @param formatName 图像的输出格式名称（如"jpg", "png"等）。
	 * @return 指定格式的图像的字节流。
	 * @throws IOException 如果写入字节流过程中发生错误。
	 */
	public static byte[] toBytes(RenderedImage img, String formatName) throws IOException {
		// 创建一个ByteArrayOutputStream对象，用于存储图像的字节流。
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            // 将图像写入os字节流并指定格式。
            ImageIO.write(img, formatName, os);
            // 将ByteArrayOutputStream转换为字节数组并返回。
            return os.toByteArray();
        }
    }

	/**
     * 检查给定的格式是否被支持。
     *
     * @param formatName 格式名称，为一个字符串。
     * @return 返回一个布尔值，如果该格式被支持则为true，否则为false。
     */
	public static boolean isFormatSupported(String formatName) {
		// 检查给定的格式名称是否存在于支持的格式数组中
		return ListUtils.inArray(SUPPORTED_FORMATS, formatName).isPresent();
	}
    
    public static void main(String[] args) throws IOException, IllegalArgumentException, ImageNotSupportException {
		String srcPath = "e:\\ad.png";
		String destPath = "e:\\a\\ad.jpg";
		System.out.println(getType(srcPath));
		BufferedImage image = scale(read(srcPath), 2);
		write(image, JEPG, destPath);
		System.out.println(getType(destPath));
		
	}
}