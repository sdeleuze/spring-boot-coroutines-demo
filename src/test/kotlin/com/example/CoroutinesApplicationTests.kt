package com.example


import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class CoroutinesApplicationTests(@Autowired val client: WebTestClient) {

	@Test
	fun index() {
		client.get().uri("/").exchange().expectStatus().is2xxSuccessful.expectBody()
	}

	@Test
	fun api() {
		client.get().uri("/api").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().is2xxSuccessful.expectBody()
	}

	@Test
	fun sequential() {
		client.get().uri("/sequential").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().is2xxSuccessful.expectBody()
	}

	@Test
	fun parallel() {
		client.get().uri("/parallel").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().is2xxSuccessful.expectBody()
	}

}
