package com.example.teamcity.api.requests;

import com.example.teamcity.api.models.BaseModel;

/**
 * С помощью интерфейса будем обязывать создавать все 4 CRUD метода
 */
public interface CrudInterface {

    Object create(BaseModel model);

    Object read(String id);

    Object update(String id, BaseModel model);

    Object delete(String id);

}
