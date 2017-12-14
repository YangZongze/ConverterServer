package com.golaxy.converter.convert;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.golaxy.converter.entity.frontend.ConverterResult;
import com.golaxy.converter.exception.ConvertFailException;
import com.golaxy.converter.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * 格式转换
 */
public class Converter {

	private final static Logger logger = LoggerFactory.getLogger(Converter.class);
    /** md文件最大200KB，大于此则拆分 **/
	private final static long mdMaxSize = 200 * 1024;
	/** 汉字unicode按3字节算，字符串长度=字节数/3 **/
	private final static long mdMaxLen = mdMaxSize / 3;
    /** html转md后图片变成这样，有些转出来是“![Image 1][]”，需要转换成"![]()" **/
	private final static String mdVoidImg = "![]()";
	
	/**
	 * 文档转换
	 * @param srcFile 源文件
	 * @param mdList
	 * @param imgList
	 * @return
	 * @throws ConvertFailException
	 */
	public static boolean converter(String srcFile, List<ConverterResult> mdList, List<ConverterResult> imgList) throws ConvertFailException {
		boolean state = false;
		
		if (!(new File(srcFile).exists())) {
			return state;
		}
		String srcFileName = CommonUtils.getFileNameFromAbspath(srcFile);
		String srcFileNameNoExt = srcFileName.substring(0, srcFileName.lastIndexOf("."));
		String htmlPath = srcFile.substring(0, srcFile.lastIndexOf("/")) + "/";
		String mdPath = srcFile.substring(0, srcFile.lastIndexOf("/"));
		String imagePath = htmlPath + "/image/";
		String mdFileName = srcFileNameNoExt + ".md";
		String htmlFileName = srcFileNameNoExt + ".html";
        String mdServer = GlobalVars.mdServer;
		String mdFile = mdPath + "/" + mdFileName;
		
		switch (CommonUtils.getFileExt(srcFile)) {
			case "doc":
			case "docx":
			case "ppt":
			case "pptx":
			case "pdf":
				state = doc2Html(srcFile, htmlPath) && html2Md(htmlPath+htmlFileName, mdPath);
				break;
			case "html":
			case "htm":
				state = html2Md(srcFile, mdPath);
				break;
			case "txt":
				state = txt2Md(srcFile, mdPath);
				break;
            case "md":
                state = md2Md(srcFile, mdPath);
                break;
			default:
				break;
		}	
		
		if (state && !CommonUtils.getFileExt(srcFile).equalsIgnoreCase("txt")) {
			// 当前路径下新建image文件夹
			CommonUtils.mkdir(imagePath);
			
			String htmlFile = htmlPath + "/" + htmlFileName;
			//图片
			//从html中按顺序拿到图片 再 按顺序写入到md中
			List<String> imageSrcList = CommonUtils.getImgSrcFromHtml(htmlFile);
			String mdStr = null;
			try {
				mdStr = CommonUtils.read(mdFile, CommonUtils.getFileEncode(mdFile));
			} catch (IOException e) {
				e.printStackTrace();
				throw new ConvertFailException(e.getMessage());
			}
			// 有些转出来是“![Image 1][]”，需要转换成"![]()"
			StringBuffer mdBuf = new StringBuffer(mdStr.replaceAll("!\\[Image 1\\]\\[\\]", mdVoidImg));
			for (int i=0; i<imageSrcList.size(); i++) {
				String imageSrc = imageSrcList.get(i);
				String imageName = null;
				/* 
				 * 盘算src是那种类型
				 * 1.相对路径             c.png
				 * 2.base64   data:image/png;base64,iVBOR......
				 * 3.绝对路径URL http://aa/bb/c.jpg
				 */
				switch (imageSrc.substring(0, 5)) {
					case "data:":   //base64
						//最后一张是开源转换工具的logo
						if ((i+1) == imageSrcList.size()) { 
							int index = mdBuf.indexOf(mdVoidImg);
							mdBuf.replace(index, mdBuf.length()-1, "");
							continue; 					
						}
						imageName = base64Src2Image(imageSrc, imagePath+File.separator+srcFileNameNoExt+(i+1));
						break;
					case "http:":   //URL					
						break;
					default:        //相对路径
						imageName = relativeImageSave(htmlPath+imageSrc, imagePath+File.separator+srcFileNameNoExt+(i+1));
						break;
				}
				if (imageName != null) {
					String imageAbsName = (imagePath+File.separator+imageName).replaceAll("[/]{2,}", "/");
					// 先判断是否空白图片
					boolean isEmptyImage = false;
					try {
						isEmptyImage = CommonUtils.isSimpleColorImg(imageAbsName, (float)0.99);
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
					if (isEmptyImage) {
						int index = mdBuf.indexOf(mdVoidImg);
						mdBuf.replace(index, index+5, "");
						CommonUtils.fileDelete(imageAbsName);
						continue;
					} 
					
					int index = mdBuf.indexOf(mdVoidImg);
                    String imageRelPath = CommonUtils.getRelativePath(GlobalVars.uploadPath, imageAbsName);
                    String imageUrl = mdServer + "/" + imageRelPath;
					mdBuf.replace(index+4, index+4, imageUrl);
                    ConverterResult img = new ConverterResult();
                    img.setName("images/" + imageName);
                    img.setPage((short)(i + 1));
                    img.setUrl(imageUrl);
                    img.setAbsolutePath(imageAbsName);
                    img.setRelativePath(imageRelPath);
                    imgList.add(img);
				}
			}
			
			/* 
			 * md表格格式调整
			 * 修正表头位置不对的问题
			 */
			List<Integer> indexes = findIndex("[^|][\n][|]", mdBuf.toString());
			for (Integer index : indexes) {
				int line1Head = mdBuf.indexOf("|", index);
				int line1Tail = mdBuf.indexOf("|\n", line1Head) +1;
				String line1 = mdBuf.substring(line1Head, line1Tail);
				
				int line2Head = mdBuf.indexOf("|", line1Tail);
				int line2Tail = mdBuf.indexOf("|\n", line2Head) +1;
				String line2 = mdBuf.substring(line2Head, line2Tail);
				
				Boolean isNeedFix = false;
				if (line1.contains("-") && !line2.contains("-"))
					isNeedFix = true;
				if (isNeedFix) {
					mdBuf.replace(line1Head, line1Tail, line2);
					mdBuf.replace(line2Head, line2Tail, line1);
				}
			}
			CommonUtils.writeStringBuffer2File(mdFile, mdBuf);
		} 
		
		//md文件
        try {
            ArrayList<String> mdSubFiles = mdSplit(mdFile);
            for (int i=0; i<mdSubFiles.size(); i++) {
                String mdAbsName = mdSubFiles.get(i);
                String mdRelName = CommonUtils.getRelativePath(GlobalVars.uploadPath, mdAbsName);
                String mdSubFileName = new File(mdAbsName).getName();

                String mdUrl = mdServer + "/" + mdRelName;

                ConverterResult md = new ConverterResult();
                md.setName(mdSubFileName);
                md.setPage((short)(i + 1));
                md.setUrl(mdUrl);
                md.setAbsolutePath(mdAbsName);
                md.setRelativePath(mdRelName);
                mdList.add(md);
            }
		} catch (IOException e) {
		    e.printStackTrace();
		    throw new ConvertFailException(e.getMessage());
        }
		
		return state;
	}

    /**
     * 字符串中查找reg正则匹配到的所有位置
     * @param reg
     * @param str
     * @return
     */
	private static List<Integer> findIndex(String reg, String str) {
        List<Integer> indexes = new ArrayList<>();
        
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(str);
         
        while(m.find())
        {
        	indexes.add(m.start());
        }
        
        return indexes;
	}
	
	/**
	 * 切割大于200K的md
	 * @param mdSrcPath
	 * @return
	 */
	private static ArrayList<String> mdSplit(String mdSrcPath) throws IOException {
		ArrayList<String> mdNames = new ArrayList<>();
		File file = new File(mdSrcPath);
		String mdSrcFileName = file.getName();
		String mdPath = mdSrcPath.substring(0, mdSrcPath.length()-mdSrcFileName.length());
		String srcFileNameNoExt = mdSrcFileName.substring(0, mdSrcFileName.lastIndexOf("."));
		long fileSize = file.length();
		if (fileSize > mdMaxSize) {
			//按200K切割md文件
            // 按字节大小切割效率太低，改为按长度切割，一个汉字utf-8按3字节算，
            //ArrayList<String> mdStrings = CommonUtils.splitByBytes(CommonUtils.read(mdSrcPath, "UTF-8"), (int)mdMaxSize);
            ArrayList<String> mdStrings = CommonUtils.splitByLen(CommonUtils.read(mdSrcPath, "UTF-8"), (int)mdMaxLen);
            for (int i=0; i<mdStrings.size(); i++) {
                String mdFileName = srcFileNameNoExt + "（第" + (i+1) + "页）" + ".md";
                String mdFile = mdPath + File.separator + mdFileName;

                //加分页链接


                CommonUtils.write(mdFile, mdStrings.get(i), "UTF-8");
                mdNames.add(mdFile);
            }

		} else {
			//小于200K不切割md文件
			mdNames.add(mdSrcPath);
		}
		return mdNames;
	}
	
	/**
	 * ppt/doc/ppt转为html
	 * @param srcFile 源文件
	 * @param htmlPath html存储路径
	 * @return
	 * @throws ConvertFailException 
	 */
	private static boolean doc2Html(String srcFile, String htmlPath) throws ConvertFailException {
		
		boolean exit = false;
		
		exit = converterUtil(srcFile, htmlPath);
            
        String fileNameExt = srcFile.substring(srcFile.lastIndexOf(File.separator));
        //String fileName = fileNameExt.substring(0, fileNameExt.lastIndexOf("."));
        String fileExt = srcFile.substring(fileNameExt.lastIndexOf(".") + 1);
        
        if (exit) {
        	// 删除转换工具生成的css/js等文件
        	File file = new File(htmlPath);
        	File[] fs = file.listFiles(new FilenameFilter() {				
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".css") || name.endsWith(".js") || name.endsWith(".outline"))
						return true;
					else
						return false;
				}
			});
        	for(File f:fs)
        		f.delete();
        	
        	logger.info("[格式转换]: "+fileExt+"-->html 成功");
        } else {
        	logger.info("[格式转换]: "+fileExt+"-->html 失败");
        	throw new ConvertFailException();
        }

        return exit;
	}
	
	/**
	 * html转为md
	 * @param htmlFile html路径
	 * @param mdPath   md存储路径
	 * @return
	 * @throws ConvertFailException 
	 */
	private static boolean html2Md(String htmlFile, String mdPath) throws ConvertFailException {
		boolean exit = false;
		
		if (!CommonUtils.fileExist(htmlFile)) {
			logger.info("[格式转换]: html-->md html:" + htmlFile+"不存在");
			throw new ConvertFailException();
		}
		//格式转换为utf-8
		String charset = CommonUtils.getHtmlCharset(htmlFile);
		if (!charset.equalsIgnoreCase("utf-8")) {
			try {
				CommonUtils.write(htmlFile, CommonUtils.read(htmlFile, charset), "UTF-8");
			} catch (Exception e) {
				//e.printStackTrace();
				logger.error(e.getMessage() + " | " + charset);
			}
		}
		
		exit = converterUtil(htmlFile, mdPath);

        if (exit) {
        	String htmlFileName = CommonUtils.getFileNameNoExt(htmlFile);
        	if (new File(mdPath+File.separator+htmlFileName+".md").exists())
        		logger.info("[格式转换]: html-->md 成功");
        	else
        		exit = false;
        } else {
        	logger.info("[格式转换]: html-->md 失败");
        }

        return exit;
	}
	
	/**
	 * txt转为md
	 * @param txtFile  txt路径
	 * @param mdPath   md存储路径
	 * @return
	 * @throws ConvertFailException 
	 */
	private static boolean txt2Md(String txtFile, String mdPath) throws ConvertFailException {
		boolean exit = false;
		
		try {
			String charSet = CommonUtils.getFileEncode(txtFile);
			if (charSet == null)
				charSet = "UTF-8";
			String content = CommonUtils.read(txtFile, charSet);
			if (!charSet.equalsIgnoreCase("utf-8")) {
				CommonUtils.fileDelete(txtFile);
				CommonUtils.write(txtFile, content, "UTF-8");
			}
			String title = CommonUtils.getFileNameNoExt(txtFile);
			String mdContent = "## " + title + "\n\n" + content;
			String mdFile = txtFile.substring(0, txtFile.lastIndexOf(".")+1) + "md";
			CommonUtils.write(mdFile, mdContent, "UTF-8");
			
			if (new File(mdFile).exists()) {
				exit = true;
        		logger.info("[格式转换]: txt-->md 成功");
			} else {
        		exit = false;
        		logger.info("[格式转换]: txt-->md 失败");
        		throw new ConvertFailException();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

        return exit;
	}

    /**
     * 上传的md做一些处理
     * @param mdFile  txt路径
     * @param mdPath   md存储路径
     * @return
     * @throws ConvertFailException
     */
    private static boolean md2Md(String mdFile, String mdPath) throws ConvertFailException {
        boolean exit = false;

        try {
            String charSet = CommonUtils.getFileEncode(mdFile);
            if (charSet!=null && !charSet.equalsIgnoreCase("utf-8")) {
                String content = CommonUtils.read(mdFile, charSet);
                CommonUtils.fileDelete(mdFile);
                CommonUtils.write(mdFile, content, "UTF-8");

                if (new File(mdFile).exists()) {
                    exit = true;
                    logger.info("[格式转换]: md-->md 成功");
                } else {
                    exit = false;
                    logger.info("[格式转换]: md-->md 失败");
                    throw new ConvertFailException();
                }
            } else {
                throw new ConvertFailException("文件格式不可读");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConvertFailException("文件读取错误");
        }

        return exit;
    }
	
	/**
	 * 调用转换工具,可以实现
	 * 1. doc/docx/ppt/pptx/pdf->html
	 * 2. html->md
	 * @param srcFile 原文件
	 * @param dstPath 转换结果存储路径
	 * @return
	 */
	private static boolean converterUtil(String srcFile, String dstPath) throws ConvertFailException {
		int exitVal = 1;
		String isOCR = "";
		
		String fileType = srcFile.substring(srcFile.lastIndexOf(".")+1);
		if (fileType.equalsIgnoreCase("pdf")) {
			// pdf需要判断是否ocr识别
			if (GlobalVars.OCR && CommonUtils.isPhotocopyPdf(srcFile))
				isOCR = " 1";
			else
				isOCR = " 0";	
		}			
		
		//调用DConverter.jar进行转换
		Runtime runtime = Runtime.getRuntime();  
        try {  
        	String cmd = "java -jar DConverter.jar "+srcFile+isOCR+" "+dstPath;
        	Process process = runtime.exec(cmd, null, new File(GlobalVars.DConverterPath));

        	StreamHandler errorStreamHandler = new StreamHandler(process.getErrorStream(), Thread.currentThread().getName()); 
            errorStreamHandler.start();
            StreamHandler outputStreamHandler = new StreamHandler(process.getInputStream(), Thread.currentThread().getName());
            outputStreamHandler.start();
            
            exitVal = process.waitFor();
        } catch (Exception e) {  
        	e.printStackTrace();
        	throw new ConvertFailException();
        } 
        
        //判断是否生成目标文件
        if (exitVal == 0) {
            String dstFileType = null;
        	switch (fileType.toLowerCase()) {
				case "doc":
				case "docx":
				case "ppt":
				case "pptx":
				case "pdf":
					dstFileType = "html";
					break;
				case "html":
				case "htm":
					dstFileType = "md";
					break;
				default:
					break;
			}
        	
        	String dstFile = srcFile.replace("."+fileType, "."+dstFileType);
        	if (CommonUtils.fileExist(dstFile))
        		exitVal = 0;
        	else 
				throw new ConvertFailException();
        }
        
        return exitVal==0 ? true : false;
	}
	
	/**
	 * htmlbase64图片的src转为图片
	 * @param imgBase64SrcStr
	 * @param fileAbsNameNoExt
	 * @return 文件名，不带路径
	 */
	private static String base64Src2Image(String imgBase64SrcStr, String fileAbsNameNoExt) {
		String imgName = null;
		String[] imgSrcBase64 = imgBase64SrcStr.split(",");
		String imgExt = imgSrcBase64[0].substring(imgSrcBase64[0].indexOf("/")+1, imgSrcBase64[0].indexOf(";"));
		String imgBase64Str = imgSrcBase64[1];
		String imgAbsName = fileAbsNameNoExt + "." + imgExt;
		
		CommonUtils.base642Image(imgBase64Str, imgAbsName);
		
		if (CommonUtils.fileExist(imgAbsName)) {
			imgName = CommonUtils.getFileNameFromAbspath(imgAbsName);
		}
		return imgName;
	}
	
	/**
	 * 相对路径图片保存
	 * @param relativeSrc
	 * @param fileAbsNameNoExt
	 * @return
	 */
	private static String relativeImageSave(String relativeSrc, String fileAbsNameNoExt) {
		String imgName = null;
		
		if (new File(relativeSrc).exists()) {
			String ext = relativeSrc.substring(relativeSrc.lastIndexOf("."));
			if (CommonUtils.imageCut(relativeSrc, fileAbsNameNoExt + ext)) {
				imgName = CommonUtils.getFileNameFromAbspath(fileAbsNameNoExt + ext);
			}
		}
		
		return imgName;
	}
}
