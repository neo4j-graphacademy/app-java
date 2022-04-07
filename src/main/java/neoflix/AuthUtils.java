package neoflix;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class AuthUtils {

    public static String encryptPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }
    public static boolean verifyPassword(String password, String hashed) {
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashed);
        return result.verified;
    }

    public static String verify(String token, String secret) {
        // todo reuse
        Algorithm algorithm = Algorithm.HMAC256(secret);
        // todo reuse
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("auth0")
                .build(); //Reusable verifier instance
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getSubject(); // sub == userId
    }

    public static String sign(String sub, Map<String,Object> data, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE,1);
            String token = JWT.create()
                    .withClaim(sub, data)
                    .withIssuer("auth0")
                    .withSubject(sub)
                    .withIssuedAt(new Date())
                    .withExpiresAt(cal.getTime())
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException exception){
            //Invalid Signing configuration / Couldn't convert Claims.
            throw new RuntimeException(exception);
        }
    }
}
