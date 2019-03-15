package com.example

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/annotations")
class CoroutinesController {

	private val banner = Banner("title", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")

	@GetMapping("/") @ResponseBody
	suspend fun suspendingEndpoint(): String {
		delay(10)
		return "toto"
	}

	@GetMapping("/deferred") @ResponseBody
	fun deferredEndpoint(): Deferred<String> = GlobalScope.async {
		delay(10)
		"toto"
	}

	@GetMapping("/banner")
	suspend fun banner(model: Model): String {
		delay(10)
		model["banner"] = banner
		return "index"
	}

}