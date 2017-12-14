package com.golaxy.converter.utils;

import com.golaxy.converter.convert.StreamHandler;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Created by yangzongze on 2017/12/11.
 *
 * pdf转swf工具
 * 需要swftools第三插件的支持 ,支持window\linux\mac等系统
 */
public class Office2Swf {

    /**
     * 根据操作系统的名称，获取执行pdf->swf文件的命令
     *
     * @param pdfFile
     *            转换的pdf源文件路径
     * @param swfOutFilePath
     *            输出的swf文件路径
     * @return
     */
    private static String getCommand(String pdfFile, String swfOutFilePath) {
        String command = null;
        String osName = System.getProperty("os.name");
        if (null == swfOutFilePath || "".equals(swfOutFilePath.trim())) {
            swfOutFilePath = pdfFile.toLowerCase().replaceAll(".pdf", ".swf");
        }

        if (Pattern.matches("Linux.*", osName)) {
            command = "pdf2swf " + pdfFile + " -o " + swfOutFilePath + " -T 9 -f -s languagedir=/usr/local/xpdf/xpdf-chinese-simplified";
        } else if (Pattern.matches("Windows.*", osName)) {
            command = "D:/Program Files/SoftWare/work/swftools/pdf2swf.exe -t " + pdfFile + " -o " + swfOutFilePath
                    + " -T 9 -f";
        } else if (Pattern.matches("Mac.*", osName)) {
        }
        return command;
    }

    /**
     * 将pdf转换swf文件，在线预览
     *
     * @param pdfInputFilePath
     *            待转换的pdf源文件路径
     * @param swfOutFilePath
     *            输出的swf目标文件路径，如果未指定(null)，则按在源文件当前目录生成同名的swf文件
     * @return swf目标文件路径
     */
    public static String pdf2Swf(String pdfInputFilePath, String swfOutFilePath) {
        String command = getCommand(pdfInputFilePath, swfOutFilePath);
        try {
            Process process = Runtime.getRuntime().exec(command);

            StreamHandler errorStreamHandler = new StreamHandler(process.getErrorStream(), Thread.currentThread().getName());
            errorStreamHandler.start();
            StreamHandler outputStreamHandler = new StreamHandler(process.getInputStream(), Thread.currentThread().getName());
            outputStreamHandler.start();

            int exitVal = process.waitFor();

            return pdfInputFilePath.replaceAll("." + getPostfix(pdfInputFilePath), ".swf");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 将office文件直接转换为swf文件
     *
     * 如果是pdf直接轉換
     *
     * @param inputFilePath
     *            待转换的源office文件路径
     * @param outputSwfPath
     *            输出的swf目标文件路径，如果未指定(null)，则按在源文件当前目录生成同名的swf文件
     * @return swf目标文件路径
     */
    public static String office2Swf(String inputFilePath, String outputSwfPath) {
        String outputPdfPath = null;

        if ("pdf".equals(getPostfix(inputFilePath))) {
            if (null == outputSwfPath || "".equals(outputSwfPath.trim())) {
                outputSwfPath = inputFilePath.replace("." + getPostfix(inputFilePath), ".swf");
            }

            String swfName = new File(outputSwfPath).getName();
            String pdfName = new File(inputFilePath).getName();

            String new_swfName = swfName.replace("." + getPostfix(outputSwfPath), ".pdf");
            outputPdfPath = inputFilePath.replace(pdfName, new_swfName);

            File old_inputFilePath = new File(inputFilePath);
            File new_outputPdfPath = new File(outputPdfPath);

            old_inputFilePath.renameTo(new_outputPdfPath); // 把pdf重命名

            outputSwfPath = pdf2Swf(outputPdfPath, outputSwfPath);
        } else {
            if (null == outputSwfPath || "".equals(outputSwfPath.trim())) {
                outputPdfPath = inputFilePath.replace("." + getPostfix(inputFilePath), ".pdf");
            } else {
                outputPdfPath = outputSwfPath.replace("." + getPostfix(outputSwfPath), ".pdf");
            }

            boolean isSucc = Office2PDF.openOffice2Pdf(inputFilePath, outputPdfPath);

            if (isSucc) {

                outputSwfPath = pdf2Swf(outputPdfPath, outputSwfPath);
            }
        }

        return outputSwfPath;
    }

    /**
     * 获取文件的后缀名
     */
    private static String getPostfix(String inputFilePath) {
        String postfix = null;
        if (null != inputFilePath && !"".equals(inputFilePath.trim())) {
            int idx = inputFilePath.lastIndexOf(".");
            if (idx > 0) {
                postfix = inputFilePath.substring(idx + 1, inputFilePath.trim().length());
            }
        }
        return postfix;
    }

}
