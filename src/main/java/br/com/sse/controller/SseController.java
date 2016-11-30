package br.com.sse.controller;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import br.com.sse.model.Message;

@Controller
public class SseController {

	private final static DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("dd/MM/yyyy HH:mm:ss");
	private final List<SseEmitter> emitters;

	public SseController() {
		this.emitters = new ArrayList<>();
	}

	@RequestMapping("/sse/stream")
	public SseEmitter stream() {
		final SseEmitter emitter = new SseEmitter();
		this.emitters.add(emitter);
		emitter.onCompletion(() -> this.emitters.remove(emitter));
		return emitter;
	}

	@RequestMapping(path = "/message", method = POST, consumes = APPLICATION_JSON_VALUE)
	@ResponseBody
	public void sendEvent(@RequestBody final Message message) {
		final LocalDateTime dateTime = LocalDateTime.now();
		final List<SseEmitter> emittersToRemove = new ArrayList<>();
		this.emitters.forEach(emitter -> {
			try {
				message.setTime(dateTime.format(DATE_TIME_FORMATTER));
				emitter.send(message);
			} catch (final IOException e) {
				emitter.complete();
				emittersToRemove.add(emitter);
			}
		});
		this.removeEmitters(emittersToRemove);
	}

	private synchronized void removeEmitters(final List<SseEmitter> emittersToRemove) {
		this.emitters.removeAll(emittersToRemove);
	}

}
