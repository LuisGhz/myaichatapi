package dev.luisghtz.myaichat.auth.services;

import dev.luisghtz.myaichat.auth.dtos.UserDto;
import dev.luisghtz.myaichat.auth.entities.Role;
import dev.luisghtz.myaichat.auth.entities.User;
import dev.luisghtz.myaichat.auth.repositories.RoleRepository;
import dev.luisghtz.myaichat.auth.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role userRole;
    private Map<String, Object> githubUserData;

    @BeforeEach
    void setUp() {
        // Setup test data
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(Role.RoleName.USER);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setGithubId("123456");
        testUser.setAvatarUrl("https://github.com/avatar.jpg");
        testUser.setLocked(false);
        testUser.setDisabled(false);
        testUser.setRole(userRole);

        githubUserData = new HashMap<>();
        githubUserData.put("id", 123456);
        githubUserData.put("login", "testuser");
        githubUserData.put("email", "test@example.com");
        githubUserData.put("avatar_url", "https://github.com/avatar.jpg");
    }

    @Nested
    @DisplayName("Create Or Update User From GitHub Tests")
    class CreateOrUpdateUserFromGitHubTests {

        @Test
        @DisplayName("createOrUpdateUserFromGitHub - Should create new user when user doesn't exist")
        void testCreateOrUpdateUserFromGitHub_ShouldCreateNewUserWhenUserDoesNotExist() {
            // Arrange
            when(userRepository.findByGithubId("123456")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
            when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.of(userRole));
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            User result = userService.createOrUpdateUserFromGitHub(githubUserData);

            // Assert
            assertNotNull(result);
            verify(userRepository).findByGithubId("123456");
            verify(userRepository).findByEmail("test@example.com");
            verify(roleRepository).findByName(Role.RoleName.USER);
            verify(userRepository).existsByUsername("testuser");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("createOrUpdateUserFromGitHub - Should update existing user found by GitHub ID")
        void testCreateOrUpdateUserFromGitHub_ShouldUpdateExistingUserFoundByGitHubId() {
            // Arrange
            User existingUser = new User();
            existingUser.setId(UUID.randomUUID());
            existingUser.setGithubId("123456");
            existingUser.setUsername("oldusername");
            existingUser.setEmail("old@example.com");
            existingUser.setRole(userRole);

            when(userRepository.findByGithubId("123456")).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(existingUser);

            // Act
            User result = userService.createOrUpdateUserFromGitHub(githubUserData);

            // Assert
            assertNotNull(result);
            verify(userRepository).findByGithubId("123456");
            verify(userRepository, never()).findByEmail(anyString());
            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("createOrUpdateUserFromGitHub - Should update existing user found by email when GitHub ID not found")
        void testCreateOrUpdateUserFromGitHub_ShouldUpdateExistingUserFoundByEmail() {
            // Arrange
            User existingUser = new User();
            existingUser.setId(UUID.randomUUID());
            existingUser.setEmail("test@example.com");
            existingUser.setUsername("oldusername");
            existingUser.setRole(userRole);

            when(userRepository.findByGithubId("123456")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(existingUser);

            // Act
            User result = userService.createOrUpdateUserFromGitHub(githubUserData);

            // Assert
            assertNotNull(result);
            verify(userRepository).findByGithubId("123456");
            verify(userRepository).findByEmail("test@example.com");
            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("createOrUpdateUserFromGitHub - Should generate unique username when username already exists")
        void testCreateOrUpdateUserFromGitHub_ShouldGenerateUniqueUsernameWhenUsernameExists() {
            // Arrange
            when(userRepository.findByGithubId("123456")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
            when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.of(userRole));
            when(userRepository.existsByUsername("testuser")).thenReturn(true);
            when(userRepository.existsByUsername("testuser1")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            User result = userService.createOrUpdateUserFromGitHub(githubUserData);

            // Assert
            assertNotNull(result);
            verify(userRepository).existsByUsername("testuser");
            verify(userRepository).existsByUsername("testuser1");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("createOrUpdateUserFromGitHub - Should generate email when email is null")
        void testCreateOrUpdateUserFromGitHub_ShouldGenerateEmailWhenEmailIsNull() {
            // Arrange
            Map<String, Object> githubDataWithoutEmail = new HashMap<>(githubUserData);
            githubDataWithoutEmail.put("email", null);

            when(userRepository.findByGithubId("123456")).thenReturn(Optional.empty());
            when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.of(userRole));
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            User result = userService.createOrUpdateUserFromGitHub(githubDataWithoutEmail);

            // Assert
            assertNotNull(result);
            verify(userRepository).findByGithubId("123456");
            verify(userRepository, never()).findByEmail(anyString());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("createOrUpdateUserFromGitHub - Should throw exception when default role not found")
        void testCreateOrUpdateUserFromGitHub_ShouldThrowExceptionWhenDefaultRoleNotFound() {
            // Arrange
            when(userRepository.findByGithubId("123456")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
            when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> userService.createOrUpdateUserFromGitHub(githubUserData));
            
            assertEquals("Default role not found", exception.getMessage());
            verify(roleRepository).findByName(Role.RoleName.USER);
        }

        @Test
        @DisplayName("createOrUpdateUserFromGitHub - Should handle null username by generating default")
        void testCreateOrUpdateUserFromGitHub_ShouldHandleNullUsernameByGeneratingDefault() {
            // Arrange
            Map<String, Object> githubDataWithoutUsername = new HashMap<>(githubUserData);
            githubDataWithoutUsername.put("login", null);

            when(userRepository.findByGithubId("123456")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
            when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.of(userRole));
            when(userRepository.existsByUsername("user")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            User result = userService.createOrUpdateUserFromGitHub(githubDataWithoutUsername);

            // Assert
            assertNotNull(result);
            verify(userRepository).existsByUsername("user");
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Convert To DTO Tests")
    class ConvertToDtoTests {

        @Test
        @DisplayName("convertToDto - Should convert User entity to UserDto correctly")
        void testConvertToDto_ShouldConvertUserEntityToUserDtoCorrectly() {
            // Act
            UserDto result = userService.convertToDto(testUser);

            // Assert
            assertNotNull(result);
            assertEquals(testUser.getId(), result.getId());
            assertEquals(testUser.getUsername(), result.getUsername());
            assertEquals(testUser.getEmail(), result.getEmail());
            assertEquals(testUser.getRole().getName().name(), result.getRoleName());
            assertEquals(testUser.getAvatarUrl(), result.getAvatarUrl());
            assertEquals(testUser.getLocked(), result.getLocked());
            assertEquals(testUser.getDisabled(), result.getDisabled());
        }

        @Test
        @DisplayName("convertToDto - Should handle User with ADMIN role")
        void testConvertToDto_ShouldHandleUserWithAdminRole() {
            // Arrange
            Role adminRole = new Role();
            adminRole.setId(2L);
            adminRole.setName(Role.RoleName.ADMIN);
            testUser.setRole(adminRole);

            // Act
            UserDto result = userService.convertToDto(testUser);

            // Assert
            assertNotNull(result);
            assertEquals("ADMIN", result.getRoleName());
        }

        @Test
        @DisplayName("convertToDto - Should handle User with null avatar URL")
        void testConvertToDto_ShouldHandleUserWithNullAvatarUrl() {
            // Arrange
            testUser.setAvatarUrl(null);

            // Act
            UserDto result = userService.convertToDto(testUser);

            // Assert
            assertNotNull(result);
            assertNull(result.getAvatarUrl());
        }
    }

    @Nested
    @DisplayName("Find By ID Tests")
    class FindByIdTests {

        @Test
        @DisplayName("findById - Should return user when valid UUID provided and user exists")
        void testFindById_ShouldReturnUserWhenValidUuidProvidedAndUserExists() {
            // Arrange
            UUID userId = testUser.getId();
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // Act
            Optional<User> result = userService.findById(userId.toString());

            // Assert
            assertTrue(result.isPresent());
            assertEquals(testUser, result.get());
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("findById - Should return empty when valid UUID provided but user doesn't exist")
        void testFindById_ShouldReturnEmptyWhenValidUuidProvidedButUserDoesNotExist() {
            // Arrange
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act
            Optional<User> result = userService.findById(userId.toString());

            // Assert
            assertFalse(result.isPresent());
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("findById - Should return empty when invalid UUID format provided")
        void testFindById_ShouldReturnEmptyWhenInvalidUuidFormatProvided() {
            // Act
            Optional<User> result = userService.findById("invalid-uuid");

            // Assert
            assertFalse(result.isPresent());
            verify(userRepository, never()).findById(any());
        }

        @Test
        @DisplayName("findById - Should return empty when empty string ID provided")
        void testFindById_ShouldReturnEmptyWhenEmptyStringIdProvided() {
            // Act
            Optional<User> result = userService.findById("");

            // Assert
            assertFalse(result.isPresent());
            verify(userRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("Generate Unique Username Tests")
    class GenerateUniqueUsernameTests {

        @Test
        @DisplayName("generateUniqueUsername - Should return original username when not taken")
        void testGenerateUniqueUsername_ShouldReturnOriginalUsernameWhenNotTaken() {
            // Arrange
            Map<String, Object> githubData = new HashMap<>(githubUserData);
            githubData.put("login", "uniqueuser");
            
            when(userRepository.findByGithubId("123456")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
            when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.of(userRole));
            when(userRepository.existsByUsername("uniqueuser")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            User result = userService.createOrUpdateUserFromGitHub(githubData);

            // Assert
            assertNotNull(result);
            verify(userRepository).existsByUsername("uniqueuser");
            verify(userRepository, never()).existsByUsername("uniqueuser1");
        }

        @Test
        @DisplayName("generateUniqueUsername - Should append number when username taken multiple times")
        void testGenerateUniqueUsername_ShouldAppendNumberWhenUsernameTakenMultipleTimes() {
            // Arrange
            when(userRepository.findByGithubId("123456")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
            when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.of(userRole));
            when(userRepository.existsByUsername("testuser")).thenReturn(true);
            when(userRepository.existsByUsername("testuser1")).thenReturn(true);
            when(userRepository.existsByUsername("testuser2")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            User result = userService.createOrUpdateUserFromGitHub(githubUserData);

            // Assert
            assertNotNull(result);
            verify(userRepository).existsByUsername("testuser");
            verify(userRepository).existsByUsername("testuser1");
            verify(userRepository).existsByUsername("testuser2");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesAndErrorHandlingTests {

        @Test
        @DisplayName("createOrUpdateUserFromGitHub - Should handle repository exception")
        void testCreateOrUpdateUserFromGitHub_ShouldHandleRepositoryException() {
            // Arrange
            when(userRepository.findByGithubId("123456")).thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> userService.createOrUpdateUserFromGitHub(githubUserData));
            
            verify(userRepository).findByGithubId("123456");
        }

        @Test
        @DisplayName("convertToDto - Should handle null user")
        void testConvertToDto_ShouldHandleNullUser() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> userService.convertToDto(null));
        }

        @Test
        @DisplayName("createOrUpdateUserFromGitHub - Should handle empty GitHub data")
        void testCreateOrUpdateUserFromGitHub_ShouldHandleEmptyGitHubData() {
            // Arrange
            Map<String, Object> emptyGithubData = new HashMap<>();
            
            when(userRepository.findByGithubId("null")).thenReturn(Optional.empty());
            when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.of(userRole));
            when(userRepository.existsByUsername("user")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            User result = userService.createOrUpdateUserFromGitHub(emptyGithubData);

            // Assert
            assertNotNull(result);
            verify(userRepository).findByGithubId("null");
        }
    }
}
