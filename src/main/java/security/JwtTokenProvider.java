package security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {
    //* 토큰 생성 및 검증, 정보 추출 및 만료 처리 기능들을 모아놓은 클래스
    public final Key key;



    //* 액세스 토큰의 유효 기간 (밀리초)
    public static final long REFRESH_TOKEN_VALIDITY = 1000 * 60 * 60; // 1시간

    //* 리프레시 토큰의 유효 기간 (밀리초)
    private static final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 60 * 24 * 7; // 7일

    public JwtTokenProvider(
            @Value("${spring.jwt.secret}") String secretKey // 서버의 환경 설정 파일에 안전하게 숨겨든 비밀키 문자열을 가져온다.
    ) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    //* 엑세스 토큰 생성
    public String generateAccessToken(String username) {
        Date now = new Date(); // 현재 시간을 먼저 가져옴
        Date expiry = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY);

        return Jwts.builder()
                .setSubject(username)
                .setId(UUID.randomUUID().toString())
                .claim("tokenType", "ACCESS")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //* 리프레시 토큰 생성
    public String generateRefreshToken(String username) {
        Date now = new Date(); // 현재 시간을 먼저 가져옴
        Date expiry = new Date(now.getTime() + REFRESH_TOKEN_VALIDITY);

        return Jwts.builder()
                .setSubject(username)
                .setId(UUID.randomUUID().toString())
                .claim("tokenType", "REFRESH")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //* 액세스 토큰 유효성 (위변조 여부) 검증
    public boolean validAccessToken(String token) {
        try {
            String tokenType = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("tokenType",  String.class);

            return tokenType.equals("ACCESS");

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    //* 리프레시 토큰 유효성 (위변조 여부) 검증
    public boolean validRefreshToken(String token) {
        try {
            String tokenType = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("tokenType",  String.class);

            return tokenType.equals("REFRESH");

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    //* JWT 토큰에서 username(subject)
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    //* JWT 토큰에서 인증일자 추출
    public Date getExpiration(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

}
