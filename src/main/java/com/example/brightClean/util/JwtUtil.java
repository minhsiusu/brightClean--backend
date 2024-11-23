// package com.example.brightClean.util;

// import java.util.Calendar;
// import java.util.Date;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.function.Function;

// import org.springframework.security.core.Authentication;
// import org.springframework.stereotype.Component;

// import com.example.brightClean.domain.User;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.security.Keys;

// @Component
// public class JwtUtil {
//     // private static final String ISS = "Hogwarts";
//     // private static final String SECRET = "AlohomoraIsASpellUsedToOpenDoors";
//     // private static final int EXPIRE_TIME = 5;

//     // public static String generateToken(Authentication authentication) {
//     //     User myUser = (User) authentication.getPrincipal();
//     //     Calendar exp = Calendar.getInstance();
//     //     exp.add(Calendar.MINUTE, EXPIRE_TIME);

//     //     Claims claims = (Claims) Jwts.claims();
//     //     claims.setSubject(myUser.getAccount());
//     //     claims.setExpiration(exp.getTime());
//     //     claims.setIssuer(ISS);
//     //     Key secretKey = Keys.hmacShaKeyFor(SECRET.getBytes());

//     //     return Jwts.builder()
//     //             .setClaims(claims)
//     //             .signWith(secretKey)
//     //             .compact(); // 將 JwtBuilder 構建的 JWT 物件，壓縮為一個字串的形式
//     // }
//     private String SECRET_KEY = "secret";

//     public String extractUsername(String token) {
//         return extractClaim(token, Claims::getSubject);
//     }

//     public Date extractExpiration(String token) {
//         return extractClaim(token, Claims::getExpiration);
//     }

//     public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//         final Claims claims = extractAllClaims(token);
//         return claimsResolver.apply(claims);
//     }

//     private Claims extractAllClaims(String token) {
//         return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
//     }

//     private Boolean isTokenExpired(String token) {
//         return extractExpiration(token).before(new Date());
//     }

//     public String generateToken(String username) {
//         Map<String, Object> claims = new HashMap<>();
//         return createToken(claims, username);
//     }

//     private String createToken(Map<String, Object> claims, String subject) {
//         return Jwts.builder()
//                 .setClaims(claims)
//                 .setSubject(subject)
//                 .setIssuedAt(new Date(System.currentTimeMillis()))
//                 .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
//                 .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
//                 .compact();
//     }

//     public Boolean validateToken(String token, String username) {
//         final String extractedUsername = extractUsername(token);
//         return (extractedUsername.equals(username) && !isTokenExpired(token));
//     }
// }
