package com.example

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapMerge
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
import org.springframework.web.reactive.function.client.bodyToFlow

@Controller
@RequestMapping("/controller")
class CoroutinesController(builder: WebClient.Builder) {

	private val banner = Banner("title", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")

	private val client = builder.baseUrl("http://localhost:8080/controller").build()

	@GetMapping("/suspend") @ResponseBody
	suspend fun suspendingEndpoint(): Banner {
		delay(10)
		return banner
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

	@GetMapping("/sequential-flow") @ResponseBody
	suspend fun sequentialFlow() = flow {
		for (i in 1..4) {
			emit(client
					.get()
					.uri("/suspend")
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.awaitBody<Banner>())}
		}

	// TODO Improve when https://github.com/Kotlin/kotlinx.coroutines/issues/1147 will be fixed
	@UseExperimental(FlowPreview::class)
	@GetMapping("/concurrent-flow") @ResponseBody
	suspend fun concurrentFlow() = flow {
		for (i in 1..4) emit("/suspend")
	}.flatMapMerge {
		flow {
			emit(client
				.get()
				.uri(it)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.awaitBody<Banner>())
		}
	}


	@GetMapping("/flow-via-webclient")
	@ResponseBody
	suspend fun flowViaWebClient() =
			client.get()
					.uri("/concurrent-flow")
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.bodyToFlow<Banner>()

	@GetMapping("/error")
	suspend fun error() {
		throw IllegalStateException()
	}

	@GetMapping("/cancel")
	suspend fun cancel() {
		throw CancellationException()
	}

}