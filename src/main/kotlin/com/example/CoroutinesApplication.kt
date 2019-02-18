package com.example

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitResponse
import org.springframework.web.reactive.function.server.*

@SpringBootApplication
class CoroutinesApplication {

	@Bean
	fun routes(handlers: Handlers) = coRouter {
		GET("/", handlers::index)
		GET("/api", handlers::api)
		GET("/sequential", handlers::sequential)
		GET("/parallel", handlers::parallel)
	}
}

data class Banner(val title: String, val message: String)

@Component
class Handlers(builder: WebClient.Builder) {

	private val banner = Banner("title", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")

	private val client = builder.baseUrl("http://localhost:8080").build()

	suspend fun index(request: ServerRequest) =
			ServerResponse
					.ok()
					.renderAndAwait("index", mapOf("banner" to banner))

	suspend fun api(request: ServerRequest) =
			ServerResponse
					.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.bodyAndAwait(banner)

	suspend fun sequential(request: ServerRequest): ServerResponse {
		val banner1 = client
				.get()
				.uri("/api" )
				.accept(MediaType.APPLICATION_JSON)
				.awaitResponse()
				.awaitBody<Banner>()
		val banner2 = client
				.get()
				.uri("/api" )
				.accept(MediaType.APPLICATION_JSON)
				.awaitResponse()
				.awaitBody<Banner>()
		return ServerResponse
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.bodyAndAwait(listOf(banner1, banner2))
	}

	suspend fun parallel(request: ServerRequest): ServerResponse  = coroutineScope {

		val deferredBanner1 = async { client
				.get()
				.uri("/api" )
				.accept(MediaType.APPLICATION_JSON)
				.awaitResponse()
				.awaitBody<Banner>() }
		val deferredBanner2 = async { client
				.get()
				.uri("/api")
				.accept(MediaType.APPLICATION_JSON)
				.awaitResponse()
				.awaitBody<Banner>()
		}
		ServerResponse
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.bodyAndAwait(listOf(deferredBanner1.await(), deferredBanner2.await()))
	}
}

fun main(args: Array<String>) {
	runApplication<CoroutinesApplication>(*args)
}
