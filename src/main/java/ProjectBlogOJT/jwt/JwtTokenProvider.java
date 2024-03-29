package ProjectBlogOJT.jwt;
import ProjectBlogOJT.model.entity.User;
import io.jsonwebtoken.*;
import ProjectBlogOJT.model.service.UserSevice;
import ProjectBlogOJT.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {
    @Autowired
    private UserSevice userService;
    @Value("${ra.jwt.secret}")
    private String JWT_SECRET;
    @Value(("${ra.jwt.expiration}"))
    private int JWT_EXPIRATION;
//    Tao jwt tu thong tin cua User

    public String generateTokenEmail(String email) {
        User users = userService.findByEmail(email);
        Date now = new Date();
        Date dateExpired = new Date(now.getTime() + JWT_EXPIRATION);
        //Tao chuoi JWT tu userName
        return Jwts.builder().setSubject(users.getUserEmail())
                .setIssuedAt(now)
                .setExpiration(dateExpired)
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET).compact();
    }

    public String generateToken(CustomUserDetails customUserDetails) {
        Date now = new Date();
        Date dateExpired = new Date(now.getTime() + JWT_EXPIRATION);
        //Tao chuoi JWT tu userName
        return Jwts.builder().setSubject(customUserDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(dateExpired)
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET).compact();
    }

    //Lay thong tin user tu jwt
    public String getUserNameFromJwt(String token) {
        Claims claims = Jwts.parser().setSigningKey(JWT_SECRET)
                .parseClaimsJws(token).getBody();
        //tra lai thong tin username
        return claims.getSubject();
    }

    //Validate thong tin cua JWT
    public boolean validateToken(String token) {
        System.out.println(JWT_SECRET);
        System.out.println(token);
        try {
            Jwts.parser().setSigningKey(JWT_SECRET)
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT Token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT Token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT Token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims String is empty");
        }
        return false;
    }
}
