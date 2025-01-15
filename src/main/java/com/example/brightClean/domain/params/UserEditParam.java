package com.example.brightClean.domain.params;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserEditParam {
    @NotBlank(message = "名稱不為空")
    @Size(max = 10, message = "名稱長度不得超過10個字符")
    private String name;

    @Pattern(regexp = "^[0-9]+$", message = "手機只能包含數字")
    private String cellPhone;

    @Size(max = 50, message = "地址長度不得超過50個字符")
    private String address;
}
