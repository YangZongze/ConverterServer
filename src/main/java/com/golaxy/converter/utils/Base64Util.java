package com.golaxy.converter.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * base64编解码
 */
public class Base64Util {

    /**
     * 将字符串s进行 base64 编码
     *
     * @param s
     * @return
     */
    public static String str2Base64(String s) {
        if (s == null) {
            return null;
        }

        return (new sun.misc.BASE64Encoder()).encode(s.getBytes());
    }

    /**
     * 将 base64 编码的字符串 s 进行解码
     *
     * @param s
     * @return
     */
    public static String base642Str(String s) {
        if (s == null) {
            return null;
        }

        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] b = decoder.decodeBuffer(s);
            return new String(b);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 文件转base64
     *
     * @param filePath 文件路径
     * @return
     */
    public static String file2Base64(String filePath) {
        // 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        byte[] data = null;

        // 读取图片字节数组
        try {
            InputStream in = new FileInputStream(filePath);
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
     * base64转文件
     * @param base64Str  base64串
     * @param filePath  目标文件路径
     */
    public static boolean base642File(String base64Str, String filePath) {
        // 对字节数组字符串进行Base64解码并生成图片
        if (base64Str == null) // 图像数据为空
            return false;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            // Base64解码
            byte[] bytes = decoder.decodeBuffer(base64Str);
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] < 0) {// 调整异常数据
                    bytes[i] += 256;
                }
            }
            // 生成图片
            OutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
