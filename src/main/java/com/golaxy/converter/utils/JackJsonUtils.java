package com.golaxy.converter.utils;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * json转换工具类
 */
public class JackJsonUtils {

	static ObjectMapper objectMapper;
	
	/** 
     * 解析json 
     *  
     * @param content 
     * @param valueType 
     * @return 
     */ 
	public static <T> T fromJson(String content, Class<T> valueType) {
		if (objectMapper == null) {  
            objectMapper = new ObjectMapper();
			objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
			objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        } 
		try {
			return objectMapper.readValue(content, valueType);
		} catch (Exception e) {  
            e.printStackTrace();  
        }  
        return null; 
	}

	/**
	 * 解析json为对象列表
	 *
	 * @param content
	 * @param valueTypeRef
	 * @return
	 */
	public static <T> T fromJson(String content, TypeReference valueTypeRef) {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
			objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
			objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
		}
		try {
			return objectMapper.readValue(content, valueTypeRef);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/** 
     * object生成json
     *  
     * @param object 
     * @return 
     */ 
	public static String toJson(Object object) {
		if (objectMapper == null) {  
            objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        } 
		try {
			return objectMapper.writeValueAsString(object);
		} catch (Exception e) {  
            e.printStackTrace();  
        }  
        return null;  
	}
	
	/** 
     * list生成json
     *  
     * @param list
     * @return 
     */ 
	public static String toJson(List<?> list) {
		if (objectMapper == null) {  
            objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        } 
		try {
			return objectMapper.writeValueAsString(list);
		} catch (Exception e) {  
            e.printStackTrace();  
        }  
        return null;  
	}
	
	/** 
     * map生成json
     *  
     * @param map
     * @return 
     */ 
	public static String toJson(Map<?, ?> map) {
		if (objectMapper == null) {  
            objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        } 
		try {
			return objectMapper.writeValueAsString(map);
		} catch (Exception e) {  
            e.printStackTrace();  
        }  
        return null;  
	}
}
