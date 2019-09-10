package com.example

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.reactive.function.server.renderAndAwait

@SpringBootApplication
@ExperimentalCoroutinesApi
class CoroutinesApplication {

	@Bean
	@FlowPreview
	fun routes(handlers: Handlers) = coRouter {
		GET("/", handlers::index)
		GET("/suspend", handlers::suspending)
		GET("/sequential-flow", handlers::sequentialFlow)
		GET("/concurrent-flow", handlers::concurrentFlow)
		GET("/error", handlers::error)
		GET("/cancel", handlers::cancel)
	}
}

data class Banner(val title: String, val message: String)

@Suppress("DuplicatedCode")
@Component
@ExperimentalCoroutinesApi
class Handlers(builder: WebClient.Builder) {

	private val banner = Banner("title", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")

	private val client = builder.baseUrl("http://localhost:8080").build()

	suspend fun index(request: ServerRequest) =
			ServerResponse
					.ok()
					.renderAndAwait("index", mapOf("banner" to banner))

	suspend fun suspending(request: ServerRequest) =
			ServerResponse
					.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.bodyValueAndAwait(banner)

	suspend fun sequentialFlow(request: ServerRequest) = flow {
		for (i in 1..4) {
			emit(client
					.get()
					.uri("/suspend")
					.accept(MediaType.APPLICATION_JSON)
					.awaitExchange()
					.awaitBody<Banner>())}
		}.let { ServerResponse
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.bodyAndAwait(it) }

	// TODO Improve when https://github.com/Kotlin/kotlinx.coroutines/issues/1147 will be fixed
	@FlowPreview
	suspend fun concurrentFlow(request: ServerRequest): ServerResponse = flow {
		for (i in 1..4) emit("/suspend")
	}.flatMapMerge {
		flow {
			emit(client
					.get()
					.uri(it)
					.accept(MediaType.APPLICATION_JSON)
					.awaitExchange()
					.awaitBody<Banner>())
		}
	}.let { ServerResponse
			.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.bodyAndAwait(it) }

	suspend fun error(request: ServerRequest): ServerResponse {
		throw IllegalStateException()
	}

	suspend fun cancel(request: ServerRequest): ServerResponse {
		throw CancellationException()
	}
}

@ExperimentalCoroutinesApi
fun main(args: Array<String>) {
	runApplication<CoroutinesApplication>(*args)
}
