package org.rdx.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.rdx.sdk.types.util.BearerTokenUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @ author rdx
 * @ describe :
 * @ date  2024/11/22
 **/
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {
    @Test
    public void test(){
        System.out.println("aaa1");

    }
    @Test
    public void testGML(){
        String apiKeySecret = "a7ce6f4ab128d3403ae7bb5e89edc699.33DZiyacos8M4e5d";
        String token = BearerTokenUtils.getToken(apiKeySecret);
        System.out.println(token);
    }
}
