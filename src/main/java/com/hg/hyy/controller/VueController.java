package com.hg.hyy.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.core.env.Environment;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import io.swagger.annotations.ApiImplicitParams;

import com.hg.hyy.entity.Student;
import com.hg.hyy.utils.Filestrem;
import com.hg.hyy.utils.HmacSHA256;
import com.hg.hyy.utils.UserSerializationUtil;
import com.hg.hyy.config.PersonConfig;
import com.hg.hyy.entity.Greeting;
import com.hg.hyy.entity.Msg;
import com.hg.hyy.entity.Sb;
import com.hg.hyy.entity.Topic;
import com.hg.hyy.entity.User;
import com.hg.hyy.entity.UserRepository;
import com.hg.hyy.grpc.HelloWorldClient;
import com.hg.hyy.grpc.HelloWorldServer;
import com.hg.hyy.kafka.KafkaClient;
import com.hg.hyy.kafka.KafkaData;
import com.hg.hyy.kafka.MyProducer;
import com.hg.hyy.mqtt.MqttPub;
import com.hg.hyy.service.StudentService;
import com.hg.hyy.vo.ResponseVo;

//@RestController，一般是使用在类上的，它表示的意思其实就是结合了@Controller和@ResponseBody两个注解，
@Api(tags = "vue")
@RestController
@RequestMapping("/v1")
public class VueController {

    private String account = "admin";
    private String pwd = "111111";

    private String servers = "localhost:9092";
    private String topic = "java_kafka_topic";

    private static final Logger log = LoggerFactory.getLogger(VueController.class);
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    // This means to get the bean called userRepository Which is auto-generated by
    // Spring, we will use it to handle the data
    @Autowired
    private Environment environment;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MqttPub mqttPub;

    @Autowired
    private PersonConfig personConfig;

    @Autowired
    private StudentService studentService;

    @ApiOperation("index")
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @ApiOperation("测试日志")
    @GetMapping("/log")
    public String log() {

        log.error("我是一条error日志");
        return "log is log";
    }

    @ApiOperation("测试异常")
    @SuppressWarnings("unused")
    @GetMapping("/err")
    public String err() {

        try {
            int a = 1 / 0;
        } catch (Exception e) {
            log.error("算术异常", e);
        }
        return "err";
    }

    @ApiOperation("测试 get传参 model传参到html")
    // You can also add the @CrossOrigin annotation at the controller class level as
    // well, to enable CORS on all handler methods of this class.
    @CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
    @GetMapping("/greet")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name, Model model) {
        log.error("---");
        Greeting g = new Greeting(counter.incrementAndGet(), String.format(template, name));
        model.addAttribute("g", g);
        return "greet";
    }

    @ApiOperation("测试CORS")
    @GetMapping("/greeting-javaconfig")
    public Greeting greetingWithJavaconfig(@RequestParam(required = false, defaultValue = "World") String name) {
        log.error("==== in greeting ====");
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

    @ApiOperation("500")
    @SuppressWarnings("unused")
    @GetMapping("/test500")
    public String test500() {
        int i = 1 / 0;// 服务器内部运行异常 跳转500页面
        return "500";
    }

    @ApiOperation("测试model传参到html")
    @GetMapping("/admin")
    public String admin(Model model) {
        List<User> userList = new ArrayList<>();
        User user = new User("admin", "admin", "管理员");
        User user1 = new User("user", "123456", "访客");
        userList.add(user);
        userList.add(user1);
        model.addAttribute("users", userList);
        model.addAttribute("user", user);
        model.addAttribute("user1", user1);
        return "user";
    }

    @ApiOperation("新增用户")
    @PostMapping(path = "/add") // Map ONLY POST Requests
    public @ResponseBody String addNewUser(@RequestParam String username, @RequestParam String password,
            @RequestParam String role) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request

        User n = new User();
        n.setUsername(username);
        n.setPassword(password);
        n.setRole(role);
        userRepository.save(n);
        return "Saved";
    }

    @ApiOperation("获取所有用户")
    @GetMapping(path = "/all")
    public @ResponseBody Iterable<User> getAllUsers() {
        // This returns a JSON or XML with the users
        return userRepository.findAll();
    }

    @ApiOperation("登录")
    @PostMapping("/signin")
    public Msg signin(@RequestBody User user) throws IOException {
        Filestrem fs = new Filestrem();
        // fs.bis_bos();
        fs.br_bw();
        fs.record(user);

        ArrayList<String> al = new ArrayList<String>();
        al.add(user.getUsername());
        al.add(user.getPassword());
        al.add(user.getRole());

        fs.userrecord(al);

        Msg msg = new Msg("", 0, "");
        for (User us : userRepository.findAll()) {
            if (us.getUsername().equals(user.getUsername()) && us.getPassword().equals(user.getPassword())) {
                msg.setMsg("you are success login!");
                msg.setCode(1000);
                msg.setData("");
            } else if (user.getUsername().equals(account) && user.getPassword().equals(pwd)) {
                msg.setMsg("you are success login with inner account!");
                msg.setCode(1000);
                msg.setData("");
            } else {
                msg.setMsg("login failed!");
                msg.setCode(500);
                msg.setData("");
            }
        }
        return msg;
    }

    @ApiOperation("测试map传参到html")
    @PostMapping("/testmap")
    public String signin(@RequestBody Map<String, String> map) {

        System.out.println(map);
        return map.get("username") + map.get("password");
    }

    @ApiOperation("测试get传参")
    @GetMapping("/topic")
    public Topic getdata(@RequestParam(value = "topic", defaultValue = "fhh", required = true) String topic) {
        Topic t = new Topic();
        t.setContent(String.format("fhh do i with %s", topic));
        log.error("topic is log");
        return t;
    }

    // 生成随机字符串
    public String getRandomString(int length) {
        // String filename=RandomStringUtils.randomAlphanumeric(10);
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    // 可以指定某个位置是a-z、A-Z或是0-9
    public static String getRandomString2(int length) {
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(3);
            long result = 0;
            switch (number) {
            case 0:
                result = Math.round(Math.random() * 25 + 65);
                sb.append(String.valueOf((char) result));
                break;
            case 1:
                result = Math.round(Math.random() * 25 + 97);
                sb.append(String.valueOf((char) result));
                break;
            case 2:
                sb.append(String.valueOf(new Random().nextInt(10)));
                break;
            }

        }
        return sb.toString();
    }

    @ApiOperation("kafka producer")
    @GetMapping("/sendkafka")
    public String sendkafka() {

        String message = getRandomString(10);

        KafkaData data = new KafkaData(0);
        List<Map<String, String>> listMap = new ArrayList<>();
        Map<String, String> map = new HashMap<String, String>();
        map.put("keyTest", "hello kafka this is java");
        listMap.add(map);
        data.setListMap(listMap);

        KafkaProducer<String, Object> producer = KafkaClient.createProducer(servers);
        KafkaClient.send(producer, topic, data);

        StringBuilder sb = new StringBuilder();
        sb.append("topic:" + topic + " message:" + data + message);
        MyProducer mp = new MyProducer();
        mp.send();
        return sb.toString();

    }

    @ApiOperation("kafka consumer")
    @GetMapping("/readkafka")
    public Object readkafka() {

        KafkaConsumer<String, Object> consumer = KafkaClient.createConsumer(servers, topic);
        Object sb = KafkaClient.readMessage(consumer, Duration.ofSeconds(1));

        KafkaClient ku = new KafkaClient();
        Thread t = new Thread(ku);
        t.start();
        return sb;
    }

    @ApiOperation("序列化")
    @GetMapping("/proto")
    public String demo() {
        User user = new User();
        user.setUsername("xpleaf");
        user.setPassword("111111");
        System.out.println("序列化前：" + user);
        // 使用UserSerializationUtil将user对象序列化
        byte[] userBytes = UserSerializationUtil.serialize(user);
        // 使用UserSerializationUtil反序列化字节数组为user对象
        User user2 = UserSerializationUtil.deserialize(userBytes);
        System.out.println("序列化后再反序列化：" + user2);
        // 判断值是否相等
        System.out.println(user.toString().equals(user2.toString()));
        return "demo";
    }

    @ApiOperation(value = "mqtt发布主题", notes = "测试发布主题")
    @GetMapping(value = "/mqtt")
    public String mqtt_test() {
        mqttPub.publish(0, false, "test/test", "hello mqtt this is spring");
        return "ok";
    }

    @ApiOperation("grpc server")
    @GetMapping("/grpcs")
    public String grpcs() {
        try {
            HelloWorldServer.grpcS();
        } catch (IOException e) {

            e.printStackTrace();
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        return "demo";
    }

    @ApiOperation("grpc client")
    @GetMapping("/grpcc")
    public String grpcc() {
        try {
            HelloWorldClient.grpcC();
        } catch (Exception e) {

            e.printStackTrace();
        }
        return "demo";
    }

    @ApiOperation("88万行代码")
    @GetMapping("/sb")
    public String sb() {
        try {
            Sb.sb();
        } catch (Exception e) {

            e.printStackTrace();
        }
        return "ok";
    }

    @ApiOperation("添加一名学生") // 为每个handler添加方法功能描述
    @PostMapping("/add_student")
    @ApiImplicitParam(name = "student", value = "所添加的学生", dataTypeClass = Student.class)
    @SuppressWarnings("unchange")
    public ResponseVo<Integer> addOneStudent(Student student) {
        return studentService.addOneStudent(student);
    }

    @ApiOperation("根据studentId删除一名学生")
    @DeleteMapping("/delete_student/{studentId}")
    public ResponseVo<Integer> deleteOneStudentByStudentId(@PathVariable Integer studentId) {
        return studentService.deleteOneStudentByStudentId(studentId);
    }

    @ApiOperation("修改一名学生")
    @PutMapping("/update_student")
    @ApiImplicitParams({ @ApiImplicitParam(name = "studentId", value = "学号", required = true), // required为是否必填项
            @ApiImplicitParam(name = "studentName", value = "学生姓名", required = false),
            @ApiImplicitParam(name = "studentSex", value = "学生性别", required = false),
            @ApiImplicitParam(name = "studentScore", value = "学生分数", required = false) })
    public ResponseVo<Integer> updateOneStudent(Student student) {
        return studentService.updateOneStudent(student);
    }

    @ApiOperation("根据id获取一名学生")
    @GetMapping("/get_student/{studentId}")
    public ResponseVo<Student> getOntStudentByStudentId(@PathVariable Integer studentId) {
        return studentService.getOneStudentByStudentId(studentId);
    }

    @ApiOperation("获取全部学生")
    @GetMapping("/get_students")
    public ResponseVo<Collection<Student>> getAllStudent() {
        return studentService.getAllStudent();
    }

    @ApiOperation("读取配置文件")
    @RequestMapping("/test")
    @ResponseBody
    public String getTest() {
        return "hello," + environment.getProperty("com.hg.hyy.test");
    }

    @ApiOperation("配置文件")
    @GetMapping("/get_app")
    public String contextLoads() {

        Map<String, String> person = personConfig.getPerson();
        List<String> list = personConfig.getList();

        return "image:" + JSONObject.fromObject(person).toString() + "list:" + JSONArray.fromObject(list).toString();
    }

    @ApiOperation("vigap_api")
    @GetMapping("/vigap")
    public String vigap() throws UnsupportedEncodingException {
        String key = "igzldvp-zaz3rpr-8xrdnlp-mpwr9";

        LocalDateTime ldt = LocalDateTime.now(); // 当前日期和时间
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String localTime = df.format(ldt);

        Map<String, String> m = new HashMap<String, String>();

        List<String> l = new ArrayList<String>();
        TreeSet<String> t = new TreeSet<String>();
        String method = "GET";
        String ip = "192.168.20.33";
        String api = "/api/v1/systems/dashboard";
        l.add(method);
        l.add(ip);
        l.add(api);
        String url = String.join("\\n", l);

        m.put("AccessKeyId", "472b307a-0d62fc1a-49368bc6-k01mk");
        m.put("SignatureMethod", "HmacSHA256");
        m.put("SignatureVersion", "2");
        m.put("Timestamp", localTime);

        for (Map.Entry<String, String> entry : m.entrySet()) {
            t.add(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "utf-8"));
        }

        String param = String.join("&", t);

        // 这里要使用转义。。。。。。
        String message = url + "\\n" + param + "\\n";

        String reqs = HmacSHA256.sha256_mac(message, key);
        String rust = Base64.getEncoder().encodeToString(reqs.toUpperCase().getBytes("utf-8")).toString();
        String Signature = URLEncoder.encode(rust, "utf-8");

        String host = "https://192.168.20.33:10000";

        String reqUrl = host + api + "?" + param + "&" + "Signature=" + Signature;

        return reqUrl;
    }

}
