package com.fortysevendeg.thool.spring.server

import arrow.core.Either
import com.fortysevendeg.thool.DecodeResult
import com.fortysevendeg.thool.Endpoint
import com.fortysevendeg.thool.model.Header
import com.fortysevendeg.thool.model.StatusCode
import com.fortysevendeg.thool.model.ServerResponse as ArrowSResponse
import com.fortysevendeg.thool.server.ServerEndpoint
import com.fortysevendeg.thool.spring.client.invokeAndResponse
import com.fortysevendeg.thool.test.ServerInterpreterSuite
import io.undertow.Undertow
import org.springframework.http.server.reactive.HttpHandler
import org.springframework.http.server.reactive.UndertowHttpHandlerAdapter
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import kotlin.properties.Delegates

class SpringServerInterpreterSuite : ServerInterpreterSuite() {

  private val client: WebClient = WebClient.create()
  private var server: Undertow by Delegates.notNull()

  init {
    afterTest { server.stop() }
  }

  override suspend fun <A> withEndpoint(
    endpoint: ServerEndpoint<*, *, *>,
    run: suspend Unit.(baseUrl: String) -> A
  ): A {
    val routerFunction: RouterFunction<ServerResponse> = routerFunction(endpoint)
    val httpHandler: HttpHandler = RouterFunctions.toHttpHandler(routerFunction)
    val adapter = UndertowHttpHandlerAdapter(httpHandler)
    server = Undertow.builder().addHttpListener(8080, "127.0.0.1").setHandler(adapter).build().apply {
      start()
    }
    return Unit.run("http://localhost:8080")
  }

  override suspend fun <I, E, O> Unit.requestAndResponse(
    endpoint: Endpoint<I, E, O>,
    baseUrl: String,
    input: I
  ): Pair<DecodeResult<Either<E, O>>, ArrowSResponse> {
    val (res, response) = client.invokeAndResponse(endpoint, baseUrl, input)
    return Pair(
      res,
      ArrowSResponse(
        StatusCode(response.rawStatusCode()),
        response.headers().asHttpHeaders().toSingleValueMap().map { (name, value) -> Header(name, value) },
        null // TODO("Extract the correct body type")
      )
    )
  }
}
