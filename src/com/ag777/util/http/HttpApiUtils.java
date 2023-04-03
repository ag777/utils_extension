package com.ag777.util.http;

import com.ag777.util.gson.GsonUtils;
import com.ag777.util.http.model.MyCall;
import com.ag777.util.lang.exception.model.JsonSyntaxException;
import okhttp3.Response;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * 第三方服务接口调用处理封装
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2023/4/3 17:41
 */
public class HttpApiUtils {

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

    public static <E extends Exception>String executeForStr(MyCall call, String apiName, BiFunction<String, Throwable, E> toException, Consumer<Response> onHttpErr) throws E, SocketTimeoutException {
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
}
