package com.selimhorri.app.integration;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.repository.CredentialRepository;
import com.selimhorri.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Test
    void testSaveAndFindUser() {
        Credential credential = Credential.builder().username("integrationUser").password("pass").isEnabled(true).isAccountNonExpired(true).isAccountNonLocked(true).isCredentialsNonExpired(true).build();
        User user = User.builder().firstName("Jane").lastName("Smith").email("jane@smith.com").credential(credential).build();

        credential.setUser(user);

        user = userRepository.save(user);

        Optional<User> found = userRepository.findById(user.getUserId());
        assertTrue(found.isPresent());
        assertEquals("Jane", found.get().getFirstName());
        assertEquals("integrationUser", found.get().getCredential().getUsername());
    }

    @Test
    void testDeleteUser() {
        Credential credential = Credential.builder().username("deleteUser").password("pass").isEnabled(true).isAccountNonExpired(true).isAccountNonLocked(true).isCredentialsNonExpired(true).build();
        User user = User.builder().firstName("Delete").lastName("Me").email("delete@me.com").credential(credential).build();

        credential.setUser(user);

        user = userRepository.save(user);
        Integer userId = user.getUserId();

        userRepository.deleteById(userId);

        Optional<User> found = userRepository.findById(userId);
        assertFalse(found.isPresent());
    }

    @Test
    void testSaveAndFindCredential() {
        Credential credential = Credential.builder().username("credUser").password("pass").isEnabled(true).isAccountNonExpired(true).isAccountNonLocked(true).isCredentialsNonExpired(true).build();

        credential = credentialRepository.save(credential);

        Optional<Credential> found = credentialRepository.findById(credential.getCredentialId());
        assertTrue(found.isPresent());
        assertEquals("credUser", found.get().getUsername());
    }

}