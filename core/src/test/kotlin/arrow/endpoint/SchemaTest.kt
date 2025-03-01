package arrow.endpoint

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SchemaTest : StringSpec({

  "enum schema" {
    Schema.enum<Test>() shouldBe
      Schema.Enum(
        Schema.ObjectInfo("arrow.endpoint.Test"),
        listOf(
          Schema.EnumValue("A", 0),
          Schema.EnumValue("B", 1),
          Schema.EnumValue("C", 2)
        )
      )
  }

  "map schema" {
    Schema.person().asOpenProduct() shouldBe
      Schema.OpenProduct(
        Schema.ObjectInfo("Map", listOf("arrow.endpoint.Person")),
        Schema.person()
      )
  }

  "product schema" {
    Schema.product(
      Person::name to Schema.string,
      Person::age to Schema.int
    ) shouldBe Schema.person()
  }
})
