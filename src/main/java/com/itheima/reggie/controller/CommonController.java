package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import com.itheima.reggie.utils.QiniuUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件的上传和下载
 * */
@RestController
@RequestMapping("common")
@Slf4j
@Api(tags = "公共相关接口")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;
    /**
     * 文件上传
     * */
    @PostMapping("/upload")
    @ApiOperation("文件上传接口")
    public R<String[]> upload(@RequestParam("imgFile") @ApiParam(name = "imgFile",value = "上传文件",required = false) MultipartFile imgFile){
        //file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除
        log.info(imgFile.toString());

        //原始文件名
        String originalFilename = imgFile.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用UUID重新生成文件名，防止文件名重复造成文件覆盖
        String fileName = UUID.randomUUID().toString()  + suffix;

      /*  //创建一个目录对象
        File dir = new File(basePath);
        //判断dir是否存在
        if (!dir.exists()){
            //目录不存在，需要创建
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(basePath  + File.separator + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        try {
            //将文件上传到七牛云服务器
            QiniuUtils.upload2Qiniu(imgFile.getBytes(),fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info(fileName.toString());
        return R.success(new String[]{basePath + File.separator + fileName, fileName});
    }

    /**
     * 文件下载
     * */
//    @GetMapping("/download")
//    public void download(String name, HttpServletResponse response){
        /*//输入流，通过输入流读取文件内容
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + File.separator +name));

            //输出流，通过输出流将文件写回浏览器，在浏览器展示图片了
            ServletOutputStream outputStream = response.getOutputStream();
            log.info(basePath + File.separator +name);

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ( (len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/

//    }
}
