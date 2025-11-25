package vn.hoidanit.jobhunter.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.OtpToken;
import vn.hoidanit.jobhunter.repository.OtpTokenRepository;

@Service
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;

    public OtpService(OtpTokenRepository otpTokenRepository) {
        this.otpTokenRepository = otpTokenRepository;
    }

    public OtpToken createOtpForEmail(String email, int minutesToExpire) {
        String code = generateNumericCode(6);
        Instant now = Instant.now();
        OtpToken otp = new OtpToken();
        otp.setEmail(email);
        otp.setCode(code);
        otp.setCreatedAt(now);
        otp.setExpiryAt(now.plus(minutesToExpire, ChronoUnit.MINUTES));
        return this.otpTokenRepository.save(otp);
    }

    public boolean validateOtp(String email, String code) {
        Optional<OtpToken> opt = this.otpTokenRepository.findFirstByEmailAndCode(email, code);
        if (opt.isEmpty())
            return false;
        OtpToken otp = opt.get();
        if (otp.getExpiryAt() == null || Instant.now().isAfter(otp.getExpiryAt())) {
            return false;
        }
        // optionally delete after use
        this.otpTokenRepository.delete(otp);
        return true;
    }

    private String generateNumericCode(int length) {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(rnd.nextInt(10));
        }
        return sb.toString();
    }
}
