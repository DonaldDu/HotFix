package com.dhy.hotfix;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
        Pattern pattern = Pattern.compile("(\\d+)\\.patch\\.apk");
        Matcher matcher = pattern.matcher("1.patch.apk");
        if (matcher.find()) System.out.println(matcher.group(1));
    }
}