package com.example.teamcity.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
//указывааем куда хотить проставлять эту аннотацию (только поля)
@Target(ElementType.FIELD)
// Указываем, что исполнение будет в рантайме
@Retention(RetentionPolicy.RUNTIME)
/**
 * Поля с этой аннотацией будут заполняться рандомными данными
 */
public @interface Random {


}
