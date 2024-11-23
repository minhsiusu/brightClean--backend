package com.example.brightClean.domain.params;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserParam {

    @NotBlank(message = "名稱不為空")
    @Size( max = 10)
    private String name;

    @NotBlank(message = "帳戶不為空")
    @Pattern(regexp = "^[A-Za-z0-9]+$")
    private String account;

    @NotBlank(message = "密碼不為空")
    @Size(min = 3,max = 10 ,message = "密碼長度需在3~10之間")
    @Pattern(regexp = "^[A-Za-z0-9]+$")
    private String password;

    @NotBlank(message = "信箱不為空")
    @Email
    private String email;

    @NotBlank(message = "手機不為空")
    private String cellPhone;
    
    private String address;
}
