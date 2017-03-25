package ua.com.lavi.komock.engine

import org.slf4j.LoggerFactory
import ua.com.lavi.komock.engine.handler.AfterRequestHandler
import ua.com.lavi.komock.engine.handler.BeforeRequestHandler
import ua.com.lavi.komock.engine.handler.RequestHandler
import ua.com.lavi.komock.engine.model.Request
import ua.com.lavi.komock.engine.model.Response
import ua.com.lavi.komock.engine.model.config.http.RouteProperties
import java.util.*
import java.util.regex.Pattern

/**
 * Created by Oleksandr Loushkin
 */

class RequestHandlerBuilder(val routeProperties: RouteProperties) {

    private val log = LoggerFactory.getLogger(this.javaClass)
    private val parameterRegexp = Pattern.compile("\\$\\{(.+?)}")

    fun beforeRouteHandler(): BeforeRequestHandler {

        val beforeRequestHandler = object : BeforeRequestHandler {
            override fun handle(request: Request, response: Response) {
                if (routeProperties.logRequest) {
                    log.info("url: ${routeProperties.url}. RequestBody: ${request.requestBody()}")
                }
                if (routeProperties.logBefore.isNotEmpty()) {
                    log.info(routeProperties.logBefore)
                }
            }
        }
        return beforeRequestHandler
    }

    fun afterRequestHandler(): AfterRequestHandler {

        val afterRequestHandler = object : AfterRequestHandler {
            override fun handle(request: Request, response: Response) {
                if (routeProperties.logResponse) {
                    log.info("url: ${routeProperties.url}. ResponseBody: ${response.content}")
                }

                if (routeProperties.logAfter.isNotEmpty()) {
                    log.info(routeProperties.logAfter)
                }
            }
        }
        return afterRequestHandler
    }

    fun routeHandler(): RequestHandler {

        val requestHandler = object : RequestHandler {
            override fun handle(request: Request, response: Response) {

                //check request for Basic Authorization header
                if (routeProperties.basicAuth.enabled) {
                    if (!checkBasicAuth(request)) {
                        response.statusCode(401)
                        return
                    }
                }

                response.contentType(routeProperties.contentType)
                response.statusCode(routeProperties.code)
                response.content = replacePlaceholders(request.queryParametersMap(), routeProperties.responseBody)

                // add http headers
                routeProperties.responseHeaders.forEach { headersMap ->
                    headersMap.forEach { header -> response.addHeader(header.key, header.value) }
                }

                //add cookies
                routeProperties.cookies.forEach { cookie -> response.addCookie(cookie) }
            }
        }
        return requestHandler

    }

    private fun checkBasicAuth(request: Request): Boolean {
        val basicUsernamePasswordEncoded = "Basic " + Base64.getEncoder().
                encodeToString("${routeProperties.basicAuth.username}:${routeProperties.basicAuth.password}"
                        .toByteArray())

        if (basicUsernamePasswordEncoded != request.authorizationHeader()) {
            return false
        }
        return true

    }

    fun replacePlaceholders(parametersMap: Map<String, String>, str: String): String {
        val m = parameterRegexp.matcher(str)
        val sb = StringBuffer()
        while (m.find()) {
            val value = parametersMap[m.group(1)]
            if (value != null) {
                m.appendReplacement(sb, value)
            }
        }
        m.appendTail(sb)
        return sb.toString()
    }

}