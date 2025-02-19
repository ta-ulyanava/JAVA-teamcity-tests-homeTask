package com.example.teamcity.api.requests;

import com.example.teamcity.api.models.BaseModel;

/**
 * С помощью интерфейса будем обязывать создавать все 4 CRUD метода
 */
public interface CrudInterface {
    // Метод Create
    // Возвращаем Object то есть по сути любой тип данных
    // На вход метода принимает сущность, кот. является родителем любой DTO модели
    //BaseModel нужна для абстракции, чтобы не гадать какую DTO модель подавать на вход
    //В интерфейсах не пишем тело метода тк это контракт без реализации
    Object create(BaseModel model);
    Object read(String id);

    //В update нам также нужна обновленная модель
    Object update(String id, BaseModel model);
    Object delete(String id);

}
