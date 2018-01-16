package com.gzf.video.core.dispatcher;

import com.gzf.video.core.controller.Controller;
import com.gzf.video.core.controller.action.method.Get;
import com.gzf.video.core.controller.action.method.Post;

import java.lang.reflect.Method;

import static com.gzf.video.util.PathAndParametersUtil.decodeComponent;
import static com.gzf.video.util.PathAndParametersUtil.findPathEndIndex;

public class ActionPathParser {

    /**
     * Parse path
     * via annotations:
     * {@link Controller},
     * {@link Get}, {@link Post}, <br/>
     * <pre>e.g.
     *      <em>@Controller</em>("/prefix")
     *      class MyController {
     *
     *          <em>@Get</em>("/path/to/action/")
     *          {@link com.gzf.video.core.http.response.Response}
     *          myAction({@link com.gzf.video.core.http.HttpExchange} response) {
     *              ...
     *          }
     *      }
     * </pre>
     * The request which has path: {@code /prefix/path/to/action/},<br />
     * will be dispatched to this action method. <br />
     *
     * @param method method ∈ { {@link Get}, {@link Post} }
     * @return path to access this action
     */
    public static String parsePath(Method method, String methodUrl) {
        // prefix url from its class's annotation @Controller
        Controller controller = method.getDeclaringClass().getDeclaredAnnotation(Controller.class);
        String classUrl = controller == null ? "" : controller.value();

        String fullUrl = classUrl + methodUrl;

        // add '/' to head if the first character isn't '/'
        if (fullUrl.isEmpty() || fullUrl.charAt(0) != '/') {
            fullUrl = '/' + fullUrl;
        }

        // e.g. "/path///to//action" -> "/path/to/action"
        fullUrl = fullUrl.replaceAll("/+", "/");

        fullUrl = decodeComponent(fullUrl, 0, findPathEndIndex(fullUrl));

        return fullUrl;
    }
}
