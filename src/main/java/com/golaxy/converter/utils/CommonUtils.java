package com.golaxy.converter.utils;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * 常用工具类
 */
public class CommonUtils {

	/**
	 * 生成唯一uid
	 * @return
	 */
	public static String getUniqueId() {
		UUID uuid = UUID.randomUUID();
		String str = uuid.toString();
		String uuidStr = str.replace("-", "");
		return uuidStr; 
	}
	
	/**
	 * 生成yyyyMMdd格式的日期
	 * @return
	 */
	public static String getStringDate() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		String dateString = formatter.format(currentTime);
		return dateString;
	}
	
	/**
	 * 生成yyyy-MM-dd HH:mm:ss格式的日期时间
	 * @return
	 */
	public static String getStringDateTime() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}
	
	/** 
     * 将时间转换为时间戳
     */    
    public static String dateToStamp(String dateTime){
        String res = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
		try {
			date = simpleDateFormat.parse(dateTime);
			long ts = date.getTime();
	        res = String.valueOf(ts);
		} catch (ParseException e) {
			e.printStackTrace();
		}
        
        return res;
    }
    
    /** 
     * 将时间戳转换为时间
     */
    public static String stampToDate(long timestamp){
        String res = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(timestamp);
        res = simpleDateFormat.format(date);
        return res;
    }
	
	/**
	 * 创建空文件夹,如果存在则清空
	 * @param path
	 * @return
	 */
	public static boolean mkdir(String path) {
		File dir = new File(path);
		if (dir.exists()) {
			if ( !dir.delete() )
				return false;
		}
		if ( dir.mkdir() ) {
			return true;
		}
		return false;
	}
	
	/**
	 * 判断某个路径下是否存在图片
	 * @param path
	 * @return
	 */
	public static boolean imageExist(String path) {
		
		File file = new File(path);
		String[] fileList = file.list();
		for (String fileName: fileList) {
			// 扩展名
			String ext = fileName.substring(fileName.lastIndexOf(".")+1);
			if (ext.equalsIgnoreCase("jpg") ||
					ext.equalsIgnoreCase("png") ||
					ext.equalsIgnoreCase("gif") ||
					ext.equalsIgnoreCase("bmp")) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 获取某个路径下的所有文件
	 * @param path
	 * @return 包含绝对路径的文件List
	 */
	public static List<String> getFileFromPath(String path) {
		
		List<String> imageList = new ArrayList<>();
		
		File file = new File(path);
		if ( file.exists() ) {
			String[] fileList = file.list();
			for (String fileName: fileList) {
				imageList.add(path + "/" +fileName);
			}
		}
		
		return imageList;
	}
	
	/**
	 * 获取某个路径下的所有图片
	 * @param path
	 * @return 包含绝对路径的图片List
	 */
	public static List<String> getImageFromPath(String path) {
		
		List<String> imageList = new ArrayList<>();
		
		File file = new File(path);
		String[] fileList = file.list();
		for (String fileName: fileList) {
			// 扩展名
			String ext = fileName.substring(fileName.lastIndexOf(".")+1);
			if (ext.equalsIgnoreCase("jpg") ||
					ext.equalsIgnoreCase("png") ||
					ext.equalsIgnoreCase("gif") ||
					ext.equalsIgnoreCase("bmp")) {
				imageList.add(path + "/" +fileName);
			}
		}
		return imageList;
	}
	
	/**
	 * 复制图片
	 * @param src
	 * @param dst
	 * @throws IOException 
	 * @return 成功true 失败false
	 */
	public static boolean imageCopy(String src, String dst) {
		try {
			FileInputStream fis = new FileInputStream(src);
			FileOutputStream fos = new FileOutputStream(dst);
			
			BufferedInputStream bufis = new BufferedInputStream(fis);
			BufferedOutputStream bufos = new BufferedOutputStream(fos);
			
			int by = 0;
			while ( (by=bufis.read()) != -1 ) {
				bufos.write(by);
				
			}
			bufos.flush();
			fos.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileExist(dst);
	}
	
	/**
	 * 图片剪切
	 * @param src
	 * @param dst
	 * @return 成功true 失败false
	 * @throws IOException
	 */
	public static boolean imageCut(String src, String dst) {
		
		if (imageCopy(src, dst)) 
			return fileDelete(src);
		
		return false;	
	}
	
	/**
	 * 文件删除
	 * @param fileName
	 * @return 成功true 失败false
	 */
	public static boolean fileDelete(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                //System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                //System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            //System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }
	
	/**
	 * 删除文件夹及其下的所有文件
	 * @param dir
	 * @return
	 */
	public static boolean dirDelete(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = dirDelete(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }
	
	/**
	 * 判断文件是否存在
	 * @param fileName
	 * @return 存在true 不存在false
	 */
	public static boolean fileExist(String fileName) {
		File file = new File(fileName);

        if (file.exists() && file.isFile()) {
        	return true;
        }
        
        return false;
	}
	
	/**
	 * 从文件绝对路径中提取文件名
	 * @param AbsolutePath
	 * @return 文件名
	 */
	public static String getFileNameFromAbspath(String AbsolutePath) {
		File file = new File(AbsolutePath);

        if (file.exists() && file.isFile()) {
        	return file.getName();
        }
		
		return null;
	}
	
	/**
	 * 从html页面中读取所有图片链接
	 * @param htmlFile html文件绝对地址
	 * @return 所有图片链接list
	 */
	public static List<String> getImgSrcFromHtml(String htmlFile) {
		List<String> imgSrcList = new ArrayList<>();
		
		File file = new File(htmlFile);
		
		Document doc = null;
		try {
			doc = Jsoup.parse(file, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Elements elements = doc.select("img");	
		for (int i=0; i<elements.size(); i++) {
			try {
				String src = elements.get(i).attr("src");
				if (src.substring(0, 5).equalsIgnoreCase("data:")) {
					// base64编码的图片
					imgSrcList.add(src);
				} else {
					// 图片路径
					// 转换出的html中图片名字中文会被urlencode
					imgSrcList.add(URLDecoder.decode(src, "UTF-8"));
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return imgSrcList;
	}
	
	public static String getHtmlCharset(String htmlFile) {
		String charset = null;
		
		File file = new File(htmlFile);
		
		Document doc = null;
		try {
			doc = Jsoup.parse(file, "UTF-8");
            Elements elements = doc.select("meta");
            for (int i=0; i<elements.size(); i++) {
                charset = elements.get(i).attr("charset");
                if (charset != null)
                    break;
            }
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (charset == null) {
			charset = getFileEncode(htmlFile);
		}

		return charset;
	}
	
	/**
	 * 读取文件内容到StringBuffer
	 * @param filePath 文件绝对路径
	 * @return
	 */
	public static StringBuffer readFile2StringBuffer(String filePath) {  
        StringBuffer sb = new StringBuffer();  
        try {    
        	BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));  
            String line = null;  
            while ((line = br.readLine()) != null) {  
                sb.append(line + "\n");  
            }  
            br.close();
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return sb;  
    }  

	/**
	 * 把stringBuffer中的内容写入文件中,返回文件大小
	 * @param fileName,buffer
	 * @param buffer
	 */
	public static long writeStringBuffer2File(String fileName, StringBuffer buffer) { 
		long file_size = 0;
        try {  
            File file = new File(fileName);  
            if (file.exists()) {// 存在，则删除  
	            if (!file.delete()) {  
	                System.err.println("删除文件" + file + "失败");  
	            }  
            } 
            if (!file.exists() && file.createNewFile()) {// 创建成功，则写入文件内容  
                PrintWriter p = new PrintWriter(new FileOutputStream(file.getAbsolutePath()));  
                p.write(buffer.toString());  
                p.close();  
                file_size = file.length();
            } else {  
                System.err.println("创建文件：" + file + "失败");  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return file_size;
    } 
	
	/**
	 * 字符串按大小切割
	 * @param text
	 * @param length
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static ArrayList<String> splitByBytes(String text, int length)
            throws UnsupportedEncodingException {

		String encode="UTF-8";
        if (text == null) {
            return null;
        }
        ArrayList<String> list = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        int currentLength = 0;
        for (int i=0; i<text.toCharArray().length; i++)
        {
            char c = text.charAt(i);
            currentLength += String.valueOf(c).getBytes(encode).length;
            if (currentLength <= length) {
                sb.append(c);
            } else {
                currentLength = 0;
                currentLength += String.valueOf(c).getBytes(encode).length;
                list.add(sb.toString());
                sb.replace(0,sb.length(),"");
                sb.append(c);
            }
            if(i==text.toCharArray().length-1)
                list.add(sb.toString());
        }
        return list;
    }
	
	/**
	 * 字符串按长度切割
	 * @param text
	 * @param len
	 * @return
	 */
	public static ArrayList<String> splitByLen(String text, int len) {
		
		ArrayList<String> list = new ArrayList<String>();
		int strLen = text.length(); 
		int mdNum = (int) Math.ceil((double)strLen/len);
		int beginIndex = 0;
		for (int i=0; i<mdNum; i++) {
			if (i==0 && len>strLen) {
				len = strLen;
			} else {
				if ( (beginIndex+len)>strLen )
					len = strLen - beginIndex;
			}
			String str = text.substring(beginIndex, beginIndex+len);
			beginIndex += len;
			list.add(str);	
		}	
		return list;
	}
	
	/**
	 * 文件转base64
	 * @param imgFilePath 文件
	 * @return
	 */
	public static String file2Base64(String imgFilePath) {
		// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
		byte[] data = null;
		 
		// 读取图片字节数组
		try {
			InputStream in = new FileInputStream(imgFilePath);
			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		 
		// 对字节数组Base64编码
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(data);// 返回Base64编码过的字节数组字符串
	}
		
	/**
	 * base64转图片
	 * @param imgBase64Str  图片base64串
	 * @param imgFilePath  生成的目标图片
	 */
	public static boolean base642Image(String imgBase64Str, String imgFilePath) {
		// 对字节数组字符串进行Base64解码并生成图片
		if (imgBase64Str == null) // 图像数据为空
			return false;
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			// Base64解码
			byte[] bytes = decoder.decodeBuffer(imgBase64Str);
			for (int i = 0; i < bytes.length; ++i) {
				if (bytes[i] < 0) {// 调整异常数据
					bytes[i] += 256;
				}
			}
			// 生成图片
			OutputStream out = new FileOutputStream(imgFilePath);
			out.write(bytes);
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * 获取文件的扩展名
	 * @param fileName 绝对路径相对路径皆可
	 * @return
	 */
	public static String getFileExt(String fileName) {
		String ext = null;
		
		ext = fileName.substring(fileName.lastIndexOf(".")+1);
		
		return ext;
	}
	
	/**
	 * 获取文件名(不包含扩展名)
	 * @param fileName
	 * @return
	 */
	public static String getFileNameNoExt(String fileName) {
		String fileNameNoExt = null;
		
		String str = fileName.substring(fileName.lastIndexOf("/")+1);
		try {
			fileNameNoExt = str.substring(0, str.lastIndexOf("."));
		} catch (StringIndexOutOfBoundsException e) {
			fileNameNoExt = fileName;
		}
		
		return fileNameNoExt;
	}

	/**
	 * 判断pdf是佛印影版
	 * @param fileName
	 * @return true是  false否
	 */
	public static boolean isPhotocopyPdf(String fileName) {
		boolean is = false;
		
		File pdfFile = new File(fileName);
        PDDocument document = null;
        try
        {
            // 方式一：
            /**
            InputStream input = null;
            input = new FileInputStream( pdfFile );
            //加载 pdf 文档
            PDFParser parser = new PDFParser(new RandomAccessBuffer(input));
            parser.parse();
            document = parser.getPDDocument();
            **/

            // 方式二：
            document = PDDocument.load(pdfFile);

            // 获取页码
            int pages = document.getNumberOfPages();

            // 读文本内容
            PDFTextStripper stripper=new PDFTextStripper();
            // 设置按顺序输出
            //stripper.setSortByPosition(true);
            
            int step = pages / 3;
            int totalLength = 0;
            for (int i=0; i<3; i++) {
            	int page = 1 + i * step;
            	stripper.setStartPage(page);
                stripper.setEndPage(page);
                totalLength += stripper.getText(document).length();  
            }
            if (totalLength < 15)
            	is = true;
            else 
            	is = false;
   
        } catch(Exception e) {
            e.printStackTrace();
        } finally{
            if(document != null){
                try {
					document.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }    
		
		return is;
	} 
	
	/**
	 * 按指定编码写文件
	 * @param path
	 * @param content
	 * @param encoding
	 * @throws IOException
	 */
	public static void write(String path, String content, String encoding)  
            throws IOException {  
        File file = new File(path);  
        file.delete();  
        file.createNewFile();  
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(  
                new FileOutputStream(file), encoding));  
        writer.write(content);  
        writer.close();  
    }  
  
	/**
	 * 按指定编码读文件
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
    public static String read(String path, String encoding) throws IOException {  
        String content = "";  
        File file = new File(path);  
        BufferedReader reader = new BufferedReader(new InputStreamReader(  
                new FileInputStream(file), encoding));  
        String line = null;  
        while ((line = reader.readLine()) != null) {  
            content += line + "\n";  
        }  
        reader.close();  
        return content;  
    }
    
    /**
     * 利用第三方开源包cpdetector获取文件编码格式
     * 依赖jar包:
     * cpdetector_1.0.10.jar
     * antlr-2.7.4.jar
     * chardet-1.0.jar
     * jargs-1.0.jar
     * 
     * @param path
     *            要判断文件编码格式的源文件的路径
     * 注意: detectCodepage方法并发执行有可能导致ConcurrentModificationException一场,故此处加了synchronized同步
     */
    @SuppressWarnings("finally")
	public static synchronized String getFileEncode(String path) {
        /*
         * detector是探测器，它把探测任务交给具体的探测实现类的实例完成。
         * cpDetector内置了一些常用的探测实现类，这些探测实现类的实例可以通过add方法 加进来，如ParsingDetector、
         * JChardetFacade、ASCIIDetector、UnicodeDetector。
         * detector按照“谁最先返回非空的探测结果，就以该结果为准”的原则返回探测到的
         * 字符集编码。使用需要用到三个第三方JAR包：antlr.jar、chardet.jar和cpdetector.jar
         * cpDetector是基于统计学原理的，不保证完全正确。
         */
        CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
        /*
         * ParsingDetector可用于检查HTML、XML等文件或字符流的编码,构造方法中的参数用于
         * 指示是否显示探测过程的详细信息，为false不显示。
         */
        detector.add(new ParsingDetector(false));
        /*
         * JChardetFacade封装了由Mozilla组织提供的JChardet，它可以完成大多数文件的编码
         * 测定。所以，一般有了这个探测器就可满足大多数项目的要求，如果你还不放心，可以
         * 再多加几个探测器，比如下面的ASCIIDetector、UnicodeDetector等。
         */
        detector.add(JChardetFacade.getInstance());// 用到antlr.jar、chardet.jar
        // ASCIIDetector用于ASCII编码测定
        detector.add(ASCIIDetector.getInstance());
        // UnicodeDetector用于Unicode家族编码的测定
        detector.add(UnicodeDetector.getInstance());
        java.nio.charset.Charset charset = null;
        File f = new File(path);
        try {
            charset = detector.detectCodepage(f.toURI().toURL());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
        	if (charset != null)
                return charset.name();
            else
                return null;
		}
    }
    
    /**
     * filePath路径下的文件按时间排序
     * @param filePath
     * @param desc 是否倒叙
     */
    public static void orderByDate(String filePath, boolean desc) {
        File file = new File(filePath);
        File[] files = file.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff > 0)
                    return desc ? -1 : 1;
                else if (diff == 0)
                    return 0;
                else
                    return desc ? 1 : -1;//如果 if 中修改为 返回-1 同时此处修改为返回 1  排序就会是递减
            }

            public boolean equals(Object obj) {
                return true;
            }
        });
//        for (int i = 0; i < files.length; i++) {
//            System.out.println(files[i].getName());
//            System.out.println(new Date(files[i].lastModified()));
//        }
    }
    
    /**
     * file路径下的文件按时间排序
     * @param files
     * @param desc 是否倒叙
     */
    public static void listFileOrderByDate(File[] files, boolean desc) {
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff > 0)
                    return desc ? -1 : 1;
                else if (diff == 0)
                    return 0;
                else
                    return desc ? 1 : -1;//如果 if 中修改为 返回-1 同时此处修改为返回 1  排序就会是递减
            }

            public boolean equals(Object obj) {
                return true;
            }
        });
    }
    
    /**
     * 判断是否为纯色
     * @param imgPath 图片源
     * @param percent 纯色百分比，即大于此百分比为同一种颜色则判定为纯色,范围[0-1]
     * @return
     * @throws IOException
     */
    public static boolean isSimpleColorImg(String imgPath, float percent) throws IOException{
        BufferedImage src = ImageIO.read(new File(imgPath));
        int height = src.getHeight();
        int width  = src.getWidth();
        int count = 0, pixTemp = 0, pixel = 0;
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                pixel = src.getRGB(i, j);
                if (pixel == pixTemp) //如果上一个像素点和这个像素点颜色一样的话，就判定为同一种颜色
                    count++;
                if ((float)count/(height*width) >= percent) //如果连续相同的像素点大于设定的百分比的话，就判定为是纯色的图片 
                    return true;
                pixTemp = pixel;
            }
        }
        return false;
    }


	/**
	 * 去掉绝对路径中的系统根路径，只留下相对路径
	 * @param rootPath
	 * @param absolutePath
	 * @return
	 */
	public static String getRelativePath(String rootPath, String absolutePath) {

		String path = absolutePath.substring(rootPath.length());
		if (path.substring(0, 1).equals("/"))
			path = path.substring(1);

		return path;
	}

}
