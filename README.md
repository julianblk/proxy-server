# proxy-server
Proxy Server REST app that leverages _Spring-Web_ and _RestTemplate_ to make GET calls to a given URL
and propagate the response body while logging the response headers


## Prerequisites:
- JDK 11

## Instructions:

Get the code: `git clone https://github.com/julianblk/proxy-server.git`

Run the app:
1. `cd proxy-server`
2. Make sure it is pointing to the `main` branch
3. `./mvnw clean spring-boot:run`

This will deploy the app at port 8080 with a single endpoint available: `/proxy`.

### Sample calls:

#### Valid URL:
`http://localhost:8080/proxy?proxy-url=http://www.google.com`

Should respond `OK (200)` with a body that contains the HTML of the original call.

#### Non-existing URL:
`http://localhost:8080/proxy?proxy-url=http://www.google.com/some-path`

Should respond `NOT_FOUND (404)` with this message in the body:

`{"errorMessage":"Could not found: http://www.google.com/some-path"}`


#### Invalid protocol URL (only `http` is accepted):
`http://localhost:8080/proxy?proxy-url=https://www.google.com`

Should respond `BAD_REQUEST (400)` with this message in the body:

`{"errorMessage":"Cannot proxy to https://www.google.com. Only HTTP protocol URI are accepted. Proxy URL must start with: http://"}`


## Testing:
`./mvnw clean test`