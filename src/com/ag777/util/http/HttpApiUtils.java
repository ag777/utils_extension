package com.ag777.util.http;

import com.ag777.util.file.FileUtils;
import com.ag777.util.gson.GsonUtils;
import com.ag777.util.http.model.MyCall;
import com.ag777.util.lang.exception.model.JsonSyntaxException;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * 第三方服务接口调用处理封装
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2023/4/23 09:19
 */
public class HttpApiUtils {

    /**
     * 发送请求并保存响应对象
     * @param call 请求
     * @param apiName 接口名
     * @param clazz 类型
     * @param toException 处理其它异常
     * @param onHttpErr 处理Http异常
     * @param <T> 返回对象类型
     * @param <E> 抛出异常类型
     * @return 返回对象
     * @throws E 异常
     * @throws SocketTimeoutException http连接超时
     */
    public static <T, E extends Exception>T executeForObj(MyCall call, String apiName, Class<T> clazz, BiFunction<String, Throwable, E> toException, Consumer<Response> onHttpErr) throws E, SocketTimeoutException {
        String json = executeForStr(call, apiName, toException, onHttpErr);
        T obj;
        try {
            obj = GsonUtils.get().fromJsonWithException(json, clazz);
        } catch (JsonSyntaxException e) {
            throw toException.apply(apiName+"返回格式错误:"+json, e);
        }
        return obj;
    }

    /**
     * 发送请求并保存响应Map
     * @param call 请求
     * @param apiName 接口名
     * @param toException 处理其它异常
     * @param onHttpErr 处理Http异常
     * @param <E> 抛出异常类型
     * @return Map
     * @throws E 异常
     * @throws SocketTimeoutException http连接超时
     */
    public static <E extends Exception>Map<String, Object> executeForMap(MyCall call, String apiName, BiFunction<String, Throwable, E> toException, Consumer<Response> onHttpErr) throws E, SocketTimeoutException {
        String json = executeForStr(call, apiName, toException, onHttpErr);
        Map<String, Object> resultMap;
        try {
            resultMap = GsonUtils.get().toMapWithException(json);
        } catch (JsonSyntaxException e) {
            throw toException.apply(apiName+"返回格式错误:"+json, e);
        }
        return resultMap;
    }

    /**
     * 发送请求并保存响应字符串
     * @param call 请求
     * @param apiName 接口名
     * @param toException 处理其它异常
     * @param onHttpErr 处理Http异常
     * @param <E> 抛出异常类型
     * @return 字符串
     * @throws E 异常
     * @throws SocketTimeoutException http连接超时
     */
    public static <E extends Exception>String executeForStr(MyCall call, String apiName, BiFunction<String, Throwable, E> toException, Consumer<Response> onHttpErr) throws E, SocketTimeoutException {
        Response res = executeForResponse(call, apiName, toException, onHttpErr);
        try {
            Optional<String> temp = HttpUtils.responseStrForce(res);
            if (!temp.isPresent()) {
                throw toException.apply(apiName+"返回为空", null);
            }
            return temp.get();
        } catch (IOException e) {
            throw toException.apply("解析"+apiName+"返回出现io异常", e);
        }

    }

    /**
     * 发送请求并保存响应文件
     * @param call 请求
     * @param apiName 接口名
     * @param targetPath 本地存储路径
     * @param toException 处理其它异常
     * @param onHttpErr 处理Http异常
     * @param <E> 抛出异常类型
     * @return 文件
     * @throws E 异常
     * @throws SocketTimeoutException http连接超时
     */
    public static <E extends Exception> File executeForFile(MyCall call, String apiName, String targetPath, BiFunction<String, Throwable, E> toException, Consumer<Response> onHttpErr) throws E, SocketTimeoutException {
        InputStream in = executeFoInputStream(call, apiName, toException, onHttpErr);
        try {
            File file = FileUtils.write(in, targetPath);
            if(file.exists() && file.isFile()) {
                return file;
            }
            throw toException.apply("转写"+apiName+"返回异常,转写文件不存在", null);
        } catch (IOException e) {
            throw toException.apply("转写"+apiName+"返回出现io异常", e);
        }
    }

    /**
     * 发送请求并获取响应流
     * @param call 请求
     * @param apiName 接口名
     * @param toException 处理其它异常
     * @param onHttpErr 处理Http异常
     * @param <E> 抛出异常类型
     * @return 对端返回的输入流
     * @throws E 异常
     * @throws SocketTimeoutException http连接超时
     */
    public static <E extends Exception>InputStream executeFoInputStream(MyCall call, String apiName, BiFunction<String, Throwable, E> toException, Consumer<Response> onHttpErr) throws E, SocketTimeoutException {
        Response res = executeForResponse(call, apiName, toException, onHttpErr);
        try {
            Optional<InputStream> temp = HttpUtils.responseInputStream(res);
            if (!temp.isPresent()) {
                throw toException.apply(apiName+"返回为空", null);
            }
            return temp.get();
        } catch (IOException e) {
            throw toException.apply("读取"+apiName+"返回出现io异常", e);
        }
    }

    /**
     * 发送请求并获取响应体
     * @param call 请求
     * @param apiName 接口名
     * @param toException 处理其它异常
     * @param onHttpErr 处理Http异常
     * @param <E> 抛出异常类型
     * @return http响应
     * @throws E 异常
     * @throws SocketTimeoutException http连接超时
     */
    public static <E extends Exception>Response executeForResponse(MyCall call, String apiName, BiFunction<String, Throwable, E> toException, Consumer<Response> onHttpErr) throws E, SocketTimeoutException {
        Response res;
        try {
            res = call.executeForResponse();
        } catch(SocketTimeoutException e) {
            throw e;
        } catch (IOException e) {
            throw toException.apply(apiName+"调用失败", e);
        }
        if (!res.isSuccessful()) {
            if (onHttpErr != null) {
                onHttpErr.accept(res);
            }
            throw toException.apply(apiName+"异常:"+res.code(), null);
        }
        return res;
    }
}
