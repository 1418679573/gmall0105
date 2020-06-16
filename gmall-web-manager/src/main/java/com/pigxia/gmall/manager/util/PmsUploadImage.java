package com.pigxia.gmall.manager.util;


import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by absen on 2020/5/29 10:48
 */
public class PmsUploadImage {

    public static String uploadImage(MultipartFile file) throws IOException, MyException {
        String imageUrl = "http://192.168.153.3";

        // 加载配置文件
        String tracker = PmsUploadImage.class.getResource("/tracker.conf").getFile();
        ClientGlobal.init(tracker);
        // 通过TrackerClient 得到 TrackerServer获得StorageClient
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getTrackerServer();
        StorageClient storageClient = new StorageClient(trackerServer, null);
        //  获取文件的扩展名
        String orginalFilename = file.getOriginalFilename();
        String extName = orginalFilename.substring(orginalFilename.lastIndexOf(".") + 1);
        // 得到文件的实际上传的本地的地址
        byte[] bytes = file.getBytes();
           //   三个参数  本地文件地址   扩展名  元数据（图片的元素据）
        String[] upload_file = storageClient.upload_file(bytes, extName, null);
        // 获得在分布式的存储图片的实际路径
        for (int i = 0; i < upload_file.length; i++) {
            imageUrl += "/" + upload_file[i];
        }
        return imageUrl;
    }
}
