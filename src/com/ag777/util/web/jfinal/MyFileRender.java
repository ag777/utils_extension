package com.ag777.util.web.jfinal;

import java.io.File;

import com.jfinal.render.FileRender;

/**
 * jfinal实现[下载后删除文件]需求的辅助类
 * <p>
 * 需要jar包(jfinal框架):
 * <ul>
 * <li>jfinal-3.2-bin-with-src.jar</li>
 * </ul>
 * </p>
 * 
 * @author ag777
 * @version create on 2018年11月16日,last modify at 2018年11月16日
 */
public class MyFileRender extends FileRender {

	private String fileName;
	private File file;

	public MyFileRender(String fileName) {
		super(fileName);
		this.fileName = fileName;
	}

	public MyFileRender(File file) {
		super(file);
		this.file = file;
	}

	@Override
	public void render() {
		try {
			super.render();
		} finally {

			if (null != fileName) {
				file = new File(fileName);
			}

			if (null != file) {
				file.delete();
				file.deleteOnExit();
			}
		}
	}
}
