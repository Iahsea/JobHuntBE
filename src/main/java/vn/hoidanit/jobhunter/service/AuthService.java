package vn.hoidanit.jobhunter.service;

import java.security.Security;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.request.outbound.ExchangeTokenRequest;
import vn.hoidanit.jobhunter.domain.response.IntrospectResponse;
import vn.hoidanit.jobhunter.domain.response.ResLoginDTO;
import vn.hoidanit.jobhunter.domain.response.outbound.ExchangeTokenResponse;
import vn.hoidanit.jobhunter.repository.UserRepository;
import vn.hoidanit.jobhunter.repository.httpclient.OutboundIdentityClient;
import vn.hoidanit.jobhunter.repository.httpclient.OutboundUserClient;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@Service
@Slf4j
public class AuthService {

    private final OutboundIdentityClient outboundIdentityClient;
    private final OutboundUserClient outboundUserClient;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final UserProfileService userProfileService;

    public AuthService(OutboundIdentityClient outboundIdentityClient, OutboundUserClient outboundUserClient,
            UserRepository userRepository, SecurityUtil securityUtil, UserProfileService userProfileService) {
        this.outboundIdentityClient = outboundIdentityClient;
        this.outboundUserClient = outboundUserClient;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
        this.userProfileService = userProfileService;
    }

    @NonFinal
    @Value("${outbound.identity.client-id}")
    protected String CLIENT_ID;

    @NonFinal
    @Value("${outbound.identity.client-secret}")
    protected String CLIENT_SECRET;

    @NonFinal
    @Value("${outbound.identity.redirect-uri}")
    protected String REDIRECT_URI;

    @NonFinal
    @Value("${hoidanit.jwt.base64-secret}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${hoidanit.jwt.access-token-validity-in-seconds}")
    protected String REFRESHABLE_DURATION;

    @NonFinal
    protected final String GRANT_TYPE = "authorization_code";

    public ResLoginDTO outboundAuthenticate(String code) {
        var response = outboundIdentityClient.exchangeToken(
                ExchangeTokenRequest.builder()
                        .code(code)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .redirectUri(REDIRECT_URI)
                        .grantType(GRANT_TYPE)
                        .build());

        log.info("TOKEN RESPONSE {}", response);

        var userInfo = outboundUserClient.getUserInfo("json", response.getAccessToken());

        log.info("USER INFO RESPONSE {}", userInfo);

        // Kiểm tra user tồn tại
        var existingUser = userRepository.findByEmail(userInfo.getEmail());

        User user;

        if (existingUser == null) {
            // User chưa tồn tại → tạo mới
            user = userRepository.save(
                    User.builder()
                            .name(userInfo.getName())
                            .email(userInfo.getEmail())
                            .avatar(userInfo.getPicture())
                            .isGoogleAccount(true)
                            .password("") // có thể bỏ
                            .build());

            userProfileService.createUserProfileForUser(user);
        } else {
            // User đã tồn tại → dùng lại
            user = existingUser;
        }

        // Build response
        ResLoginDTO res = ResLoginDTO.builder()
                .user(
                        ResLoginDTO.UserLogin.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .name(user.getName())
                                .avatar(user.getAvatar())
                                .phoneNumber(user.getPhoneNumber())
                                .dateOfBirth(user.getDateOfBirth())
                                .gender(user.getGender())
                                .isUserGoogleAccount(user.isGoogleAccount())
                                .favoriteJobIds(user.getFavoriteJobIds())
                                .build())
                .build();

        // Tạo access token
        String accessToken = this.securityUtil.createAccessToken(user.getEmail(), res);
        res.setAccessToken(accessToken);

        log.info("RES LOGIN DTO: {}", res);

        return res;
    }


    public IntrospectResponse introspect(String token, boolean isRefresh) throws JOSEException, ParseException, IdInvalidException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(Long.parseLong(REFRESHABLE_DURATION), ChronoUnit.SECONDS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new IdInvalidException("Token is invalid");

        return IntrospectResponse.builder()
                .valid(true)
                .userId(signedJWT.getJWTClaimsSet().getSubject())
                .build();
    }
}
