package com.iamport.sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import com.google.gson.Gson;
import com.iamport.sdk.data.sdk.IamPortRequest;
import com.iamport.sdk.data.sdk.PayMethod;

import org.junit.Test;

public class CustomdataTest {


    @Test
    public void java_customdata_write_test() {

        //given
        String extected = "{\"pg\":\"kcp\",\"pay_method\":\"card\",\"merchant_uid\":\"mid_123456\",\"name\":\"여기주문이요\",\"amount\":\"3000\",\"buyer_name\":\"홍길동\",\"m_redirect_url\":\"http://detectchangingwebview/iamport/a\",\"niceMobileV2\":true,\"custom_data\":{\"employees\":{\"employee\":[{\"id\":\"1\",\"firstName\":\"Tom\",\"lastName\":\"Cruise\",\"photo\":\"https://pbs.twimg.com/profile_images/735509975649378305/B81JwLT7.jpg\"},{\"id\":\"2\",\"firstName\":\"Maria\",\"lastName\":\"Sharapova\",\"photo\":\"https://pbs.twimg.com/profile_images/786423002820784128/cjLHfMMJ_400x400.jpg\"},{\"id\":\"3\",\"firstName\":\"James\",\"lastName\":\"Bond\",\"photo\":\"https://pbs.twimg.com/profile_images/664886718559076352/M00cOLrh.jpg\"}]}}}";

        //when
        IamPortRequest request
                = IamPortRequest.builder()
                .pg("kcp")
                .pay_method(PayMethod.card.name())
                .name("여기주문이요")
                .merchant_uid("mid_123456")
                .amount("3000")
                .custom_data("{\n" +
                        "  \"employees\": {\n" +
                        "    \"employee\": [\n" +
                        "      {\n" +
                        "        \"id\": \"1\",\n" +
                        "        \"firstName\": \"Tom\",\n" +
                        "        \"lastName\": \"Cruise\",\n" +
                        "        \"photo\": \"https://pbs.twimg.com/profile_images/735509975649378305/B81JwLT7.jpg\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"id\": \"2\",\n" +
                        "        \"firstName\": \"Maria\",\n" +
                        "        \"lastName\": \"Sharapova\",\n" +
                        "        \"photo\": \"https://pbs.twimg.com/profile_images/786423002820784128/cjLHfMMJ_400x400.jpg\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"id\": \"3\",\n" +
                        "        \"firstName\": \"James\",\n" +
                        "        \"lastName\": \"Bond\",\n" +
                        "        \"photo\": \"https://pbs.twimg.com/profile_images/664886718559076352/M00cOLrh.jpg\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}")
                .buyer_name("홍길동").build();

//        System.out.println(new Gson().toJson(request));
        String actual = new Gson().toJson(request);

        //then
        assertEquals(extected, actual);
    }
}
