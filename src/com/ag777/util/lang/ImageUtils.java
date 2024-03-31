package com.ag777.util.lang;

import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.exception.Assert;
import com.ag777.util.lang.exception.model.ImageNotSupportException;
import com.ag777.util.lang.exception.model.ValidateException;
import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.imageio.plugins.gif.GIFImageReader;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;
import com.sun.imageio.plugins.png.PNGImageWriter;
import com.sun.imageio.plugins.png.PNGImageWriterSpi;

import javax.imageio.*;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.Iterator;

/**
 * 图片处理工具类
 * <p>
 * 对于原生<code>ImageIO</code>的封装
 * <a href="https://blog.csdn.net/u012454773/article/details/50735266">参考资料</a>
 * </p>
 *
 * @author ag777
 * @version create on 2018年05月08日,last modify at 2024年03月31日
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
	 * 获取图片格式
	 * @param in 用完会关闭流
	 * @return
	 * @throws IllegalArgumentException IllegalArgumentException
	 * @throws ImageNotSupportException ImageNotSupportException
	 * @throws IOException IOException
	 */
	public static String getType(ImageInputStream in)  throws IllegalArgumentException, ImageNotSupportException, IOException {
		try {
			// get all currently registered readers that recognize the image format
	        Iterator<ImageReader> iter = ImageIO.getImageReaders(in);
	 
	        if (!iter.hasNext()) {
	            throw new ImageNotSupportException("不支持的图片格式");
	        }
	 
	        // get the first reader
	        ImageReader reader = iter.next();
	        return reader.getFormatName();
		} catch (IOException ex) {
			 throw ex;
		} finally {
			// close stream
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
	public static int[] getWidthAndHeight(BufferedImage bi) throws IllegalArgumentException, ImageNotSupportException, IOException {
	    // 获取图像的宽度和高度
	    int width = bi.getWidth();
	    int height = bi.getHeight();

	    // 将宽度和高度封装成数组返回
	    return new int[]{width, height};
	}
	

    /**
     * 获取指定图片文件的宽度和高度。
     *
     * @param filePath 图片文件的路径。
     * @return 包含图片宽度和高度的整型数组，数组第一个元素为宽度，第二个元素为高度。
     * @throws IllegalArgumentException 当文件路径为null或空字符串时抛出。
     * @throws ImageNotSupportException 当图片格式不被支持时抛出。
     * @throws IOException 当读取图片文件发生IO错误时抛出。
     */
    public static int[] getWidthAndHeight(String filePath) throws IllegalArgumentException, ImageNotSupportException, IOException {
        BufferedImage bi;
        File file = new File(filePath);
        // 检查文件是否存在
        Assert.notExisted(file, "文件[" + filePath + "]不存在");
        bi = ImageIO.read(file);  // 尝试读取文件为BufferedImage对象
        if (bi == null) {
            // 如果读取的对象为null，抛出图片格式不支持异常
            throw new ImageNotSupportException("不支持的图片格式:" + filePath);
        }

        // 返回图片的宽度和高度
        return getWidthAndHeight(bi);

    }
	

	/**
	 * 图片转换方法
	 * 将指定路径的图片转换成另一种格式并保存到目标路径
	 *
	 * @param srcPath 源图片文件路径
	 * @param destPath 目标图片文件路径
	 * @param type 要转换的图片格式（如："jpg", "png"）
	 * @return 返回转换后的图片文件对象
	 * @throws IllegalArgumentException 当参数不合法时抛出
	 * @throws ImageNotSupportException 当图片格式不被支持时抛出
	 * @throws IOException 当读取或写入图片发生IO错误时抛出
	 */
	public static File transform(String srcPath, String destPath, String type) throws IllegalArgumentException, ImageNotSupportException, IOException {
	    BufferedImage bi;
	    try {
	        File file = new File(srcPath);
	        // 检查源文件是否存在
	        Assert.notExisted(file, "文件[" + srcPath + "]不存在");
	        bi = ImageIO.read(file);
	        // 检查是否成功读取图片
	        if (bi == null) {
	             throw new ImageNotSupportException("不支持的图片格式:" + srcPath);
	        }

	        // 创建目标文件的父目录
	        File f2 = new File(destPath);
	        f2.getParentFile().mkdirs();
	        // 将图片写入目标路径
	        ImageIO.write(bi, type, f2);
	        return new File(destPath);
	    } catch (IOException ex) {
	        // 重新抛出IO异常
	        throw ex;
	    }
    }
	
	/**
	 * 图片裁剪
	 * <p>
	 * Java Image I/O API 提供了为编写复 杂程序的能力。  
     * 为了利用API的高级特性，应用程序应当直接使用类ImageReader和 ImageWriter读写图片  
	 * </p>
	 * 
	 * @param srcPath srcPath
	 * @param destPath destPath
	 * @param imgType imgType
	 * @param helper helper
	 * @throws IllegalArgumentException IllegalArgumentException
	 * @throws ImageNotSupportException ImageNotSupportException
	 * @throws IOException IOException
	 */
    public static File complexRWImage(String srcPath, String destPath, String imgType, ComplexHelper helper) throws IllegalArgumentException, ImageNotSupportException, IOException {  
    	ImageInputStream is = null;
    	ImageOutputStream os=null;
    	try {  
            /**********************读取图片*********************************/  
            File file = new File(srcPath); 
            Assert.notExisted(file, "文件["+srcPath+"]不存在");
            is = ImageIO.createImageInputStream(file);
			// 网上不是这么写的，感觉这样用了两次is,当时网上的写法需要指定格式
            Iterator<ImageReader> iter = ImageIO.getImageReaders(is);
            if (!iter.hasNext()) {
	            throw new ImageNotSupportException("不支持的图片格式:"+srcPath);
	        }
            ImageReader reader = iter.next();  

            /*  
             * 一旦有了输入源，可以把它与一个ImageReader对象关联起来.  
             * 如果输入源文件包含多张图片，而程序不保证按顺序读取时，第二个参数应该设置为 false。  
             * 对于那些只允许存储一张图片的文件格式，永远传递true是合理的  
            */  
            reader.setInput(is, true);
            
            /*  
             * 如果需要更多的控制，可以向read()方法传递一个ImageReadParam类型的参数。  
             * 一个 ImageReadParam对象可以让程序更好的利用内存。  
             * 它不仅允许指定一个感兴趣的区域，还 可以指定一个抽样因子，用于向下采样.  
             * */  
            ImageReadParam param=reader.getDefaultReadParam();  
            int imageIndex=0;  
            int width = reader.getWidth(imageIndex);
            int height = reader.getHeight(imageIndex);
            Rectangle rectangle=helper.getRectangle(width, height);
            param.setSourceRegion(rectangle);  
            BufferedImage bi=reader.read(0, param);  
              
            /**********************写图片*********************************/
            Iterator<ImageWriter> writes=ImageIO.getImageWritersByFormatName(imgType);  
            ImageWriter imageWriter=writes.next();
            file=new File(destPath);
            file.getParentFile().mkdirs();
            os=ImageIO.createImageOutputStream(file);  
            imageWriter.setOutput(os);  
            imageWriter.write(bi);
            return new File(destPath);
        } finally {
        	IOUtils.close(is);
        	IOUtils.close(os);
        }
    } 
    
    /**
     * 缩放至固定大小(未测试)
     * <p>
     * 宽高不能同时为0，其中一项为0代表该项为按比例缩放
     * </p>
     * 
     * @param srcPath srcPath
     * @param destPath destPath
     * @param w 传0则需要固定高度,等比缩放
     * @param h 传0则需要固定宽度,等比缩放
     * @throws IllegalArgumentException IllegalArgumentException
     * @throws ImageNotSupportException ImageNotSupportException
     * @throws IOException IOException
     */
    public static void scare(String srcPath, String destPath, int w, int h)  throws IllegalArgumentException, ImageNotSupportException, IOException {
        
        double wr=0,hr=0;
        File srcFile = new File(srcPath);
        Assert.notExisted(srcFile, "文件["+srcPath+"]不存在");
        File destFile = new File(destPath);

        BufferedImage bufImg = ImageIO.read(srcFile); //读取图片
        if(bufImg == null) {
			 throw new ImageNotSupportException("不支持的图片格式:"+srcPath);
		}
        
        if(w ==0 || h==0) {
        	int width = bufImg.getWidth();
        	int height = bufImg.getHeight();
        	if(w == 0) {	//计算宽度,四舍五入
        		w = Math.round((width * h * 1f)/height);
        	} else if(h == 0) {	//计算高度,四舍五入
        		h = Math.round((height * w * 1f)/width);
        	}
        }  
        
		Image Itemp = bufImg.getScaledInstance(w, h, BufferedImage.SCALE_SMOOTH);//设置缩放目标图片模板
        
        wr=w*1.0/bufImg.getWidth();     //获取缩放比例
        hr=h*1.0 / bufImg.getHeight();

        AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(wr, hr), null);
        Itemp = ato.filter(bufImg, null);
        destFile.getParentFile().mkdirs();
        ImageIO.write((BufferedImage) Itemp,destPath.substring(destPath.lastIndexOf(".")+1), destFile); //写入缩减后的图片
    }
    
    /**
     * 压缩图片质量
     * <p>
     * <a href="http://www.ibooker.cc/article/109/detail">代码地址</a>
     * </p>
     *
     * @param bufferedImage bufferedImage
     * @param targetPath targetPath
     * @param quality quality
     * @throws IOException IOException
     */
    public static void zoomBufferedImageByQuality(BufferedImage bufferedImage, String targetPath, float quality) throws IOException {
		// 得到指定Format图片的writer
		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");// 得到迭代器
		ImageWriter writer = iter.next(); // 得到writer

		// 得到指定writer的输出参数设置(ImageWriteParam)
		ImageWriteParam iwp = writer.getDefaultWriteParam();
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); // 设置可否压缩
		iwp.setCompressionQuality(quality); // 设置压缩质量参数，0~1，1为最高质量
		iwp.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
		ColorModel colorModel = ColorModel.getRGBdefault();
		// 指定压缩时使用的色彩模式
		iwp.setDestinationType(new ImageTypeSpecifier(colorModel, colorModel.createCompatibleSampleModel(16, 16)));
		// 开始打包图片，写入byte[]
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); // 取得内存输出流
		IIOImage iIamge = new IIOImage(bufferedImage, null, null);
		// 此处因为ImageWriter中用来接收write信息的output要求必须是ImageOutput
		// 通过ImageIo中的静态方法，得到byteArrayOutputStream的ImageOutput
		writer.setOutput(ImageIO.createImageOutputStream(byteArrayOutputStream));
		writer.write(null, iIamge, iwp);

		// 获取压缩后的btye
		byte[] tempByte = byteArrayOutputStream.toByteArray();
		// 创建输出文件，outputPath输出文件路径，imgStyle目标文件格式（png）
		File outFile = new File(targetPath);
		FileOutputStream fos = new FileOutputStream(outFile);
		try {
			fos.write(tempByte);
		} finally {
			IOUtils.close(fos);
		}
	}
    
    /**
	 * 解析gif文件的每一帧到特定文件夹下(导出格式为png)
	 * <p>
	 * 实测导出的图片会失真
	 * </p>
	 * @param gifPath gifPath
	 * @param targetDir targetDir
	 * @throws FileNotFoundException FileNotFoundException
	 * @throws IOException IOException
	 */
    @Deprecated
	public static void splitGif(String gifPath, String targetDir) throws FileNotFoundException, IOException {
		splitGif(getFileImageInputStream(gifPath), targetDir);
	}
	
	/**
	 * 解析gif文件的每一帧到特定文件夹下(导出格式为png)
	 * <p>
	 * 实测导出的图片会失真
	 * </p>
	 * @param in in
	 * @param targetDir targetDir
	 * @throws FileNotFoundException FileNotFoundException
	 * @throws IOException IOException
	 */
	@Deprecated
	public static void splitGif(FileImageInputStream in, String targetDir) throws FileNotFoundException, IOException {
		try {
			FileImageOutputStream out = null;
			//加载gif解析工具
			ImageReaderSpi readerSpi = new GIFImageReaderSpi();
			GIFImageReader gifReader = (GIFImageReader) readerSpi.createReaderInstance();
			gifReader.setInput(in);
			
			//创建输出路径
			new File(targetDir).mkdirs();
			
			//解析每一帧
			int num = gifReader.getNumImages(true);
			
			ImageWriterSpi writerSpi = new PNGImageWriterSpi();
			PNGImageWriter writer = (PNGImageWriter) writerSpi.createWriterInstance();
			for (int i = 0; i < num; i++) {
				String targetPath = StringUtils.concat(targetDir, i, ".png");
				try {
					out = getFileImageOutputStream(targetPath);
					writer.setOutput(out);
					// 读取读取帧的图片
					writer.write(gifReader.read(i));
				} finally {
					IOUtils.close(out);
				}
				
			}
		} finally {
			IOUtils.close(in);
		}

	}
	
	/**
     * BufferedImage转byte数组
     * <p>
     * <a href="https://zhoupuyue.iteye.com/blog/780315">参考文章</a>
     * </p>
     * @param img img
     * @return 返回图像转换后的字节数组。
     * @throws ImageFormatException ImageFormatException
     * @throws IOException IOException
     */
	public static byte[] toBytes(BufferedImage img) throws ImageFormatException, IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();  
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);  
        encoder.encode(img);  
        return os.toByteArray();  
	}
	
	/**
	 * 获取指定图像路径的 FileImageInputStream 对象。
	 *
	 * @param imgPath 图像文件的路径。
	 * @return 返回一个 FileImageInputStream 对象，用于读取指定图像文件。
	 * @throws FileNotFoundException 当指定的文件不存在时抛出。
	 * @throws IOException 在读取图像文件时发生输入输出异常。
	 */
	public static FileImageInputStream getFileImageInputStream(String imgPath) throws FileNotFoundException, IOException {
		return new FileImageInputStream(new File(imgPath));
	}

	/**
	 * 获取指定图像路径的 FileImageOutputStream 对象。
	 *
	 * @param imgPath 图像文件的路径。
	 * @return 返回一个 FileImageOutputStream 对象，用于写入指定图像文件。
	 * @throws FileNotFoundException 当指定的文件不存在时抛出。
	 * @throws IOException 在写入图像文件时发生输入输出异常。
	 */
	public static FileImageOutputStream getFileImageOutputStream(String imgPath) throws FileNotFoundException, IOException {
		return new FileImageOutputStream(new File(imgPath));
	}

	/**
     * 将RenderedImage对象转换为PNG格式的字节数组。
     *
     * @param image 要转换的渲染图像对象。
     * @return 转换后的PNG格式图像的字节数组。
     * @throws ValidateException 如果转换过程中出现验证错误，则抛出此异常。
     */
	public static byte[] png2Bytes(RenderedImage image) throws ValidateException {
		return toBytes(image, "png");
		// 调用toBytes方法，将图像以PNG格式转为字节数组
	}

	/**
     * 将渲染后的图像转换为指定格式的字节数组。
     *
     * @param image      渲染后的图像对象，不可为null。
     * @param formatName 图像的格式名称（如"jpg", "png"等），必须是受支持的格式。
     * @return 返回图像转换后的字节数组。
     * @throws ValidateException 如果输入的图像为null或指定的格式不受支持，或者转换过程中发生异常，则抛出此异常。
     */
	public static byte[] toBytes(RenderedImage image, String formatName) throws ValidateException {
		// 检查输入的图像对象是否为null
		if (image == null) {
			throw new ValidateException("Image cannot be null.");
		}

		// 检查指定的格式是否受支持
		if (!isFormatSupported(formatName)) {
			throw new ValidateException("Format " + formatName + " is not supported.");
		}

		// 使用ByteArrayOutputStream来捕获图像数据
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			// 将捕获的图像写入到ByteArrayOutputStream
			try {
				ImageIO.write(image, formatName, baos);
			} catch (IOException e) {
				// 当捕获到异常时，提供更多关于失败原因的上下文信息
				throw new ValidateException("图片转二进制异常: 格式 " + formatName + " 可能不被支持或存在其他写入问题", e);
			}
			// 将ByteArrayOutputStream转换为字节数组并返回
			return baos.toByteArray();
		} catch (Exception e) {
			// 虽然在当前场景下，try-with-resources不会抛出Exception，但为了代码的健壮性可以加上这一步
			throw new ValidateException("图片处理过程中发生未知异常", e);
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
		transform(srcPath, destPath, JEPG);
		System.out.println(getType(destPath));
//		complexRWImage(srcPath, destPath, JEPG, (width,height)->{
//			return new Rectangle(0, 0, width, height/2);
//		});
		
	}
    
    /**
     * 裁剪辅助类
     * @author ag777
     *
     */
    public interface ComplexHelper {
    	Rectangle getRectangle(int width, int height);
    }
}