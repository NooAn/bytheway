package ru.a1024bits.bytheway

import java.security.SecureRandom

private const val POSSIBLE_VALUES = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
private const val ID_SIZE = 28
private val random = SecureRandom()

fun randomString(): String = StringBuilder(ID_SIZE).apply {
    (0 until ID_SIZE).forEach { append(POSSIBLE_VALUES[random.nextInt(POSSIBLE_VALUES.length)]) }
}.toString()