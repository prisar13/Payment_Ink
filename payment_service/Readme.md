# Notes

1. Since i am using jpa, i will have pagination fetaure by default. We use PageRequest to pass the page, size of each page, Sorting criteria etc options in the config. This can be passed into inbuilt jpa methods to get the desired outcome.
2. Can run asynchronous calls (fire and forget) for the fraud service using CompletableFuture.runAsync. we will also need the RestTemplate class to send a postFor object for all post calls.
3. Set Headers for auth : 
    ```
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(jwtUtil.getServiceToken());
    HttpEntity<FraudRequestDTO> entity = new HttpEntity<>(fraudRequest, headers);

    ```
4. @Value("${fraud.service.url}"): to access any value from the application properties file directly.
