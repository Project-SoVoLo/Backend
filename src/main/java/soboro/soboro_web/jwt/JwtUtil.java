package soboro.soboro_web.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

// JWT 생성, 파싱, 검증
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtProperties jwtProperties;

    // 로그 설정
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // 개인키로 서명키 생성
    private Key getSigningKey(){
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }

    // 토큰 발급(로그인 성공 시) - 관리자, 사용자 구분
    public String generateToken(String email, String role){
        Date date = new Date();

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(date) // 발급일
                .setExpiration(new Date(date.getTime() + jwtProperties.getExpiration())) // 만료시간
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 암호 알고리즘
                .compact();
    }

    // 토큰 검증
    public boolean validateToken(String token){
        try{
            // JWT 파싱
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        }catch(SecurityException | MalformedJwtException | io.jsonwebtoken.security.SignatureException e){
            logger.error("유효하지 않은 JWT 서명입니다.");
        }catch (ExpiredJwtException e){
            logger.error("만료된 JWT 토큰입니다.");
        }catch (UnsupportedJwtException e){
            logger.error("지원되지 않는 JWT 토큰입니다.");
        }catch (IllegalArgumentException e){
            logger.error("잘못된 JWT 토큰 입니다.");
        }
         return false;
    }

    // 토큰에서 사용자 이메일 추출
    public String getUsernameFromToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰에서 role 추출
    public String getRoleFromToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    // 토큰 만료시간 전달
    public long getExpiration(){
        return jwtProperties.getExpiration();
    }
}
