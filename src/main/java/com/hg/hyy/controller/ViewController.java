package com.hg.hyy.controller;

import com.hg.hyy.entity.Greeting;
import com.hg.hyy.entity.Msg;
import com.hg.hyy.entity.Person;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author hyy
 * @since 2021-11-18
 */
@Api(tags = "view")
@Controller
@RequestMapping("/view")
public class ViewController {

  private static final Logger log = LoggerFactory.getLogger(VueController.class);
  private static final String template = "Hello, %s!";
  private final AtomicLong counter = new AtomicLong();

  @ApiOperation("wss")
  @GetMapping("/wss")
  public String wss() {

    return "wss";
  }

  @ApiOperation("测试权限管理")
  @GetMapping("/role")
  public String index(Model model) {
    Msg msg = new Msg("测试标题", 1000, "额外信息，只对管理员显示");
    model.addAttribute("msg", msg);
    return "role";
  }

  @ApiOperation("测试 get传参 model传参到html")
  // You can also add the @CrossOrigin annotation at the controller class level as
  // well, to enable CORS on all handler methods of this class.
  @GetMapping("/greet")
  public String greet(@RequestParam(value = "name", defaultValue = "World") String name, Model model) {
    Greeting g = new Greeting(counter.incrementAndGet(), String.format(template, name));
    model.addAttribute("g", g);
    log.error("hello this is test");
    return "greet";
  }

  @ApiOperation("表单校验")
  @GetMapping("/form")
  public String showForm(Person Person) {
    return "form";
  }

  @PostMapping("/form")
  public String checkPersonInfo(@Valid Person Person, BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      return "form";
    }

    return "redirect:/results";
  }
}
