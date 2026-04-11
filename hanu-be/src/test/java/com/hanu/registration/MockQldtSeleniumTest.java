package com.hanu.registration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MockQldtSeleniumTest {

    @LocalServerPort
    private int port;

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void shouldTickCheckboxAndShowSelectedCourse() {
        driver.get("http://localhost:" + port + "/mock-qldt");

        WebElement checkbox = driver.findElement(By.id("course-61FIT4GRP"));
        checkbox.click();

        WebElement registerBtn = driver.findElement(By.id("mock-register-btn"));
        registerBtn.click();

        WebElement result = driver.findElement(By.id("mock-result"));
        String text = result.getText();

        assertTrue(text.contains("61FIT4GRP"));
    }
}