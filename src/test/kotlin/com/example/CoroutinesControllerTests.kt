package com.example

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.*
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.expectBodyList

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class CoroutinesControllerTests(@Autowired val client: WebTestClient) {

	private val banner = Banner("title", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")

	@Test
	fun index() {
		client.get().uri("/controller/").exchange().expectStatus().is2xxSuccessful.expectBody()
	}

	@Test
	fun suspending() {
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
	fun sequentialFlow() {
		client.get()
				.uri("/controller/sequential-flow")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.is2xxSuccessful
				.expectBodyList<Banner>().contains(banner, banner, banner, banner)
	}

	@Test
	fun parallelFlow() {
		client.get()
				.uri("/controller/concurrent-flow")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.is2xxSuccessful
				.expectBodyList<Banner>().contains(banner, banner, banner, banner)
	}

	@Test
	fun flowViaWebClient() {
		client.get()
				.uri("/controller/flow-via-webclient")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.is2xxSuccessful
				.expectBodyList<Banner>().contains(banner, banner, banner, banner)
	}

	@Test
	fun error() {
		client.get().uri("/controller/error").exchange().expectStatus().is5xxServerError
	}

}