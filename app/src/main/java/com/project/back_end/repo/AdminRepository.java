package com.project.back_end.repo;

import com.project.back_end.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for performing CRUD operations on Patient entities.
 * Extends JpaRepository to inherit standard data access methods
 * (save, findById, findAll, delete, etc.) without requiring implementation.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Retrieves a patient by their unique email address.
     * Used primarily during patient login and authentication, where the
     * email serves as the identifying credential to look up the account.
     *
     * @param email the email address associated with the patient's account
     * @return the matching Patient, or null if no patient is found with that email
     */
    Patient findByEmail(String email);

    /**
     * Retrieves a patient by matching either their email address or phone number.
     * Useful for scenarios such as account recovery or duplicate-registration
     * checks, where a user may attempt to identify themselves using either
     * piece of contact information instead of just their email.
     *
     * @param email the email address to match
     * @param phone the phone number to match
     * @return the matching Patient if either the email or phone matches, or null if none is found
     */
    Patient findByEmailOrPhone(String email, String phone);
}
