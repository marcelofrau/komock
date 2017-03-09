package ua.com.lavi.komock.engine.model.config.property.spring

import ua.com.lavi.komock.engine.model.config.property.http.ServerProperties
import java.util.*

/**
 * Created by Oleksandr Loushkin
 */

open class SpringConfigProperties {
    var enabled: Boolean = false
    var profiles:List<String> = ArrayList()
    var sourceFolder: String = "/"
    var server: ServerProperties = ServerProperties()
}
