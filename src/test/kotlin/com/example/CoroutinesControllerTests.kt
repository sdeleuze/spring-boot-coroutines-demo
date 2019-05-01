package com.example

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.expectBodyList

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class CoroutinesControllerTests(@Autowired val client: WebTestClient) {

	private val banner = Banner("title", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")

	@Test
	fun index() {
		client.get().uri("/controller/").exchange().expectStatus().is2xxSuccessful.expectBody()
	}

	@Test
	fun api() {
		client.get()
				.uri("/controller/suspend")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.is2xxSuccessful
				.expectBody<Banner>()
				.isEqualTo(banner)
	}

	@Test
	fun apiFlow() {
		client.get()
				.uri("/controller/flow")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.is2xxSuccessful
				.expectBodyList<Banner>().contains(banner, banner)
	}

	@Test
	fun sequential() {
		client.get()
				.uri("/controller/sequential")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.is2xxSuccessful
				.expectBodyList<Banner>().contains(banner, banner)
	}

	@Test
	fun parallel() {
		client.get()
				.uri("/controller/parallel")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.is2xxSuccessful
				.expectBodyList<Banner>().contains(banner, banner)
	}

	@Test
	fun flowViaWebClient() {
		client.get()
				.uri("/controller/flow-via-webclient")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.is2xxSuccessful
				.expectBodyList<Banner>().contains(banner, banner)
	}

	@Test
	fun error() {
		client.get().uri("/controller/error").exchange().expectStatus().is5xxServerError
	}

	@Test
	fun cancel() {
		client.get().uri("/controller/cancel").exchange().expectStatus().is5xxServerError
	}

}