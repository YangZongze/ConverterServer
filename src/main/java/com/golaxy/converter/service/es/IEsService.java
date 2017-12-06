package com.golaxy.converter.service.es;


import com.golaxy.converter.entity.es.ESSetData;
import org.apache.http.NameValuePair;

import java.util.List;

public interface IEsService {
	
	public void esAdd(ESSetData esSetData) throws Exception;
	
	public void esGet(List<NameValuePair> params);
	
	public void esUpdate(ESSetData esSetData);

}

