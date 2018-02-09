package com.ming.common.solution.model;

import com.ming.common.solution.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author CJ
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserCreation extends User {
    private String rawPassword;
}
