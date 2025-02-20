package com.example.teamcity.api.generators;

import org.apache.commons.lang3.RandomStringUtils;

/**  В классе генерируем данные и НЕ храним никакого состояния, поэтому final
 *
 */
public final class RandomData {
    //Константа для хранения префикса
    private static final String TEST_PREFIX="test_";
    private static final int MAX_LENGTH=10;
    //Метод по генерации строки
    public static String getString(){
        return TEST_PREFIX + RandomStringUtils.randomAlphanumeric(MAX_LENGTH);
    }
    public static String getString(int length){
        return TEST_PREFIX + RandomStringUtils
                .randomAlphanumeric(Math.max(length -TEST_PREFIX.length(), MAX_LENGTH));
    }
}
