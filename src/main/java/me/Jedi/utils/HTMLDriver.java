package me.Jedi.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import javax.swing.text.html.HTML;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HTMLDriver {

    private WebDriver driver;
    private JavascriptExecutor js;
    private Map<Letter, WebElement> keyboardMap;

    public HTMLDriver(String url) {
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\WordleBot\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.get(url);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        js =  (JavascriptExecutor) driver;

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

    public void typeWord(String word) {
        List<Character> chars = Utils.stringToCharList(word);

        for(Character c : chars) {
            pressKey(c);
        }

        pressEnter();
    }

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

    public void close() {
        this.driver.close();
    }

    private void pressKey(Character c) {
        keyboardMap.get(Letter.valueOf(c.toString().toUpperCase())).click();
    }

    private void pressEnter() {
        keyboardMap.get(Letter.ENTER).click();
    }

    public enum Letter {
        A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, BACKSPACE, ENTER;
    }

}
