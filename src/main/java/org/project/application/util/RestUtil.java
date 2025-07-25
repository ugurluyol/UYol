package org.project.application.util;

import org.project.application.dto.common.ErrorMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class RestUtil {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private RestUtil() {
	}

	public static WebApplicationException responseException(Response.Status status, String message) {
		String entity;
		try {
			entity = objectMapper.writeValueAsString(new ErrorMessage(message));
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Unexpected exception, error message can`t be serialized.");
		}

		return new WebApplicationException(
				Response.status(status).entity(entity).type(MediaType.APPLICATION_JSON_TYPE).build());
	}
}
