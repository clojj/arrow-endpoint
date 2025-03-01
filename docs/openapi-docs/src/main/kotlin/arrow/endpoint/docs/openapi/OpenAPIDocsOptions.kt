package arrow.endpoint.docs.openapi

import arrow.endpoint.Schema
import arrow.endpoint.model.Method
import java.util.Locale

public data class OpenAPIDocsOptions(
  val operationIdGenerator: (List<String>, Method) -> String = Companion::defaultOperationIdGenerator,
  val schemaName: (Schema.ObjectInfo) -> String = Companion::defaultSchemaName
) {
  public companion object {
    public fun defaultOperationIdGenerator(pathComponents: List<String>, method: Method): String {
      val components = pathComponents.ifEmpty { listOf("root") }
      // converting to camelCase
      return method.value.lowercase() + components.joinToString("") { it.lowercase().replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString() } }
    }

    public fun defaultSchemaName(info: Schema.ObjectInfo): String {
      val shortName = info.fullName.split('.').last()
      return (shortName + info.typeParameterShortNames.joinToString("_"))
    }
  }
}
