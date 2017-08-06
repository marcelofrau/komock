package ua.com.lavi.komock.engine.handler.after

import ua.com.lavi.komock.engine.model.Request
import ua.com.lavi.komock.engine.model.Response

/**
 * Created by Oleksandr Loushkin
 */

interface AfterResponseHandler {
    fun handle(request: Request, response: Response)
}
