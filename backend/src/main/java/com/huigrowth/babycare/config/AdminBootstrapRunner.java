package com.huigrowth.babycare.config;

import com.huigrowth.babycare.entity.DataDict;
import com.huigrowth.babycare.entity.Menu;
import com.huigrowth.babycare.entity.Role;
import com.huigrowth.babycare.entity.RoleMenuRelation;
import com.huigrowth.babycare.entity.SystemConfig;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.entity.UserRoleRelation;
import com.huigrowth.babycare.repository.DataDictRepository;
import com.huigrowth.babycare.repository.MenuRepository;
import com.huigrowth.babycare.repository.PermissionRepository;
import com.huigrowth.babycare.repository.RoleMenuRelationRepository;
import com.huigrowth.babycare.repository.RoleRepository;
import com.huigrowth.babycare.repository.SystemConfigRepository;
import com.huigrowth.babycare.repository.UserRepository;
import com.huigrowth.babycare.repository.UserRoleRelationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 系统初始化引导器
 * 负责创建默认管理员账号和初始 RBAC 数据（角色、菜单、权限、配置）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRelationRepository userRoleRelationRepository;
    private final RoleMenuRelationRepository roleMenuRelationRepository;
    private final DataDictRepository dataDictRepository;
    private final SystemConfigRepository systemConfigRepository;

    @Value("${app.bootstrap.admin.enabled:false}")
    private boolean enabled;

    @Value("${app.bootstrap.admin.username:admin}")
    private String adminUsername;

    @Value("${app.bootstrap.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.bootstrap.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.nickname:系统管理员}")
    private String adminNickname;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        initRoles();
        initSystemMenus();
        initSystemConfig();
        initDataDictTypes();
        initAdminUser();
        log.info("系统初始化完成：角色、菜单、配置、字典和管理员账号");
    }

    // ========== 初始化默认角色 ==========
    private void initRoles() {
        if (roleRepository.count() > 0) return;

        List<Role> roles = Arrays.asList(
            createRole("系统管理员", "PLATFORM_ADMIN", "平台运维与权限配置", true),
            createRole("老板/机构管理员", "ORG_ADMIN", "多园区经营统筹", false),
            createRole("园长", "DIRECTOR", "园区运营管理", false),
            createRole("教师", "TEACHER", "班级教学与家园沟通", false),
            createRole("保育员", "CAREGIVER", "一日照护记录", false),
            createRole("保健员", "HEALTH_WORKER", "卫生保健与异常处理", false),
            createRole("安全后勤", "SAFETY_OFFICER", "安全台账与巡检", false),
            createRole("财务运营", "FINANCE", "收费与招生运营", false),
            createRole("家长", "PARENT", "查看宝宝在园情况", false),
            createRole("长辈", "GRANDPARENT", "查看宝宝动态", false)
        );
        roleRepository.saveAll(roles);
        log.info("已初始化 {} 个系统角色", roles.size());
    }

    private Role createRole(String name, String code, String description, boolean system) {
        Role role = new Role();
        role.setName(name);
        role.setCode(code);
        role.setDescription(description);
        role.setType(system ? Role.RoleType.SYSTEM : Role.RoleType.CUSTOM);
        role.setIsSystem(system);
        role.setStatus(Role.RoleStatus.ACTIVE);
        return role;
    }

    // ========== 初始化系统菜单 ==========
    private void initSystemMenus() {
        if (menuRepository.count() > 0) return;

        // 一级目录：系统管理
        Menu sysDir = createMenu("系统管理", "/system-management", "SettingOutlined", null, 1, Menu.MenuType.DIR);
        menuRepository.save(sysDir);

        // 系统管理子菜单
        List<Menu> sysMenus = Arrays.asList(
            createMenu("角色管理", "/system-management?tab=roles", "TeamOutlined", sysDir.getId(), 1, Menu.MenuType.MENU),
            createMenu("权限管理", "/system-management?tab=permissions", "SafetyOutlined", sysDir.getId(), 2, Menu.MenuType.MENU),
            createMenu("菜单管理", "/system-management?tab=menus", "MenuOutlined", sysDir.getId(), 3, Menu.MenuType.MENU),
            createMenu("用户管理", "/system-management?tab=users", "UserOutlined", sysDir.getId(), 4, Menu.MenuType.MENU),
            createMenu("审计日志", "/system-management?tab=audit-log", "FileSearchOutlined", sysDir.getId(), 5, Menu.MenuType.MENU),
            createMenu("数据字典", "/system-management?tab=data-dict", "BookOutlined", sysDir.getId(), 6, Menu.MenuType.MENU),
            createMenu("系统配置", "/system-management?tab=config", "SettingOutlined", sysDir.getId(), 7, Menu.MenuType.MENU)
        );
        menuRepository.saveAll(sysMenus);

        // 为系统管理员角色关联所有菜单
        roleRepository.findByCode("PLATFORM_ADMIN").ifPresent(role -> {
            List<Menu> allMenus = menuRepository.findAll();
            for (Menu menu : allMenus) {
                RoleMenuRelation rm = new RoleMenuRelation();
                rm.setRoleId(role.getId());
                rm.setMenuId(menu.getId());
                roleMenuRelationRepository.save(rm);
            }
        });

        log.info("已初始化系统菜单和关联");
    }

    private Menu createMenu(String name, String route, String icon, Long parentId, int sort, Menu.MenuType type) {
        Menu menu = new Menu();
        menu.setName(name);
        menu.setRoute(route);
        menu.setIcon(icon);
        menu.setParentId(parentId);
        menu.setSortOrder(sort);
        menu.setMenuType(type);
        menu.setVisible(true);
        menu.setStatus(Menu.MenuStatus.ACTIVE);
        return menu;
    }

    // ========== 初始化系统配置 ==========
    private void initSystemConfig() {
        if (systemConfigRepository.count() > 0) return;

        List<SystemConfig> configs = Arrays.asList(
            createConfig("site.name", "好芽儿托育保育平台", "站点名称", "basic"),
            createConfig("site.logo", "/logo.svg", "站点 Logo", "basic"),
            createConfig("security.jwt.expiration", "86400000", "JWT 过期时间(毫秒)", "security"),
            createConfig("file.upload.max-size", "10485760", "文件上传最大字节", "file"),
            createConfig("notification.push.enabled", "true", "是否启用推送通知", "notification")
        );
        systemConfigRepository.saveAll(configs);
        log.info("已初始化系统配置项");
    }

    private SystemConfig createConfig(String key, String value, String name, String group) {
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setConfigName(name);
        config.setConfigGroup(group);
        config.setStatus(SystemConfig.ConfigStatus.ACTIVE);
        return config;
    }

    // ========== 初始化数据字典类型示例 ==========
    private void initDataDictTypes() {
        if (dataDictRepository.count() > 0) return;

        List<DataDict> dicts = Arrays.asList(
            createDict("CERT_TYPE", "证件类型", "ID_CARD", "身份证", 1),
            createDict("CERT_TYPE", "证件类型", "PASSPORT", "护照", 2),
            createDict("CERT_TYPE", "证件类型", "HK_MACAO", "港澳居民来往内地通行证", 3),
            createDict("NATION", "民族", "HAN", "汉族", 1),
            createDict("NATION", "民族", "MINORITY", "少数民族", 2),
            createDict("PAYMENT_METHOD", "缴费方式", "WECHAT", "微信支付", 1),
            createDict("PAYMENT_METHOD", "缴费方式", "ALIPAY", "支付宝", 2),
            createDict("PAYMENT_METHOD", "缴费方式", "BANK", "银行转账", 3),
            createDict("PAYMENT_METHOD", "缴费方式", "CASH", "现金", 4),
            createDict("RELATIONSHIP", "关系", "FATHER", "父亲", 1),
            createDict("RELATIONSHIP", "关系", "MOTHER", "母亲", 2),
            createDict("RELATIONSHIP", "关系", "GRANDFATHER", "祖父", 3),
            createDict("RELATIONSHIP", "关系", "GRANDMOTHER", "祖母", 4),
            createDict("RELATIONSHIP", "关系", "OTHER", "其他", 5)
        );
        dataDictRepository.saveAll(dicts);
        log.info("已初始化数据字典项");
    }

    private DataDict createDict(String type, String typeName, String code, String value, int sort) {
        DataDict dict = new DataDict();
        dict.setDictType(type);
        dict.setDictName(typeName);
        dict.setItemCode(code);
        dict.setItemValue(value);
        dict.setSortOrder(sort);
        dict.setStatus(DataDict.DictStatus.ACTIVE);
        return dict;
    }

    // ========== 初始化管理员账号 ==========
    private void initAdminUser() {
        String normalizedUsername = adminUsername.trim();
        if (userRepository.existsByUsername(normalizedUsername)) {
            return;
        }

        User admin = new User();
        admin.setUsername(normalizedUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setEmail(StringUtils.hasText(adminEmail) ? adminEmail.trim() : normalizedUsername + "@example.com");
        admin.setNickname(StringUtils.hasText(adminNickname) ? adminNickname.trim() : normalizedUsername);
        admin.setRole(User.UserRole.ADMIN);
        admin.setEnabled(true);
        admin.setEmailVerified(true);
        admin.setPhoneVerified(false);
        final User savedAdmin = userRepository.save(admin);

        // 为管理员分配 PLATFORM_ADMIN 角色
        roleRepository.findByCode("PLATFORM_ADMIN").ifPresent(role -> {
            UserRoleRelation ur = new UserRoleRelation();
            ur.setUserId(savedAdmin.getId());
            ur.setRoleId(role.getId());
            userRoleRelationRepository.save(ur);
        });

        log.info("已创建开发环境管理员账号：{}，并分配系统管理员角色", normalizedUsername);
    }
}
