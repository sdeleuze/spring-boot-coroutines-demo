package com.example

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyAndAwait

@Controller
@RequestMapping("/controller")
@FlowPreview
class CoroutinesController(builder: WebClient.Builder) {

	private val banner = Banner("title", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")

	private val client = builder.baseUrl("http://localhost:8080/controller").build()

	@GetMapping("/suspend") @ResponseBody
	suspend fun suspendingEndpoint(): Banner {
		delay(10)
		return banner
	}

	@GetMapping("/flow") @ResponseBody
	fun flowEndpoint()= flow {
		emit(banner)
		emit(banner)
	}

	@GetMapping("/deferred") @ResponseBody
	fun deferredEndpoint(): Deferred<Banner> = GlobalScope.async {
		delay(10)
		banner
	}

	@GetMapping("/")
	suspend fun render(model: Model): String {
		delay(10)
		model["banner"] = banner
		return "index"
	}

	@GetMapping("/sequential") @ResponseBody
	suspend fun sequential(): List<Banner> {
		val banner1 = client
				.get()
				.uri("/suspend")
				.accept(MediaType.APPLICATION_JSON)
				.awaitExchange()
				.awaitBody<Banner>()
		val banner2 = client
				.get()
				.uri("/suspend")
				.accept(MediaType.APPLICATION_JSON)
				.awaitExchange()
				.awaitBody<Banner>()
		return listOf(banner1, banner2)
	}

	@GetMapping("/parallel") @ResponseBody
	suspend fun parallel(): List<Banner> = coroutineScope {
		val deferredBanner1: Deferred<Banner> = async {
			client
					.get()
					.uri("/suspend")
					.accept(MediaType.APPLICATION_JSON)
					.awaitExchange()
					.awaitBody<Banner>()
		}
		val deferredBanner2: Deferred<Banner> = async {
			client
					.get()
					.uri("/suspend")
					.accept(MediaType.APPLICATION_JSON)
					.awaitExchange()
					.awaitBody<Banner>()
		}
		listOf(deferredBanner1.await(), deferredBanner2.await())
	}

	@GetMapping("/error")
	suspend fun error() {
		throw IllegalStateException()
	}

	@GetMapping("/cancel")
	suspend fun cancel() {
		throw CancellationException()
	}

}