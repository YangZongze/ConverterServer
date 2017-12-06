package com.golaxy.converter.upload;

import com.golaxy.converter.utils.JackJsonUtils;
import com.golaxy.converter.utils.NumUtil;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * 文件上传进度实体类
 */
public class Progress {

	/** 已读字节 **/
	private long bytesRead = 0L;
	/** 已读MB **/
	private String mbRead = "0";
	/** 总长度 **/
	private long contentLength = 0L;
	/****/
	private int items;
	/** 已读百分比 **/
	private String percent;
	/** 读取速度 **/
	private String speed;
	/** 实时速度 **/
	private String realSpeed;
	/** 开始读取的时间 **/
	private long startReatTime = System.currentTimeMillis();

	public long getBytesRead() {
		return bytesRead;
	}

	public void setBytesRead(long bytesRead) {
		this.bytesRead = bytesRead;
	}

	public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public int getItems() {
		return items;
	}

	public void setItems(int items) {
		this.items = items;
	}

	public String getPercent() {
		percent = NumUtil.getPercent(bytesRead, contentLength);
		return percent;
	}

	public void setPercent(String percent) {
		this.percent = percent;
	}

	public String getSpeed() {
		speed = NumUtil.divideNumber(
								NumUtil.divideNumber(bytesRead * 1000, System.currentTimeMillis()- startReatTime), 
								1000);
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}
	
	public String getRealSpeed() {
		return realSpeed;
		//System.cu
	}

	public void setRealSpeed(String realSpeed) {
		this.realSpeed = realSpeed;
	}

	public long getStartReatTime() {
		return startReatTime;
	}

	public void setStartReatTime(long startReatTime) {
		this.startReatTime = startReatTime;
	}

	public String getMbRead() {
		mbRead = NumUtil.divideNumber(bytesRead, 1000000);
		return mbRead;
	}

	public void setMbRead(String mbRead) {
		this.mbRead = mbRead;
	}

	@Override
	public String toString() {
		//return JsonUtil.getJsonString4JavaPOJO(this);
		return JackJsonUtils.toJson(this);
	}
}
