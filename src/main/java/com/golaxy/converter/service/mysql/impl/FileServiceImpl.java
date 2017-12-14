package com.golaxy.converter.service.mysql.impl;

import javax.annotation.Resource;

import com.golaxy.converter.dao.mysql.FileDao;
import com.golaxy.converter.entity.frontend.StatusCode;
import com.golaxy.converter.entity.mysql.File;
import com.golaxy.converter.service.mysql.IFileService;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.golaxy.converter.entity.frontend.StatusCode.MYSQL_CONVERT_FINISHED_FAILURE;
import static com.golaxy.converter.entity.frontend.StatusCode.MYSQL_CONVERT_FINISHED_SUCCESS;
import static com.golaxy.converter.entity.frontend.StatusCode.MYSQL_CONVERT_INQUEUE_FAILURE;


@Service("fileService")
public class FileServiceImpl implements IFileService {

	@Resource
	private FileDao fileDao;
	
	@Override
	public int fileAdd(String md5, String name, String path, String type) {
		
		File file = new File();
		file.setMd5(md5);
		file.setName(name);
		file.setPath(path);
		file.setType(type);
		file.setUploadTime(new Date());
        
        this.fileDao.insertSelective(file);
        
        return file.getId();
	}
	
    @Override
    public int fileSearch(String md5) {

	    int id = -1;

        File file = this.fileDao.selectByUniqueKey(md5);

        return file!=null ? file.getId() : id;
    }

    @Override
    public File getFileByUid(String md5) {

	    return this.fileDao.selectByUniqueKey(md5);
    }

    /**
     * 更新转换状态，如果出错则记录出错日志
     * @param md5
     * @param status
     * @param errMsg
     */
    @Override
    public boolean convertStatusUpdate(String md5, Integer status, String errMsg) {

	    File file = new File();

	    file.setMd5(md5);
	    file.setConvertStatus(status);
	    file.setConvertLog(errMsg);
	    switch (status) {
            case MYSQL_CONVERT_INQUEUE_FAILURE:
            case MYSQL_CONVERT_FINISHED_FAILURE:
                file.setConvertResult(StatusCode.MYSQL_CONVERT_FAILURE);
                break;
            case MYSQL_CONVERT_FINISHED_SUCCESS:
                file.setConvertResult(StatusCode.MYSQL_CONVERT_SUCCESS);
                break;
        }

	    int rows = this.fileDao.updateByUniqueKeySelective(file);

	    return rows>0? true : false;
    }

    /**
     * 更新是否已通知用户字段
     * @param md5
     * @param status
     * @return
     */
    @Override
    public boolean noticeStatusUpdate(String md5, boolean status) {

        File file = new File();

        file.setMd5(md5);
        file.setNoticeStatus(status);

        int rows = this.fileDao.updateByUniqueKeySelective(file);

        return rows>0? true : false;
    }

    /**
     * 更新swfPath字段
     * @param md5
     * @param status
     * @return
     */
    @Override
    public boolean swfPathUpdate(String md5, String swfPath) {

        File file = new File();

        file.setMd5(md5);
        file.setSwfPath(swfPath);

        int rows = this.fileDao.updateByUniqueKeySelective(file);

        return rows>0? true : false;
    }

}
