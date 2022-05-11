package me.Jedi.drivers;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WordleGamesDriver extends Driver{
    private WebDriver driver;
    private JavascriptExecutor js;
    private Map<Letter, WebElement> keyboardMap;
    private String url;
    private int numberOfLetters;

    public WordleGamesDriver(int numberOfLetters) {
        this.url = "https://www.wordleunlimited.com";
        this.numberOfLetters = numberOfLetters;
    }

    @Override
    public void open() throws InterruptedException {
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\WordleBot\\chromedriver.exe");
        this.driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        js =  (JavascriptExecutor) driver;

        driver.get(url);

//        Thread.sleep(5000);

        setMode();

        WebElement keyboard = driver.findElement(By.className("Game-keyboard"));
//        List<WebElement> keys = keyboard.findElements(By.tagName("button"));

        List<WebElement> keys = keyboard.findElements(By.tagName("button"));

        keyboardMap = new HashMap<>();

        for(WebElement k : keys) {

            String key = k.getText();

            keyboardMap.put(Letter.valueOf(key.toUpperCase()), k);
        }
    }

    private void setMode() {
        WebElement settings = (WebElement) js.executeScript("return document.querySelector(\"#root > div > div.Game > div.header-row.header > div.header-flex.two.game-icons.right-icons.pointer.stats-icon > div.settings-icon.pointer > svg\")");
        settings.click();

        WebElement letterSlider = (WebElement) js.executeScript("return document.querySelector(\"#wordLength\")");
        js.executeScript("document.querySelector(\"#wordLength\").value = '" + numberOfLetters + "';");
    }

    @Override
    public String getInfo(int guess) throws InterruptedException {
        return null;
    }

    @Override
    protected void pressKey(Character c) {
        keyboardMap.get(Letter.valueOf(c.toString().toUpperCase())).click();
    }

    @Override
    protected void pressEnter() {
        keyboardMap.get(Letter.valueOf("ENTER")).click();
    }
}
