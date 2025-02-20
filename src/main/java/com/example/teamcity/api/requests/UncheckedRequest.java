package com.example.teamcity.api.requests;

import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import io.restassured.specification.RequestSpecification;

import java.util.EnumMap;

/**
 * Паттерн Фасад
 */
public class UncheckedRequest {
    // По эндпоинту будем создавать UncheckedBase request
    // Это внутрення мапа , мы по эндпоинту будем создавать анчекед бейз риквест,
    // создаем по всем ключам
    private final EnumMap<Endpoint, UncheckedBase> requests = new EnumMap<>(Endpoint.class);

    // конструктор пройдется по созданию всех этих сущностей
    // передаем юзера, для кот хотим создавать риквсет, те спеку
    public UncheckedRequest(RequestSpecification spec) {
        // цикл
        for (var endpoint: Endpoint.values()) {
            requests.put(endpoint, new UncheckedBase(spec, endpoint));
        }
    }
    public UncheckedBase getRequest(Endpoint endpoint){
        return requests.get(endpoint);
    }
}
