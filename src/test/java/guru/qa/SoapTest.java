package guru.qa;

import io.restassured.RestAssured;
import io.restassured.internal.util.IOUtils;
import io.restassured.response.Response;
import io.spring.guides.gs_producing_web_service.Currency;
import io.spring.guides.gs_producing_web_service.GetCountryRequest;
import io.spring.guides.gs_producing_web_service.GetCountryResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPMessage;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SoapTest {

    @Test
    void getCountriesTest() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("getCountryRequest.xml");

        final String requestBody = new String(IOUtils.toByteArray(is));

        RestAssured.baseURI = "http://localhost:8080/ws";

        given()
                .contentType("text/xml")
                .when()
                .body(requestBody)
                .log().all()
                .post("/getCountry")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK);

    }

    @Test
    void getCountriesTestWithModel() throws Exception {
        GetCountryRequest requestBody = new GetCountryRequest();
        requestBody.setName("Spain");

        // Создаём SOAP-сообщение
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage reqSoapMessage = messageFactory.createMessage();

        // Добавляем пустой заголовок
        reqSoapMessage.getSOAPHeader(); // создаёт header


        // Добавляем тело
        SOAPBody reqSoapBody = reqSoapMessage.getSOAPBody();

        // Маршалим объект в элемент и вставляем в Body
        JAXBContext reqContext = JAXBContext.newInstance(GetCountryRequest.class);
        Marshaller reqMarshaller = reqContext.createMarshaller();

        reqMarshaller.marshal(requestBody, reqSoapBody);

        // Преобразуем SOAPMessage в строку
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        reqSoapMessage.writeTo(out);
        String soapRequest = out.toString(StandardCharsets.UTF_8);

        RestAssured.baseURI = "http://localhost:8080/ws";

        Response response = given()
                .contentType("text/xml")
                .when()
                .body(soapRequest)
                .post("/getCountry")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().response();


        // 1. Получаем XML-ответ в виде строки
        String responseXml = response.asString();

        // 2. Создаём SOAPMessage из строки

        SOAPMessage resSoapMessage = messageFactory.createMessage(null, new java.io.ByteArrayInputStream(responseXml.getBytes(StandardCharsets.UTF_8)));

        // 3. Извлекаем тело SOAP-ответа
        SOAPBody resSoapBody = resSoapMessage.getSOAPBody();
        Node responseNode = resSoapBody.getFirstChild(); // это <getCountryResponse>

        JAXBContext resContext = JAXBContext.newInstance(GetCountryResponse.class);
        Unmarshaller resUnmarshaller = resContext.createUnmarshaller();
        GetCountryResponse responseBody = (GetCountryResponse) resUnmarshaller.unmarshal(responseNode);

        assertAll(
                () -> assertEquals("Spain", responseBody.getCountry().getName()),
                () -> assertEquals(46704314, responseBody.getCountry().getPopulation()),
                () -> assertEquals("Madrid", responseBody.getCountry().getCapital()),
                () -> assertEquals(Currency.EUR, responseBody.getCountry().getCurrency())
        );

    }

}
