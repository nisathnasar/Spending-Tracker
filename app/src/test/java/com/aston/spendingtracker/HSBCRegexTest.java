package com.aston.spendingtracker;

import org.junit.Test;

import static org.junit.Assert.*;

import com.aston.spendingtracker.pdf.HSBCRegex;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class HSBCRegexTest {
//    @Test
//    public void addition_isCorrect() {
//        assertEquals(4, 2 + 2);
//    }

    @Test
    public void startsWithDateTest1(){
        assertFalse(HSBCRegex.startsWithDate("12 jun 16"));
    }

    @Test
    public void startsWithDateTest2(){
        assertFalse(HSBCRegex.startsWithDate("12 jun 16 "));
    }

    @Test
    public void startsWithDateTest3(){
        assertTrue(HSBCRegex.startsWithDate("12 jun 16 someothertext473892![];'"));
    }

    @Test
    public void startsWithDateTest4(){
        assertTrue(HSBCRegex.startsWithDate("12 jun 16 DD Shop A 47 2901"));
    }

    @Test
    public void startsWithDateTest5(){
        assertFalse(HSBCRegex.startsWithDate("wfwef 12 jun 16 DD Shop A 47 2901"));
    }

    @Test
    public void startsWithFormattedDateTest(){
        assertFalse(HSBCRegex.startsWithFormattedDate("12-jun-16"));
    }

    @Test
    public void startsWithFormattedDateTest2(){
        assertTrue(HSBCRegex.startsWithFormattedDate("12-jun-16 a"));
    }

    @Test
    public void startsWithFormattedDateTest3(){
        assertFalse(HSBCRegex.startsWithFormattedDate("a 12-jun-16"));
    }

    @Test
    public void startsWithFormattedDateTest4(){
        assertTrue(HSBCRegex.startsWithFormattedDate("12-jun-16 DD Shop A 47 2901"));
    }

    @Test
    public void endsWithMoneyValueTest(){
        assertFalse(HSBCRegex.endsWithMoneyValue("2901.00"));
    }

    @Test
    public void endsWithMoneyValueTest2(){
        assertFalse(HSBCRegex.endsWithMoneyValue("12-jun-16 DD Shop A 47 2901"));
    }

    @Test
    public void endsWithMoneyValueTest3(){
        assertTrue(HSBCRegex.endsWithMoneyValue("12-jun-16 DD Shop A 47 2901.00"));
    }

    @Test
    public void endsWithMoneyValueTest4(){
        assertTrue(HSBCRegex.endsWithMoneyValue("12-jun-16 DD Shop A 47 1.00"));
    }

    @Test
    public void endsWithMoneyValueTest5(){
        assertFalse(HSBCRegex.endsWith2MoneyValues("12-jun-16 DD Shop A 47 .00"));
    }


    @Test
    public void endsWith2MoneyValuesTest(){
        assertFalse(HSBCRegex.endsWith2MoneyValues("2901.00"));
    }

    @Test
    public void endsWith2MoneyValuesTest2(){
        assertFalse(HSBCRegex.endsWith2MoneyValues("12-jun-16 DD Shop A 47 2901"));
    }

    @Test
    public void endsWith2MoneyValuesTest3(){
        assertTrue(HSBCRegex.endsWith2MoneyValues("12-jun-16 DD Shop A 47.00 2901.00"));
    }

    @Test
    public void endsWith2MoneyValuesTest4(){
        assertFalse(HSBCRegex.endsWith2MoneyValues("12-jun-16 DD Shop A 47 1.00"));
    }

    @Test
    public void endsWith2MoneyValuesTest5(){
        assertFalse(HSBCRegex.endsWith2MoneyValues("12-jun-16 DD Shop A dsjlks 1.00"));
    }

}