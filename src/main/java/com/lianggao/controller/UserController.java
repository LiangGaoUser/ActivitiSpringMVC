package com.lianggao.controller;

//import com.lianggao.service.UserService;
import com.lianggao.bean.UserInfo;
import com.lianggao.service.UserService;
import com.lianggao.utils.Crypt;
import com.lianggao.utils.DBUtils;
import com.lianggao.utils.DBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@Scope("prototype")
public class UserController {
    @Autowired
    private UserService userService;
    @RequestMapping("userAction_login")
    public String login(HttpServletRequest httpServletRequest, HttpSession session, Model model, String userName, String userPassword) {
        System.out.println("enter login");
        UserInfo userInfo2 = new UserInfo();
        userInfo2.setUserName(userName);
        userInfo2.setUserPassword(userPassword);
        System.out.println(userInfo2.toString());
        UserInfo userInfo = userService.login(userInfo2);

        //登录成功
        if (userInfo!=null) {
            System.out.println(userInfo.toString());
            session.setAttribute("activeUser", userInfo);
            //将用户信息存入到session中
            System.out.println("======================================");
            System.out.println(userInfo.toString());
            System.out.println("login success");
            httpServletRequest.setAttribute("EmployeeName",userInfo2.getUserName());
            return "/main2";
        } else {
            //登录失败
//设置错误提示信息
            System.out.println("======================================");
            System.out.println("login failed");
            model.addAttribute("msg", "用户名或密码错误！");
            return "/home/login_failed";
        }
    }

    @RequestMapping("/user_login")
    @ResponseBody
    public Map<String, Object> Login(@RequestParam("userid") String userid, @RequestParam("password") String password, HttpSession session) throws  Exception {
        //LogUtils.log("test");
        Map<String, Object> result = new HashMap<>();
        String key = "shirleyL";
        String _password = "";
        try {
            _password = Crypt.DESEncrypt(password, key);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("text", "服务器解析错误");
            return result;
        }
        DBo dbo=new DBo();
        session.setAttribute("UserID",userid);
        if(!userid.equals("admin"))
        {
            userid=dbo.getString("select EmployeeNum from employee_t where EmployeeID = '"+userid+"'","EmployeeNum");
        }
        String sql = "select * from UserInfo_T where EmployeeNum='" + userid + "' collate Chinese_PRC_CS_AI and Password='" + _password + "'";
        List<Map<String, Object>> list = DBUtils.query(sql);
        if (list.size() > 0) {
            String userState = list.get(0).get("UserState").toString();
            if (userState.equals("1")) {
                String userRight = list.get(0).get("UserRight").toString();
                if(!userRight.equals(""))
                {
                    userRight = Crypt.DESDecrypt(userRight, key);
                }
                session.setAttribute("UserRight", userRight);
                session.setAttribute("UserNum", userid);
                session.setAttribute("UserName", list.get(0).get("EmployeeName").toString());
                //System.out.println(session.getAttribute("UserName"));
                String userInstitutionCategoryNum = "";
                String userInstitution = "";
                String userInstitutionName = "";
                if (userid.equals("admin") || userid.equals("kuang")) {
                    session.setAttribute("UserInstitution", "AHB");
                    session.setAttribute("UserInstitutionName", "安全管理部");
                    session.setAttribute("UserInstitutionCategoryNum","3");
                    userInstitution="AHB";
                    userInstitutionName="安全管理部";
                    userInstitutionCategoryNum="3";
                } else {


                    String sql4 = "select e.*,i.InstitutionName,i.InstitutionCategoryNum from Employee_t e left join institution_t i on e.InstitutionNum=i.InstitutionNum where EmployeeNum='" + userid + "'";
                    List<Map<String, Object>> list4 = DBUtils.query(sql4);
                    if (list4.size() > 0) {
                        userInstitution = list4.get(0).get("InstitutionNum").toString();
                        userInstitutionName = list4.get(0).get("InstitutionName").toString();
                        userInstitutionCategoryNum = list4.get(0).get("InstitutionCategoryNum").toString();
                    } else {
                        String sql5 = "select c.*,i.InstitutionName,i.InstitutionCategoryNum from ContractorEmployee_t c left join institution_t i on c.InstitutionNum=i.InstitutionNum where EmployeeNum='" + userid + "'";
                        List<Map<String, Object>> list5 = DBUtils.query(sql5);
                        if (list5.size() > 0) {
                            userInstitution = list5.get(0).get("InstitutionNum").toString();
                            userInstitutionName = list5.get(0).get("InstitutionName").toString();
                            userInstitutionCategoryNum = list5.get(0).get("InstitutionCategoryNum").toString();

                        } else {
                            result.put("text", "账号或密码错误");
                            return result;
                        }
                    }
                    session.setAttribute("UserInstitution", userInstitution);
                    session.setAttribute("UserInstitutionName", userInstitutionName);
                    session.setAttribute("UserInstitutionCategoryNum",userInstitutionCategoryNum);
                }
                long loginTimes = (long) (list.get(0).get("LoginTimes")) + 1;
                String loginTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String sql3 = "update  UserInfo_T set LoginTimes='" + loginTimes + "', LoginTime='" + loginTime + "',LastLoginTime='" + loginTime + "' where EmployeeNum='" + userid + "'";
                if (DBUtils.update(sql3) > 0) {
                    result.put("status","ok");
                    result.put("userRight",userRight);
                    result.put("userName",list.get(0).get("EmployeeName").toString());
                    result.put("userInstitutionCategoryNum",userInstitutionCategoryNum);
                    result.put("userInstitution",userInstitution);
                    result.put("userInstitutionName",userInstitutionName);
                    result.put("userNum",userid);
//                    result.put("text", "成功");
                    return result;
                } else {
                    result.put("text", "数据更新错误");
                    return result;
                }
            } else {
                result.put("text", "用户无权限登录该系统，请联系管理员");
                return result;
            }
        } else {
            String sql7 = "select u.*,i.InstitutionName from USER_ASSESS_EXPERT_T u left join institution_t i on u.professorInstitution=i.InstitutionNum where professorID='" + userid + "' and professorPWD='" + _password + "'";
            List<Map<String, Object>> list7 = DBUtils.query(sql7);
            if (list7.size() > 0) {
                session.setAttribute("UserRight", "220200-1;220400-1");
                session.setAttribute("UserNum", userid);
                session.setAttribute("UserName", list.get(0).get("EmployeeName").toString());
                session.setAttribute("UserInstitution", list7.get(0).get("professorInstitution").toString());
                session.setAttribute("UserInstitutionName", list7.get(0).get("InstitutionName").toString());
            }
        }
        result.put("text", "账号或密码错误");
        return result;
    }
}
