# proxy-server
Proxy Server REST app that leverages _Spring-Web_ and _RestTemplate_ to make GET calls to a given URL and respond with the response headers


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

Should respond `OK (200)` with a body like this one:

`{"Date":"Mon, 20 Dec 2021 17:54:38 GMT","Expires":"-1","Cache-Control":"private, max-age=0","Content-Type":"text/html; charset=ISO-8859-1","P3P":"CP=\"This is not a P3P policy! See g.co/p3phelp for more info.\"","Server":"gws","X-XSS-Protection":"0","X-Frame-Options":"SAMEORIGIN","Set-Cookie":"1P_JAR=2021-12-20-17; expires=Wed, 19-Jan-2022 17:54:38 GMT; path=/; domain=.google.com; Secure","Accept-Ranges":"none","Vary":"Accept-Encoding","Transfer-Encoding":"chunked"}`


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