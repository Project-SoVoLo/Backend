package soboro.soboro_web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.data.mongodb.uri=mongodb+srv://ahhyun:1234@cluster0.rxbgwnd.mongodb.net/sovoloDB"
})
class SoboroWebApplicationTests {

	@Test
	void contextLoads() {

	}

}
