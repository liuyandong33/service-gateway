package build.dream.gateway.controllers;

import build.dream.common.utils.*;
import com.google.zxing.WriterException;
import okhttp3.Headers;
import okhttp3.Response;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping(value = "/media")
class MediaController {
    /**
     * 生成二维码
     */
    @RequestMapping(value = "/generateQRCode")
    @ResponseBody
    public void generateQRCode() throws IOException, WriterException {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String width = requestParameters.get("width");
        if (StringUtils.isBlank(width)) {
            width = "400";
        }

        String height = requestParameters.get("height");
        if (StringUtils.isBlank(height)) {
            height = "400";
        }
        String data = requestParameters.get("data");
        HttpServletResponse httpServletResponse = ApplicationHandler.getHttpServletResponse();
        httpServletResponse.setContentType(MimeMappingUtils.obtainMimeTypeByExtension(ZXingUtils.FORMAT_NAME_PNG));
        OutputStream outputStream = httpServletResponse.getOutputStream();
        ZXingUtils.generateQRCode(Integer.parseInt(width), Integer.parseInt(height), data, outputStream);
        outputStream.close();
    }

    /**
     * 生成条码
     */
    @RequestMapping(value = "/generateBarCode")
    @ResponseBody
    public void generateBarCode() throws IOException, WriterException {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String width = requestParameters.get("width");
        if (StringUtils.isBlank(width)) {
            width = "400";
        }

        String height = requestParameters.get("height");
        if (StringUtils.isBlank(height)) {
            height = "200";
        }
        String data = requestParameters.get("data");
        HttpServletResponse httpServletResponse = ApplicationHandler.getHttpServletResponse();
        httpServletResponse.setContentType(MimeMappingUtils.obtainMimeTypeByExtension(ZXingUtils.FORMAT_NAME_PNG));
        OutputStream outputStream = httpServletResponse.getOutputStream();
        ZXingUtils.generateBarCode(Integer.parseInt(width), Integer.parseInt(height), data, outputStream);
        outputStream.close();
    }

    /**
     * 下载二维码
     */
    @RequestMapping(value = "/downloadQRCode")
    @ResponseBody
    public void downloadQRCode() throws IOException, WriterException {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String width = requestParameters.get("width");
        if (StringUtils.isBlank(width)) {
            width = "400";
        }

        String height = requestParameters.get("height");
        if (StringUtils.isBlank(height)) {
            height = "400";
        }
        String data = requestParameters.get("data");
        String fileName = requestParameters.get("fileName");
        HttpServletResponse httpServletResponse = ApplicationHandler.getHttpServletResponse();
        httpServletResponse.setContentType(MimeMappingUtils.obtainMimeTypeByExtension(ZXingUtils.FORMAT_NAME_PNG));
        httpServletResponse.setHeader("Content-disposition", "attachment;filename=" + fileName + ZXingUtils.FORMAT_NAME_PNG);
        OutputStream outputStream = httpServletResponse.getOutputStream();
        ZXingUtils.generateQRCode(Integer.parseInt(width), Integer.parseInt(height), data, outputStream);
        outputStream.close();
    }

    /**
     * 下载二维码
     */
    @RequestMapping(value = "/downloadBarCode")
    @ResponseBody
    public void downloadBarCode() throws IOException, WriterException {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String width = requestParameters.get("width");
        if (StringUtils.isBlank(width)) {
            width = "400";
        }

        String height = requestParameters.get("height");
        if (StringUtils.isBlank(height)) {
            height = "200";
        }
        String data = requestParameters.get("data");
        String fileName = requestParameters.get("fileName");
        HttpServletResponse httpServletResponse = ApplicationHandler.getHttpServletResponse();
        httpServletResponse.setContentType(MimeMappingUtils.obtainMimeTypeByExtension(ZXingUtils.FORMAT_NAME_PNG));
        httpServletResponse.setHeader("Content-disposition", "attachment;filename=" + fileName + ZXingUtils.FORMAT_NAME_PNG);
        OutputStream outputStream = httpServletResponse.getOutputStream();
        ZXingUtils.generateBarCode(Integer.parseInt(width), Integer.parseInt(height), data, outputStream);
        outputStream.close();
    }


    /**
     * 显示外部图片，绕过防盗链
     */
    @RequestMapping(value = "/doGet")
    @ResponseBody
    public ResponseEntity<byte[]> doGet() throws IOException {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String url = requestParameters.get("url");
        Response response = OutUtils.doGetNative(url);

        byte[] body = response.body().bytes();

        HttpHeaders httpHeaders = new HttpHeaders();
        Headers headers = response.headers();
        Set<String> names = headers.names();
        for (String name : names) {
            httpHeaders.addAll(name, headers.values(name));
        }

        HttpStatus httpStatus = HttpStatus.valueOf(response.code());
        OkHttpUtils.closeResponse(response);
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>(body, httpHeaders, httpStatus);
        return responseEntity;
    }
}