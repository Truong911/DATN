package com.phamtruong.rookbooks.controller.admin;

import com.phamtruong.rookbooks.controller.common.BaseController;
import com.phamtruong.rookbooks.service.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@AllArgsConstructor
@Controller
@RequestMapping("/admin/roles_management")
public class AdminRoleController extends BaseController {
    private final RoleService roleService;
}

