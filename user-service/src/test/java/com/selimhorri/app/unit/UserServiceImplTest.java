package com.selimhorri.app.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.service.impl.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById_ReturnsUserDto() {
        Credential credential = Credential.builder().credentialId(1).username("testuser").password("pass").isEnabled(true).isAccountNonExpired(true).isAccountNonLocked(true).isCredentialsNonExpired(true).build();
        User user = User.builder().userId(1).firstName("John").lastName("Doe").email("john@doe.com").credential(credential).build();

        credential.setUser(user);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        UserDto result = userService.findById(1);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("testuser", result.getCredentialDto().getUsername());
    }

    @Test
    void testSave_ReturnsSavedUserDto() {
        CredentialDto credentialDto = CredentialDto.builder().credentialId(1).username("testuser").password("pass").isEnabled(true).isAccountNonExpired(true).isAccountNonLocked(true).isCredentialsNonExpired(true).build();
        UserDto userDto = UserDto.builder().userId(1).firstName("John").lastName("Doe").email("john@doe.com").credentialDto(credentialDto).build();
        Credential credential = Credential.builder().credentialId(1).username("testuser").password("pass").isEnabled(true).isAccountNonExpired(true).isAccountNonLocked(true).isCredentialsNonExpired(true).build();
        User user = User.builder().userId(1).firstName("John").lastName("Doe").email("john@doe.com").credential(credential).build();

        credential.setUser(user);

        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.save(userDto);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("testuser", result.getCredentialDto().getUsername());
    }

}