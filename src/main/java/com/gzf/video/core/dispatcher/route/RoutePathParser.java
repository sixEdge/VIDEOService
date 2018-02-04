package com.gzf.video.core.dispatcher.route;

import java.util.Objects;

import static com.gzf.video.core.tool.PathAndParametersUtil.decodeComponent;
import static com.gzf.video.core.tool.PathAndParametersUtil.findPathEndIndex;

public class RoutePathParser {

    /**
     * Parse path
     * via annotations:
     * {@link com.gzf.video.core.controller.Controller},
     * {@link Route}, <br/>
     * <pre>e.g.
     *      <em>@Controller</em>("/prefix")
     *      class MyController {
     *
     *          <em>@Route</em>(method = GET, url = "/path/to/action/")
     *          {@link com.gzf.video.core.http.response.Response}
     *          myAction({@link com.gzf.video.core.http.HttpExchange} response) {
     *              ...
     *          }
     *      }
     * </pre>
     * The request which has path: {@code /prefix/path/to/action/},<br />
     * will be dispatched to this action method. <br />
     *
     * @param classUrl prefix url from class's annotation @Controller
     * @return path to access this action
     */
    public static String parsePath(final String classUrl, final String methodUrl) {
        Objects.requireNonNull(classUrl, "classUrl");

        String controllerUrl = classUrl.endsWith("/") ? classUrl : classUrl + "/";
        String fullUrl = controllerUrl + methodUrl;

        // add '/' to head if the first character isn't '/'
        if (fullUrl.charAt(0) != '/') {
            fullUrl = '/' + fullUrl;
        }

        // e.g. "/path///to//action" -> "/path/to/action"
        fullUrl = fullUrl.replaceAll("/+", "/");

        fullUrl = decodeComponent(fullUrl, 0, findPathEndIndex(fullUrl));

        return fullUrl;
    }
}
