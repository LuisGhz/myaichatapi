package dev.luisghtz.myaichat.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String roleName;
    private String avatarUrl;
    private Boolean locked;
    private Boolean disabled;
}
