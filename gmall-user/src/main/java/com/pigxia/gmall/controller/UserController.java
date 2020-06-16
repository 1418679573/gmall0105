package com.pigxia.gmall.controller;




import com.pigxia.gmall.bean.UmsMember;
import com.pigxia.gmall.bean.UmsMemberReceiveAddress;
import com.pigxia.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by absen on 2020/5/26 19:02
 */
@Controller
public class UserController {

    @Autowired
    UserService userService;

    @ResponseBody
    @RequestMapping("/getAllUsers")
    public List<UmsMember> geAllUsers(){
        List<UmsMember> members=userService.getAllUsers();

        return members;
    }
    @ResponseBody
    @RequestMapping("/getReceiveAddressByMemberId")
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(@RequestParam String memberId){
        List<UmsMemberReceiveAddress> members=userService.getReceiveAddressByMemberId(memberId);

        return members;
    }
}
