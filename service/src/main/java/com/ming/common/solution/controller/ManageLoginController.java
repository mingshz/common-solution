package com.ming.common.solution.controller;

import com.ming.common.solution.entity.User;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.field.Fields;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author CJ
 */
@Controller
@RequestMapping("/users")
@RowCustom(dramatizer = AntDesignPaginationDramatizer.class, distinct = true)
public class ManageLoginController extends AbstractCrudController<User, Long> {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    protected void preparePersist(User data, Map<String, Object> otherData) {
        super.preparePersist(data, otherData);
        data.setPassword(passwordEncoder.encode((CharSequence) otherData.get("rawPassword")));
        data.setEnabled(true);
    }

    @Override
    protected void prepareRemove(User entity) {
        super.prepareRemove(entity);
        if ("root".equalsIgnoreCase(entity.getUsername()))
            throw new IllegalArgumentException("root 是无法被删除的。");
    }

    @Override
    protected List<FieldDefinition<User>> listFields() {
        return Arrays.asList(
//                FieldBuilder.asName(User.class, "loginName")
//                        .addSelect(userRoot -> userRoot.get(User_.username))
//                        .build(),
//                FieldBuilder.asName(User.class, "name")
//                        .addSelect(userRoot -> userRoot.get(User_.username))
//                        .build(),
//                FieldBuilder.asName(User.class, "currentAuthority")
//                        .addBiSelect((userRoot, criteriaBuilder) -> criteriaBuilder.literal("user"))
//                        .build(),
//                FieldBuilder.asName(User.class, "avatar")
//                        .addBiSelect((userRoot, criteriaBuilder) -> criteriaBuilder.literal("https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png"))
//                        .build(),
                Fields.asBasic("id"),
                Fields.asBasic("username"),
                FieldBuilder.asName(User.class, "role")
                        .addFormat((data, type) -> data.toString())
                        .build()
        );
    }

    @Override
    protected Specification<User> listSpecification(Map<String, Object> map) {
        return null;
    }
}
