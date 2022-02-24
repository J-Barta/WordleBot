package me.Jedi.drivers;

import me.Jedi.utils.Utils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NormalDriver extends Driver{

    private WebDriver driver;
    private JavascriptExecutor js;
    private Map<Letter, WebElement> keyboardMap;
    private String url;

    public NormalDriver(String url) {
        this.url = url;
    }

    @Override
    public void open() {
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\WordleBot\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        js =  (JavascriptExecutor) driver;

        driver.get(url);

        WebElement closeIntro = (WebElement) js.executeScript("return document.querySelector(\"body > game-app\").shadowRoot.querySelector(\"#game > game-modal\").shadowRoot.querySelector(\"div > div > div > game-icon\").shadowRoot.querySelector(\"svg\")");
        closeIntro.click();
        WebElement keyboard = (WebElement) js.executeScript("return document.querySelector(\"body > game-app\").shadowRoot.querySelector(\"#game > game-keyboard\").shadowRoot.querySelector(\"#keyboard\")");
        List<WebElement> keys = keyboard.findElements(By.tagName("button"));

        keyboardMap = new HashMap<>();

        for(WebElement k : keys) {

            String key = k.getAttribute("data-key");
            if(key.equals("↵"))  key = "enter";
            if(key.equals("←")) key = "backspace";

            keyboardMap.put(Letter.valueOf(key.toUpperCase()), k);
        }
    }

    @Override
    public String getInfo(int guess) throws InterruptedException {
        Thread.sleep(1000);

        WebElement row = (WebElement) js.executeScript("return document.querySelector(\"body > game-app\").shadowRoot.querySelector(\"#board > game-row:nth-child(" + guess + ")\").shadowRoot.querySelector(\"div\")");

        List<WebElement> letters = row.findElements(By.tagName("game-tile"));

        List<Character> info = new ArrayList<>();

        for(WebElement l : letters) {
            String evalutaion = l.getAttribute("evaluation");

            Character c;

            if(evalutaion.equals("correct")) c = 'g';
            else if(evalutaion.equals("present")) c = 'y';
            else c = 'n';

            info.add(c);
        }

        return Utils.charListToString(info);
    }

    @Override
    protected void pressKey(Character c) {
        keyboardMap.get(Letter.valueOf(c.toString().toUpperCase())).click();
    }

    @Override
    protected void pressEnter() {
        keyboardMap.get(Letter.ENTER).click();
    }

    public enum Letter {
        A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, BACKSPACE, ENTER;
    }

}
