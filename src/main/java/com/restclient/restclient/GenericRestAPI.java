package com.restclient.restclient;

import java.util.HashMap;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class GenericRestAPI {

	public static Response makeAPICall(String methodName, String baseUri, String contextPath, String username,
			String password, Object payload, HashMap<String, Object> queryParams, HashMap<String, Object> pathParams,
			HashMap<String, Object> headers) {

		Response response = null;
		RestAssured.baseURI = baseUri;
		RestAssured.useRelaxedHTTPSValidation();

		RequestSpecification requestSpecification = RestAssured.given();

		if (username != null && password != null) {
			requestSpecification.auth().preemptive().basic(username, password);
		}

		if (headers != null && headers.size() > 0) {
			requestSpecification.headers(headers);
		}

		if (queryParams != null && queryParams.size() > 0) {
			requestSpecification.queryParams(queryParams);
		}

		if (pathParams != null && pathParams.size() > 0) {
			requestSpecification.pathParams(pathParams);
		}

		requestSpecification.contentType(ContentType.JSON);

		System.out.println("*************Request*************");
		requestSpecification.log().all();
		System.out.println("methodName:"+methodName);
		if (methodName == "GET") {
			response = requestSpecification.get().then().extract().response();
		} else if (methodName == "POST") {
			requestSpecification.body(payload);
			response = requestSpecification.post().then().extract().response();
		}else{
			System.out.println(methodName.contains(" is not supported yet"));
		}

		System.out.println("*************Response*************");
		response.then().log().everything(true);

		return response;
	}

	public static void main(String[] args) {

		HashMap<String, Object> queryParams = new HashMap<>();
		queryParams.put("page", 2);

		makeAPICall("GET", "https://reqres.in/", "/api/users", null, null, null, queryParams, null, null);
		System.out.println("************************");
		HashMap<String, Object> pathParams = new HashMap<>();
		pathParams.put("id", 2);
		String payload = "{\r\n" + "    \"name\": \"Babu Vemula\",\r\n" + "    \"job\": \"zion resident\"\r\n" + "}";
		makeAPICall("POST", "https://reqres.in/", "/api/users/{id}", null, null, payload, null, pathParams, null);

	}

}
