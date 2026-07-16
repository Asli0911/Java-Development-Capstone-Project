package com.project.back_end.services;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public TokenService(AdminRepository adminRepository, DoctorRepository doctorRepository, PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    // Return type changed to SecretKey to fix verifyWith(...) issue
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generates a signed JWT containing the user's email as the subject.
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7))
                .signWith(getSigningKey()) // clean & modern
                .compact();
    }

    /**
     * Extracts the email (subject) stored in the token's claims.
     */
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // No more error now
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Validates that the token is well-formed, unexpired, and belongs to
     * an existing user of the given role (admin, doctor, or patient).
     */
    public boolean validateToken(String token, String user) {
        try {
            String extracted = extractEmail(token);

            if (user.equals("admin")) {
                Admin admin = adminRepository.findByUsername(extracted);
                if (admin != null) {
                    return true;
                }
            } else if (user.equals("doctor")) {
                Doctor doctor = doctorRepository.findByEmail(extracted);
                if (doctor != null) {
                    return true;
                }
            } else if (user.equals("patient")) {
                Patient patient = patientRepository.findByEmail(extracted);
                if (patient != null) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Alias for extractEmail(token), provided for callers that expect
     * this method name. Delegates to the existing extraction logic
     * rather than duplicating it.
     */
    public String extractEmailFromToken(String token) {
        return extractEmail(token);
    }

    /**
     * Extracts the doctor's ID associated with the token by first
     * pulling the email out of the token's claims, then looking up
     * the corresponding Doctor record.
     *
     * @param token the JWT to inspect
     * @return the doctor's ID, or null if the token is invalid or no
     *         matching doctor is found
     */
    public Long extractDoctorIdFromToken(String token) {
        try {
            String email = extractEmail(token);
            Doctor doctor = doctorRepository.findByEmail(email);
            return (doctor != null) ? doctor.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
