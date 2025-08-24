package org.project.application.util;

import org.project.application.dto.common.ErrorMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

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

	public static Object required(String fieldName, Object field) {
		if (field == null)
			throw responseException(Status.BAD_REQUEST, "%s must be provided.".formatted(fieldName));

		return field;
	}

	public static WebApplicationException unableToProcessRequestException() {
		return responseException(Response.Status.INTERNAL_SERVER_ERROR,
				"Unable to process your request at the moment. Please try again.");
	}
}
