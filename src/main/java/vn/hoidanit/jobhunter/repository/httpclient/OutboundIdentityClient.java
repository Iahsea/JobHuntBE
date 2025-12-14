package vn.hoidanit.jobhunter.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import feign.QueryMap;
import vn.hoidanit.jobhunter.domain.request.outbound.ExchangeTokenRequest;
import vn.hoidanit.jobhunter.domain.response.outbound.ExchangeTokenResponse;

@FeignClient(name = "outbound-identity", url = "https://oauth2.googleapis.com")
public interface OutboundIdentityClient {
    @PostMapping(value = "/token", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ExchangeTokenResponse exchangeToken(@QueryMap ExchangeTokenRequest request);
}
