package com.ag777.util.file.qrcode;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.ImageUtils;
import com.ag777.util.lang.collection.MapUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * 有关 <code>二维码</code> 构建工具类
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
public class QRCodeBuilder {
	private static final int BLACK = 0xFF000000;//用于设置图案的颜色  
    private static final int WHITE = 0xFFFFFFFF; //用于背景色  
	
	private Map<EncodeHintType, Object> hints;
	private BufferedImage logo;
	
	public QRCodeBuilder() {
		hints = MapUtils.newHashMap();
		// 指定纠错等级,纠错级别（L 7%、M 15%、Q 25%、H 30%）
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		// 内容所使用字符集编码
		hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8);
		// hints.put(EncodeHintType.MAX_SIZE, 350);//设置图片的最大值
		// hints.put(EncodeHintType.MIN_SIZE, 100);//设置图片的最小值
		hints.put(EncodeHintType.MARGIN, 1);// 设置二维码边的空度，非负数
	}
	
	public QRCodeBuilder setLogo(String filePath) throws IOException {
		return setLogo(new File(filePath));
	}
	
	public QRCodeBuilder setLogo(File file) throws IOException {
		logo = ImageIO.read(file);
		return this;
	}
	
	public QRCodeBuilder setLogo(InputStream input) throws IOException {
		logo = ImageIO.read(input);
		return this;
	}
	
	public QRCodeBuilder addLogo(ImageInputStream input) throws IOException {
		logo = ImageIO.read(input);
		return this;
	}
	
	
	
	/**
	 * 保存成png格式的二维码图片
	 * @param content 内容
	 * @param width  二维码宽度
	 * @param height 二维码高度
	 * @param filePath 目标路径(存放二维码的路径)
	 * @return
	 * @throws WriterException
	 * @throws IOException
	 */
	public File build(String content, int width, int height, String filePath) throws WriterException, IOException {
		BufferedImage image = build(content, width, height);

		// 生成二维码
		File outputFile = new File(filePath);// 指定输出路径

		writeToFile(image, ImageUtils.PNG, outputFile);
		return outputFile;
	}
	
	/**
	 * 生成BufferedImage图片(内存)
	 * @param content 内容
	 * @param width 二维码宽度
	 * @param height 二维码高度
	 * @return
	 * @throws WriterException
	 * @throws IOException
	 */
	public BufferedImage build(String content, int width, int height) throws WriterException, IOException {
		BitMatrix bitMatrix = new MultiFormatWriter().encode(content, // 要编码的内容
				// 编码类型，目前zxing支持：Aztec 2D,CODABAR 1D format,Code 39 1D,Code 93
				// 1D ,Code 128 1D,
				// Data Matrix 2D , EAN-8 1D,EAN-13 1D,ITF (Interleaved Two of
				// Five) 1D,
				// MaxiCode 2D barcode,PDF417,QR Code 2D,RSS 14,RSS
				// EXPANDED,UPC-A 1D,UPC-E 1D,UPC/EAN
				// extension,UPC_EAN_EXTENSION
				BarcodeFormat.QR_CODE, 
				width, // 条形码的宽度
				height, // 条形码的高度
				hints);// 生成条形码时的一些配置,此项可选
		BufferedImage image = toBufferedImage(bitMatrix);  
        //设置logo图标
        if(logo != null) {
        	image = setLogo(image);
        }
        return image;
	}
	
	//--MatrixToImageWriter
	private void writeToFile(BufferedImage image, String format, File file) throws IOException {  
        writeToStream(image, format, FileUtils.getOutputStream(file));
    }

    /**
     * 将BufferedImage对象写入到指定的输出流中
     * 此方法主要用于将给定格式的图像数据写入到指定的OutputStream对象中
     * 它使用ImageIO.write方法来执行实际的写入操作如果写入失败，它会抛出一个IOException
     *
     * @param image 要写入的BufferedImage对象
     * @param format 图像的格式，例如"jpg"、"png"等
     * @param stream 要写入图像数据的OutputStream对象
     * @throws IOException 如果写入图像失败
     */
    private void writeToStream(BufferedImage image, String format, OutputStream stream) throws IOException {
        // 尝试将图像写入到指定的输出流中如果返回false，表示写入操作失败
        if (!ImageIO.write(image, format, stream)) {
            // 抛出IOException，指示写入图像失败并指定图像格式
            throw new IOException("Could not write an image of format " + format);  
        }  
    }
	
	private static BufferedImage toBufferedImage(BitMatrix matrix) {  
        int width = matrix.getWidth();  
        int height = matrix.getHeight();  
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);  
        for (int x = 0; x < width; x++) {  
            for (int y = 0; y < height; y++) {  
                image.setRGB(x, y,  (matrix.get(x, y) ? BLACK : WHITE));  
//              image.setRGB(x, y,  (matrix.get(x, y) ? Color.YELLOW.getRGB() : Color.CYAN.getRGB()));  
            }  
        }  
        return image;  
    }
	
	
	/** 
     * 设置 logo
     * @param matrixImage 源二维码图片 
     * @return 返回带有logo的二维码图片 
     * @throws IOException 
     * @author Administrator sangwenhao 
     */  
     private BufferedImage setLogo(BufferedImage matrixImage) throws IOException{  
         /** 
          * 读取二维码图片，并构建绘图对象 
          */  
         Graphics2D g2 = matrixImage.createGraphics();  
           
         int matrixWidth = matrixImage.getWidth();  
         int matrixHeigh = matrixImage.getHeight();  
           
        
         //开始绘制图片  
         g2.drawImage(logo,matrixWidth/5*2,matrixHeigh/5*2, matrixWidth/5, matrixHeigh/5, null);//绘制
             
         BasicStroke stroke = new BasicStroke(5,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);   
         g2.setStroke(stroke);// 设置笔画对象  
         //指定弧度的圆角矩形  
         RoundRectangle2D.Float round = new RoundRectangle2D.Float(matrixWidth/5*2, matrixHeigh/5*2, matrixWidth/5, matrixHeigh/5,20,20);  
         g2.setColor(Color.white);  
         g2.draw(round);// 绘制圆弧矩形  
           
         //设置logo 有一道灰色边框  
         BasicStroke stroke2 = new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);   
         g2.setStroke(stroke2);// 设置笔画对象  
         RoundRectangle2D.Float round2 = new RoundRectangle2D.Float(matrixWidth/5*2+2, matrixHeigh/5*2+2, matrixWidth/5-4, matrixHeigh/5-4,20,20);  
         g2.setColor(new Color(128,128,128));  
         g2.draw(round2);// 绘制圆弧矩形  
           
         g2.dispose();  
         matrixImage.flush() ;  
         return matrixImage ;  
     } 
     
}
