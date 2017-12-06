package com.golaxy.converter.convert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

public class StreamHandler extends Thread {
	
	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(StreamHandler.class);
	InputStream m_inputStream;
	String m_type;

	public StreamHandler(InputStream is, String type)
    {
        this.m_inputStream = is;
        this.m_type = type;
    }

	@Override
	public void run() {
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            //设置编码方式，否则输出中文时容易乱码
            isr = new InputStreamReader(m_inputStream, "UTF-8");
            br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null) {
                //System.out.println("PRINT > " + m_type + " : " + line);
                logger.info(m_type + " : " + line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                br.close();
                isr.close();
            } catch (IOException ex) {
                Logger.getLogger(StreamHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
	}
}
