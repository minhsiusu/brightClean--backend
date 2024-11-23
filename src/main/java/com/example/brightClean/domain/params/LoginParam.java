package com.example.brightClean.domain.params;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LoginParam {

    @NotBlank(message = "帳戶不為空")
    // @Pattern(regexp = "^[A-Za-z0-9]+$")
    private String email;

    @NotBlank(message = "密碼不為空")
    // @Size(min = 3,max = 10 ,message = "密碼長度需在3~10之間")
    // @Pattern(regexp = "^[A-Za-z0-9]+$")
    private String password;
}
