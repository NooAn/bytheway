package ru.a1024bits.bytheway.dagger

import kotlin.reflect.KClass

/**
 * Created by andrey.gusenkov on 03/11/2017.
 */
import android.arch.lifecycle.ViewModel

import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import dagger.MapKey

@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(RetentionPolicy.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)