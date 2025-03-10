package com.example.teamcity.api.requests.checked;

import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.generators.TestDataStorage;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.requests.CrudInterface;
import com.example.teamcity.api.requests.Request;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;

public final class CheckedBase<T extends BaseModel> extends Request implements CrudInterface {
    private final UncheckedBase uncheckedBase;

    public CheckedBase(RequestSpecification spec, Endpoint endpoint) {
        super(spec, endpoint);
        this.uncheckedBase = new UncheckedBase(spec, endpoint);
    }

    @Override
    public Response create(BaseModel model) {
        Response response = uncheckedBase.create(model);
        response.then().assertThat().statusCode(HttpStatus.SC_OK);
        BaseModel createdModel = response.getBody().as(endpoint.getModelClass());
        TestDataStorage.getInstance().addCreatedEntity(endpoint, createdModel);

        return response;
    }


    @Override
    public T read(String id) {
        var response = uncheckedBase.read(id)
                .then().assertThat().statusCode(HttpStatus.SC_OK);
                
        if (id.contains(":")) {
            // Если это локатор, получаем первый проект из массива
            return (T) response.extract().jsonPath().getObject("project[0]", endpoint.getModelClass());
        } else {
            // Если это прямой ID, десериализуем напрямую
            return (T) response.extract().as(endpoint.getModelClass());
        }
    }

    @Override
    public T update(String id, BaseModel model) {
        return (T) uncheckedBase.update(id, model)
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .extract().as(endpoint.getModelClass());
    }

    @Override
    public Object delete(String id) {
        return uncheckedBase.delete(id)
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .extract().asString();
    }
}

