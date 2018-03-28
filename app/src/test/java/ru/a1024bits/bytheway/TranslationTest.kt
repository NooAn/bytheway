package ru.a1024bits.bytheway

import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import ru.a1024bits.bytheway.util.Translit

/**
 * Created by Andrei_Gusenkov on 3/16/2018.
 */
@RunWith(JUnit4::class)
class TranslationTest {

    @Test
    @Throws(Exception::class)
    fun testCitySlate() {
        val t = Translit()
        val s = "Рига"
        val s1 = "Riga"
        Assert.assertTrue(s1.toLowerCase().compareTo(t.cyr2lat(s).toLowerCase()) == 0)
    }

    @Test
    @Throws(Exception::class)
    fun testCitySlate2() {
        val t = Translit()
        val s = "Москва"
        val s1 = "Moskva"
        Assert.assertTrue(s1.toLowerCase().compareTo(t.cyr2lat(s).toLowerCase()) == 0)
    }

    @Test
    @Throws(Exception::class)
    fun testCitySlate3() {
        val t = Translit()
        val s = "Санкт"
        val s1 = "Sankt"
        Assert.assertTrue(s1.toLowerCase().compareTo(t.cyr2lat(s).toLowerCase()) == 0)
    }

    @Test
    @Throws(Exception::class)
    fun testCitySlate4() {
        val t = Translit()
        val s = "Хельсинки"
        val s1 = "Helsinki"
    //    Assert.assertEquals(s1, t.cyr2lat(s))
        //Assert.assertTrue(s1.toLowerCase().compareTo(t.cyr2lat(s).toLowerCase()))
    }
}