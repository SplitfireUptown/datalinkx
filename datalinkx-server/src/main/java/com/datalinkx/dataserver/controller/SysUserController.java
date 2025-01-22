package com.datalinkx.dataserver.controller;

import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.common.result.WebResult;
import com.datalinkx.dataserver.bean.domain.ImageBean;
import com.datalinkx.dataserver.bean.domain.SysRoleBean;
import com.datalinkx.dataserver.bean.domain.SysUserBean;
import com.datalinkx.dataserver.repository.ImageRepository;
import com.datalinkx.dataserver.repository.SysRoleRepository;
import com.datalinkx.dataserver.service.ISysUserService;
import com.datalinkx.dataserver.utils.RSAEncrypt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static com.datalinkx.dataserver.utils.SecurityUtils.*;


/**
 * 用户信息
 *
 * @author ruoyi
 */
@Slf4j
@RestController
@RequestMapping("/system/user")
public class SysUserController {
    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysRoleRepository sysRoleRepository;

    @Autowired
    private ImageRepository imageRepository;

    /**
     * 根据用户编号获取详细信息
     */
    @GetMapping(value = {"/", "/{userId}"})
    public WebResult<HashMap<String, Object>> getInfo(@PathVariable(value = "userId", required = false) String userId) {
        HashMap<String, Object> resultMap = new HashMap<>();
        SysUserBean sysUserBean;
        if (Objects.isNull(userId)) {
            userId = getUserId();
        }
        sysUserBean = userService.selectUserById(userId);
        ArrayList<SysRoleBean> sysRoleBean = new ArrayList<>(sysRoleRepository.selectRoleByUserId(userId));
        Integer i = StringUtils.isNotEmpty(sysUserBean.getAvatar()) ? Integer.parseInt(sysUserBean.getAvatar()) : -1;
        imageRepository.findById(i).ifPresent(imageBean -> {
            byte[] imageData = imageBean.getData();
            // 将二进制数据转换为Base64编码
            String encodedImage = Base64.getEncoder().encodeToString(imageData);
            resultMap.put("avatar", encodedImage);
        });
        resultMap.put("user", sysUserBean);
        resultMap.put("roles", sysRoleBean);
        return WebResult.of(resultMap);
    }

    /**
     * 获取用户列表
     *
     * @param user
     * @return
     */
    @GetMapping("/list")
    public WebResult<List<SysUserBean>> list() {
        return WebResult.of(userService.selectUserList());
    }

    /**
     * 修改用户
     *
     * @param user
     */
    @PutMapping
    public WebResult<Integer> edit(@Validated @RequestBody SysUserBean user) {
//        if (!userService.checkUserNameUnique(user))
//        {
//            return WebResult.fail(new DatalinkXServerException("修改用户'" + user.getNickName() + "'失败，登录账号已存在"));
//        }
//        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
//        {
//            return WebResult.fail(new DatalinkXServerException("修改用户'" + user.getEmail() + "'失败，邮箱账号已存在"));
//        }
//        else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
//        {
//            return WebResult.fail(new DatalinkXServerException("修改用户'" + user.getUserName() + "'失败，手机号码已存在"));
//        }
        user.setUpdateBy(getUserId());
        return WebResult.of(userService.updateUser(user));
    }

    /**
     * 修改用户头像
     *
     * @param file
     * @return
     */
    @PostMapping(value = "/avatar")
    public WebResult<HashMap<String, String>> avatar(@RequestParam("file") MultipartFile file) {
        try {
            // 获取文件的原始名称
            String fileName = file.getOriginalFilename();
            // 获取文件的字节数组
            byte[] fileData = file.getBytes();
            // 保存图片
            ImageBean imageBean = new ImageBean();
            imageBean.setName(fileName);
            imageBean.setData(fileData);
            imageBean.setIsDel("0");
            Integer id = imageRepository.save(imageBean).getId();
            userService.updateUserAvatar(getUserId(), String.valueOf(id));
            HashMap<String, String> resultMap = new HashMap<>();
            resultMap.put("id", id.toString());
            return WebResult.of(resultMap);
        } catch (IOException e) {
            e.printStackTrace();
            return WebResult.fail(new DatalinkXServerException("上传图片失败"));
        }
    }

    /**
     * 重置密码
     *
     * @param user
     * @return
     */
    @PutMapping("/resetUserPwd")
    public WebResult<Integer> resetUserPwd(@RequestBody SysUserBean user) throws Exception {
        if (Objects.equals(user.getUserId(), getUserId()))
            return WebResult.of(userService.resetUserPwd(getUsername(), encryptPassword(RSAEncrypt.decrypt(user.getPassword())), user.getPasswordLevel()));
        else
            return WebResult.fail(new DatalinkXServerException("只能重置自己的密码"));
    }
}
