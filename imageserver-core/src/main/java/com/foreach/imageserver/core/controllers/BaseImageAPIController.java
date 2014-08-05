package com.foreach.imageserver.core.controllers;

import com.foreach.imageserver.dto.JsonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

public class BaseImageAPIController
{

	protected JsonResponse<Void> error( String message ) {
		JsonResponse<Void> result = new JsonResponse<>();
		result.setSuccess( false );
		result.setErrorMessage( message );
		return result;
	}

	protected <T> JsonResponse<T> success( T result ) {
		JsonResponse<T> jsonResponse = success();
		jsonResponse.setResult( result );
		return jsonResponse;
	}

	protected <T> JsonResponse<T> success() {
		JsonResponse<T> jsonResponse = new JsonResponse<>();
		jsonResponse.setSuccess( true );
		return jsonResponse;
	}

	/**
	 * Make sure that for controller methods that fail with an exception, we still return some pretty json
	 */

	@ExceptionHandler(Exception.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public JsonResponse handleError( HttpServletRequest req, Exception exception ) {
		return error( exception.getMessage() );
	}
}
