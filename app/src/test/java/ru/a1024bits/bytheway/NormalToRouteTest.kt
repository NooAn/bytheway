package ru.a1024bits.bytheway

import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Created by tikhon.osipov on 26.11.17
 */
@RunWith(JUnit4::class)
class NormalToRouteTest {

    @Test
    @Throws(Exception::class)
    fun testArctgZero() {
        val lat1 = 60.0
        val lon1 = 60.0

        val lat2 = 60.0
        val lon2 = 100.0

        val arctg = findArctg(lat1, lat2, lon1, lon2)
        Assert.assertEquals(0.0, arctg)
    }

    @Test
    @Throws(Exception::class)
    fun testArctg90() {
        val lat1 = 60.0
        val lon1 = 60.0

        val lat2 = 100.0
        val lon2 = 60.0

        val arctg = findArctg(lat1, lat2, lon1, lon2)
        Assert.assertEquals(90.0, arctg)
    }

    @Test
    @Throws(Exception::class)
    fun testPointRotation_shouldNotRotate() {
        val lat1 = 60.0
        val lon1 = 60.0

        val lat2 = 60.0
        val lon2 = 100.0

        val angle = findArctg(lat1, lat2, lon1, lon2)
        val module = module(lat1, lat2, lon1, lon2)

        println("module = $module")

        val latCentral = (lat2+lat1)/2
        val lonCentral = (lon2+lon1)/2

        val latTop = latCentral + module / 8
        val lonTop = lonCentral

        val latBottom = latCentral - module / 8
        val lonBottom = lonCentral

        val rotatedTop = rotatePoint(lonTop, latTop, lonCentral, latCentral, angle)
        val rotatedBottom = rotatePoint(lonBottom, latBottom, lonCentral, latCentral, angle)

        println("rotatedTop: x = ${rotatedTop[0]}, y = ${rotatedTop[1]}")
        println("rotatedBottom: x = ${rotatedBottom[0]}, y = ${rotatedBottom[1]}")
    }

    @Test
    @Throws(Exception::class)
    fun testPointRotation_shouldRotate90() {
        val lat1 = 60.0
        val lon1 = 60.0

        val lat2 = 100.0
        val lon2 = 60.0

        val angle = findArctg(lat1, lat2, lon1, lon2)
        val module = module(lat1, lat2, lon1, lon2)

        println("module = $module")

        val latCentral = (lat2+lat1)/2
        val lonCentral = (lon2+lon1)/2

        val latTop = latCentral + module / 8
        val lonTop = lonCentral

        val latBottom = latCentral - module / 8
        val lonBottom = lonCentral

        val rotatedTop = rotatePoint(lonTop, latTop, lonCentral, latCentral, angle)
        val rotatedBottom = rotatePoint(lonBottom, latBottom, lonCentral, latCentral, angle)

        println("rotatedTop: x = ${rotatedTop[0]}, y = ${rotatedTop[1]}")
        println("rotatedBottom: x = ${rotatedBottom[0]}, y = ${rotatedBottom[1]}")
    }

    @Test
    @Throws(Exception::class)
    fun testPointRotation_shouldRotateSomeDegrees() {
        val lat1 = 50.0
        val lon1 = 50.0

        val lat2 = 65.0
        val lon2 = 65.0

        val angle = findArctg(lat1, lat2, lon1, lon2)
        //val angle = 90.0
        val module = module(lat1, lat2, lon1, lon2)

        println("module = $module")

        val latCentral = (lat2+lat1)/2
        val lonCentral = (lon2+lon1)/2

        val latTop = latCentral + module / 8
        val lonTop = lonCentral

        val latBottom = latCentral - module / 8
        val lonBottom = lonCentral

        val rotatedTop = rotatePoint(lonTop, latTop, lonCentral, latCentral, angle)
        val rotatedBottom = rotatePoint(lonBottom, latBottom, lonCentral, latCentral, angle)

        println("rotatedTop: x = ${rotatedTop[0]}, y = ${rotatedTop[1]}")
        println("rotatedBottom: x = ${rotatedBottom[0]}, y = ${rotatedBottom[1]}")
    }

    //lat = y
    //lon = x

    private fun findArctg(lat1: Double, lat2: Double, lon1: Double, lon2: Double) : Double {
        val arctg = Math.atan( (lat2-lat1) / (lon2-lon1) )
        println("fraction: lat = ${lat2-lat1} / lon = ${lon2 - lon1}; arctg = $arctg")
        return Math.toDegrees(arctg)
    }

    /* Apply rotation matrix to the point(x,y) */
    private fun rotatePoint(x: Double, y: Double, x0: Double, y0: Double, angle: Double) : Array<Double> {
        val x1 = - (y - y0) * Math.sin(Math.toRadians(angle)) + Math.cos(Math.toRadians(angle)) * (x - x0) + x0
        val y1 = (y - y0) * + Math.cos(Math.toRadians(angle)) + Math.sin(Math.toRadians(angle)) * (x - x0) + y0
        return arrayOf(x1, y1)
    }

    private fun module(lat1: Double, lat2: Double, lon1: Double, lon2: Double) : Double {
        return Math.sqrt((lat2-lat1)*(lat2-lat1) + (lon2-lon1)*(lon2-lon1))
    }
}