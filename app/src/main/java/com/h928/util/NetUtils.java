package com.h928.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by xiechunping on 2017/6/28.
 */

public class NetUtils extends AsyncTask<String, Integer, String> {

    private final static String TAG="ST"; //调试输出标识
    private static int CONNECT_TIMEOUT=10; //设置连接主机超时（单位：秒）
    private static int READ_TIMEOUT=30; //设置从主机读取数据超时（单位：秒）

    private IRequestListener listener=null;
    private HashMap<String, String> params=null;
    private HashMap<String, String> headers=new HashMap<>();
    private String savePath=null;
    private ArrayList<String> remoteUrls=new ArrayList<>();
    private String method="GET";

    public NetUtils(){
        super();
    }
    public NetUtils(IRequestListener listener){
        super();
        this.setListener(listener);
    }

    public NetUtils(IRequestListener listener,HashMap<String, String> params){
        super();
        this.setListener(listener);
        this.setParams(params);
    }

    public NetUtils(IRequestListener listener,HashMap<String, String> params,
                    HashMap<String, String> headers)
    {
        super();
        this.setListener(listener);
        this.setParams(params);
        this.setHeaders(headers);
    }

    public void setListener(IRequestListener listener){
        this.listener=listener;
    }

    public void setHeaders(HashMap<String, String> headers){
        this.headers.putAll(headers);
    }

    public void setParams(HashMap<String, String> params){
        this.params=params;
    }

    public void setParam(String name,String value){
        if(this.params==null){
            this.params=new HashMap<>();
        }
        this.params.put(name,value);
    }

    public void setHeader(String name,String value){
        this.headers.put(name,value);
    }

    public void setSavePath(String path){
        this.savePath=path;
    }

    public void addRemoteUrl(String url){
        this.remoteUrls.add(url);
    }

    public void removeRemoteUrl(int index){
        this.remoteUrls.remove(index);
    }

    public void clearRemoteUrls(){
        this.remoteUrls.clear();
    }

    public void setMethod(String method){
        this.method=method;
    }


    /**
     *  GET请求(异步)
     *  @param remoteUrl 请求地址
     *  @param listener 监听器
     *  @return 服务器返回的字符串
     * */
    public static void get(String remoteUrl , IRequestListener listener){
        NetUtils netUtils= new NetUtils(listener);
        netUtils.addRemoteUrl(remoteUrl);
        netUtils.setMethod("GET");
        netUtils.execute();
    }

    /**
     *  POST请求(异步)
     *  @param remoteUrl 请求地址
     *  @param params 请求参数
     *  @param listener 监听器
     *  @return 服务器返回的字符串
     * */
    public static void post(String remoteUrl , HashMap<String, String> params,
                            IRequestListener listener)
    {
        NetUtils netUtils= new NetUtils(listener,params);
        netUtils.addRemoteUrl(remoteUrl);
        netUtils.setMethod("POST");
        netUtils.execute();
    }

    /**
     *  文件下载(异步)
     *  @param remoteUrl 请求地址
     *  @param savePath 保存路径
     *  @param listener 监听器
     * */
    public static void download(String remoteUrl,String savePath,IRequestListener listener){
        NetUtils netUtils= new NetUtils(listener);
        netUtils.addRemoteUrl(remoteUrl);
        netUtils.setSavePath(savePath);
        netUtils.setMethod("GET");
        netUtils.execute();
    }

    public String request(String requestUrl){
        String result="";
        //带有数据参数，则自动使用POST方法
        String cMethod=params!=null ? "POST" : (!method.equalsIgnoreCase("POST")?"GET":method.toUpperCase());
        boolean isGet=cMethod.equals("GET"); //是否为GET请求
        boolean isCache=isGet; //是否使用缓存
        boolean isKeepAlive=isCache; //是否保持链接
        boolean isDownload=savePath!=null && !savePath.isEmpty(); //是否为文件下载模式
        if (!requestUrl.isEmpty()) {
            try {
                //新建一个URL对象
                URL url = new URL(isGet ? buildUrl(requestUrl, params) : requestUrl);
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setConnectTimeout(CONNECT_TIMEOUT * 1000); //设置连接超时时间
                urlConn.setReadTimeout(READ_TIMEOUT * 1000); //设置从主机读取数据超时
                urlConn.setRequestMethod(cMethod); //设置请求类型
                urlConn.setInstanceFollowRedirects(true); //设置本次连接是否自动处理重定向
                urlConn.setUseCaches(isCache); //Post请求不能使用缓存

                //设置请求头部信息
                for (String name : this.headers.keySet()) {
                    urlConn.addRequestProperty(name, this.headers.get(name));
                }

                //配置请求Content-Type
                urlConn.addRequestProperty("Content-Type", isDownload ? "application/octet-stream" :
                        (isGet ? "text/html" : "application/x-www-form-urlencoded"));

                //设置客户端与服务连接类型（保持连接）
                if (isKeepAlive) {
                    urlConn.addRequestProperty("Connection", "Keep-Alive");
                }

                if (!isGet) {
                    urlConn.setDoOutput(true); //Post请求必须设置允许输出 默认false
                    urlConn.setDoInput(true); //设置请求允许输入 默认是true
                }

                urlConn.connect();//开始连接

                if (!isGet && params != null) {
                    DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
                    dos.writeBytes(buildParams(params));
                    dos.flush();
                    dos.close();
                }

                int responseCode = urlConn.getResponseCode();
                if (responseCode == 200) {
                    if (!isDownload) { //一般模式
                        result = streamToString(urlConn.getInputStream());
                    } else { //下载模式
                        File descFile = new File(savePath);
                        FileOutputStream fos = new FileOutputStream(descFile);
                        byte[] buffer = new byte[1024];
                        long fileSize = 0;
                        int len;
                        InputStream inputStream = urlConn.getInputStream();
                        while ((len = inputStream.read(buffer)) != -1) {
                            fos.write(buffer, 0, len); //写到本地
                            fileSize += len;
                        }
                        result = String.valueOf(fileSize);
                    }
                } else {
                    Log.e(TAG, "File download failed : " + String.valueOf(responseCode));
                }
            }catch(Exception e){
                Log.e(TAG, e.toString());
            }
        }
        return result;
    }



    /**
     * 文件上传（POST方式）
     * @param uploadUrl 文件上传地址
     * @param filePath 上传文件路径
     * @return 服务器返回的字符串
     * */
    public String upLoad(String uploadUrl,String filePath){
        return upLoad(uploadUrl,filePath,null);
    }

    /**
     * 文件上传（POST方式）
     * @param uploadUrl 文件上传地址
     * @param filePath 上传文件路径
     * @param params 上传参数
     * @return 服务器返回的字符串
     * */
    public String upLoad(String uploadUrl,String filePath, HashMap<String, String> params) {
        String result="";
        try {
            File file = new File(filePath);
            URL url = new URL(uploadUrl);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true); //设置该连接允许读取
            urlConn.setDoInput(true); //设置该连接允许写入
            urlConn.setUseCaches(false); //设置不能适用缓存
            urlConn.setConnectTimeout(CONNECT_TIMEOUT * 1000);   //设置连接超时时间
            urlConn.setReadTimeout(READ_TIMEOUT * 1000);   //读取超时
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("connection", "Keep-Alive");//设置维持长连接
            urlConn.setRequestProperty("Accept-Charset", "UTF-8");//设置文件字符集
            //设置文件类型
            urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + "*****");
            String name = file.getName();
            DataOutputStream requestStream = new DataOutputStream(urlConn.getOutputStream());
            requestStream.writeBytes("--" + "*****" + "\r\n");
            //发送文件参数信息
            StringBuilder tempParams = new StringBuilder();
            tempParams.append("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + name + "\"; ");

            if(params!=null) {
                int pos = 0;
                int size = params.size();
                for (String key : params.keySet()) {
                    tempParams.append(String.format("%s=\"%s\"", key, params.get(key), "utf-8"));
                    if (pos < size - 1) {
                        tempParams.append("; ");
                    }
                    pos++;
                }
            }
            tempParams.append("\r\n");
            tempParams.append("Content-Type: application/octet-stream\r\n");
            tempParams.append("\r\n");
            requestStream.writeBytes(tempParams.toString());
            //发送文件数据
            FileInputStream fileInput = new FileInputStream(file);
            int bytesRead;
            byte[] buffer = new byte[1024];
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            while ((bytesRead = in.read(buffer)) != -1) {
                requestStream.write(buffer, 0, bytesRead);
            }
            requestStream.writeBytes("\r\n");
            requestStream.flush();
            requestStream.writeBytes("--" + "*****" + "--" + "\r\n");
            requestStream.flush();
            fileInput.close();
            int statusCode = urlConn.getResponseCode();
            if (statusCode == 200) {
                //上传成功：获取返回的数据
                result = streamToString(urlConn.getInputStream());
            } else {
                Log.e(TAG, "File upload failed : "+String.valueOf(statusCode));
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
        return result;
    }

    /**
     * 构建URL
     *
     * @param url URL
     * @param params 参数
     * @return 返回构建的URL字符串
     * */
    public static String buildUrl(String url,HashMap<String, String> params){
        if(params!=null) {
            if(url.contains("?")){
                if(url.endsWith("&")){
                    url = url+buildParams(params);
                }else{
                    url = url+"&"+buildParams(params);
                }
            }else{
                url = url+"?"+buildParams(params);
            }
        }
        return url;
    }

    /**
     * 参数序列号
     *
     * @param params 参数
     * @return 序列化后的字符串
     * */
    public static String buildParams(HashMap<String, String> params){
        String result="";
        if(params!=null) {
            try {
                StringBuilder tempParams = new StringBuilder();
                int pos = 0;
                for (String key : params.keySet()) {
                    if (pos > 0) {
                        tempParams.append("&");
                    }
                    tempParams.append(String.format("%s=%s", key, URLEncoder.encode(params.get(key), "utf-8")));
                    pos++;
                }
                result =  tempParams.toString();
            }catch (Exception e){
                Log.e(TAG,e.toString());
            }
        }
        return result;
    }

    /**
     * 将根据参数生成签名
     *  @param params 签名参数
     *  @param signatureKey 签名密钥
     * 签名步骤:
     * 1、将键名按照升序排列组成url序列号字符串，如：abc=a&bc=b&id=12&name=spring；
     * 2、对字符串进行md5加密后与密钥进行连接；
     * 3、对连接后的字符串再次进行md5加密
     * */
    public static String signature(HashMap<String, String> params,String signatureKey){
        int len=params.keySet().size();
        String[] keys = new String[len];
        params.keySet().toArray(keys);
        Arrays.sort(keys);
        StringBuffer paras=new StringBuffer();
        for (String key : keys) {
            len--;
            paras.append(key);
            paras.append("=");
            paras.append(params.get(key));
            if(len>0){
                paras.append("&");
            }
        }
        return BaseUtils.md5(BaseUtils.md5(paras.toString())+signatureKey);
    }

    /**
     * 使用系统APP_KEY进行签名
     *
     * @param params 签名参数
     * @return 签名字符串
     * */
    public static String signature(HashMap<String, String> params){
        return signature(params,Constants.APP_KEY);
    }

    /**
     * 将输入流转换成字符串
     *
     * @param is 从网络获取的输入流
     * @return 返回转换后的字符串
     */
    private static String streamToString(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
            is.close();
            byte[] byteArray = baos.toByteArray();
            return new String(byteArray);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }


    ///////////////////外部调用接口////////////////////

    /**
     * 处理网络请求
     * @param params [0] 请求地址
     * @param params [1] 请求方法
     * @param params [2] 保持结果路径
     * */
    @Override
    protected String doInBackground(String ...params) {
        String cRemoteUrl=null;
        String cResult="";
        int cIndex=0;
        for(String remoteUrl : remoteUrls){
            if(this.listener!=null){
                cRemoteUrl=this.listener.onResume(cIndex,remoteUrl,cResult);
                cResult=cRemoteUrl!=null ? request(cRemoteUrl!=null ? cRemoteUrl : remoteUrl) : "";
            }else{
                cResult=request(remoteUrl);
            }
            cIndex++;
        }
        return cResult;
    }

    @Override
    protected void onPostExecute(String result) {
        if(this.listener!=null) {
            this.listener.onComplete(result);
        }
    }

    ///////////////////外部调用接口////////////////////

    public interface IRequestListener{
        public void onComplete(String result);
        public String onResume(int index,String url,String result);
    }
}
