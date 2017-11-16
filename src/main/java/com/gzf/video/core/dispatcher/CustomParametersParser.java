package com.gzf.video.core.dispatcher;

import com.gzf.video.core.annotation.Controller;
import com.gzf.video.core.annotation.action.Get;
import com.gzf.video.core.annotation.action.PathParam;
import com.gzf.video.core.annotation.action.Post;
import com.gzf.video.core.annotation.action.ReqParam;
import com.gzf.video.util.StringUtil;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class CustomParametersParser {

    /**
     * Parse custom-request-pathSegments-parameters and custom-request-parameters via annotations:
     * {@link Controller},
     * {@link Get}, {@link Post},
     * {@link PathParam}, {@link ReqParam}.
     *
     * @param method the method has {@link Get} or {@link Post}
     * @return {@link CustomParameter}
     * @throws Exception when parse fail
     */
    public CustomParameter parseParams(Method method, String methodUrl) throws Exception {
        // prefix url declared by @ControllerFunctions from class
        Controller controller = method.getDeclaringClass().getDeclaredAnnotation(Controller.class);
        String classUrl = controller == null ? "" : controller.value();

        String fullUrl = classUrl + methodUrl;

        // add '/' to head if the first character not equals '/'
        if (fullUrl.isEmpty() || fullUrl.charAt(0) != '/') {
            fullUrl = '/' + fullUrl;
        }

        // e.g. "/pathSegments///to//action" -> "/pathSegments/to/action"
        fullUrl = fullUrl.replaceAll("/+", "/");

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(fullUrl);

        final String[] pathSegments = queryStringDecoder.path().split("/");

        CustomParameter customParameters = new CustomParameter();

        // parse path parameters
        List<String> pathParamList = new LinkedList<>();
        for (String seg : pathSegments)
            if (!seg.isEmpty() && seg.charAt(0) == '{' && seg.charAt(seg.length() - 1) == '}')
                pathParamList.add( parsePathParam(seg, fullUrl) );
        customParameters.pathParameters = pathParams(pathParamList, method);

        // parse request parameters
        customParameters.requestParameters = parseReqParams(method, queryStringDecoder.parameters());

        // parse custom request parameters
        customParameters.customRequestParameters = customReqParams(method, customParameters.requestParameters.keySet());

        // parse pathSegments
        customParameters.pathSegments = emptyBrace(pathSegments);

        return customParameters;
    }

    private Map<String, Class> pathParams(List<String> paramList, Method method) throws Exception {
        final Map<String, Class> cp = bindPathParamsFromMethodParams(method);
        final Map<String, Class> customParams = new LinkedHashMap<>(cp.size());

        for (String s : paramList) {
            Class clazz = cp.get(s);
            if (clazz == null)
                throw new Exception("'{' and '}' can only be used on custom parameter, " +
                        "found \"" + s + "\" unexpected at "
                        + method.getDeclaringClass().getName() + "#" + method.getName());
            customParams.put(s, clazz);
            cp.remove(s);
        }

        if (!cp.isEmpty())
            throw new Exception("unbound custom pathSegments parameter found at "
                    + method.getDeclaringClass().getName() + "#" + method.getName());

        return Collections.unmodifiableMap(customParams);
    }

    private Map<String, String> parseReqParams(final Method method, Map<String, List<String>> parameters) {
        Map<String, String> reqParams = new HashMap<>();

        parameters.forEach((k, v) -> {
            if (v.size() > 1)
                throw new RuntimeException("a request parameter can only have one value, but " +
                        k + " has " + v.size() +
                        " at " + method.getDeclaringClass().getName() + "#" + method.getName());
            reqParams.put(k, v.get(0));
        });

        return Collections.unmodifiableMap(reqParams);
    }

    private Map<String, Class> customReqParams(final Method method, final Set<String> keySet) throws Exception {
        final Parameter[] ps = method.getParameters();
        Map<String, Class> reqParameters = new HashMap<>();
        for (Parameter p : ps) {
            ReqParam reqParam = p.getDeclaredAnnotation(ReqParam.class);
            if (reqParam != null) {
                String v = reqParam.value();
                Class t = p.getType();

                if (keySet.contains(v))
                    throw new Exception("custom-request-parameter and request-parameter have same name " +
                            "\"" + v + "\"" +
                            " at " + method.getDeclaringClass().getName() + "#" + method.getName());
                validType(t, method);

                reqParameters.put(v, t);
            }
        }

        return Collections.unmodifiableMap(reqParameters);
    }

    private Map<String, Class> bindPathParamsFromMethodParams(final Method method) throws Exception {
        final Parameter[] ps = method.getParameters();
        Map<String, Class> pathParameters = new HashMap<>();
        for (Parameter p : ps) {
            PathParam pathParam = p.getDeclaredAnnotation(PathParam.class);
            if (pathParam != null) {
                Class t = p.getType();
                validType(t, method);

                pathParameters.put(pathParam.value(), t);
            }
        }

        return pathParameters;
    }

    private String parsePathParam(final String seg, final String url) {
        String custom = seg.substring(1, seg.length() - 1);
        if (custom.isEmpty())
            throw new IllegalArgumentException("empty {}, in " + url);
        return custom;
    }

    private void validType(final Class<?> type, final Method method) throws Exception {
        if (!(type.isAssignableFrom(String.class)
                || type.isAssignableFrom(char.class)
                || type.isAssignableFrom(short.class)
                || type.isAssignableFrom(int.class)
                || type.isAssignableFrom(float.class)
                || type.isAssignableFrom(double.class)
                || type.isAssignableFrom(boolean.class)))
        {
            throw new Exception("custom parameter can only have primitive Java types or String type, " +
                    "found " + type.getName() + " at " +
                    method.getDeclaringClass().getName() + "#" + method.getName());
        }
    }

    private String[] emptyBrace(String[] segs) {
        String[] ret = new String[segs.length];
        for (int i = 0; i < segs.length; i++) {
            String a = segs[i];
            ret[i] = a.startsWith("{") && a.endsWith("}")
                    ? "{}"
                    : a;
        }
        return ret;
    }



    public static class CustomParameter {
        private String[] pathSegments;

        /**
         * LinkedHashMap.
         */
        private Map<String, Class> pathParameters;
        private Map<String, String> requestParameters;
        private Map<String, Class> customRequestParameters;

        CustomParameter() {}

        public String[] getPathSegments() {
            return pathSegments;
        }

        public Map<String, String> getRequestParameters() {
            return requestParameters;
        }

        public Map<String, Class> getCustomRequestParameters() {
            return customRequestParameters;
        }

        @Override
        public String toString() {
            return "CustomParameter{" +
                    "pathSegments=" + StringUtil.concatWith(pathSegments, "/") +
                    ", pathParameters=" + pathParameters +
                    ", requestParameters=" + requestParameters +
                    ", customRequestParameters=" + customRequestParameters +
                    '}';
        }
    }
}
