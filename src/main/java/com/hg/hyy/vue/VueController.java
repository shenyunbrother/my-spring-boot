package com.hg.hyy.vue;

import java.io.IOException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.hg.hyy.file.Filestrem;
import com.hg.hyy.grpc.HelloWorldClient;
import com.hg.hyy.grpc.HelloWorldServer;
import com.hg.hyy.kafka.KafkaUtil;
import com.hg.hyy.kafka.MyProducer;
import com.hg.hyy.util.UserSerializationUtil;

@RestController
@RequestMapping("/vue")
public class VueController {

    private String account = "admin";
    private String pwd = "111111";

    private String servers = "localhost:9092";
    private String topic = "java_kafka_topic";
    @Autowired
    private UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(VueController.class);

    @PostMapping("/signin")
    public Msg signin(@RequestBody User user) throws IOException {
        Filestrem fs = new Filestrem();
        // fs.bis_bos();
        fs.br_bw();
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

    @PostMapping("/test")
    public String signin(@RequestBody Map<String, String> map) {

        System.out.println(map);
        return map.get("username") + map.get("password");
    }

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

    @GetMapping("/sendkafka")
    public String sendkafka() {

        String message = getRandomString(10);

        KafkaData data = new KafkaData(0);
        List<Map<String, String>> listMap = new ArrayList<>();
        Map<String, String> map = new HashMap<String, String>();
        map.put("keyTest", "hello kafka this is java");
        listMap.add(map);
        data.setListMap(listMap);

        KafkaProducer<String, Object> producer = KafkaUtil.createProducer(servers);
        KafkaUtil.send(producer, topic, data);

        StringBuilder sb = new StringBuilder();
        sb.append("topic:" + topic + " message:" + data + message);
        MyProducer mp = new MyProducer();
        mp.send();
        return sb.toString();

    }

    @GetMapping("/readkafka")
    public Object readkafka() {

        KafkaConsumer<String, Object> consumer = KafkaUtil.createConsumer(servers, topic);
        Object sb = KafkaUtil.readMessage(consumer, Duration.ofSeconds(1));

        KafkaUtil ku = new KafkaUtil();
        Thread t = new Thread(ku);
        t.start();
        return sb;
    }

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

    @GetMapping("/grpcc")
    public String grpcc() {
        try {
            HelloWorldClient.grpcC();
        } catch (Exception e) {

            e.printStackTrace();
        }
        return "demo";
    }
}
