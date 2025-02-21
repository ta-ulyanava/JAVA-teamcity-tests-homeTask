package com.example.teamcity.api.requests.checked;

import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.generators.TestDataStorage;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.requests.CrudInterface;
import com.example.teamcity.api.requests.Request;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;

//Этот код — часть фреймворка для REST API-тестирования с RestAssured, использующего шаблон проектирования "Адаптер".
// Применяет паттерн "Адаптер", отделяя запросы без проверок от запросов с проверками

/** Разница с непроверяемым- мы проверяет ответ и де-сериализируем его
 * Мы тут опираемся на Unchecked запросы и добавляем к ним асерты
 *
 */
//CheckedBase делегирует выполнение запросов классу UncheckedBase,
    //CheckedBase – обёртка над UncheckedBase, которая добавляет валидацию ответа.
// но добавляет валидацию (assertThat().statusCode(HttpStatus.SC_OK))
//Это позволяет разделить логику отправки запроса и его проверки.
    //<T extends BaseModel> - поддерживаем любой дочерний тип Бейзмодел
    //<T extends BaseModel>  нужно чтобы кастовать возващаемые методами объекты к нужно
@SuppressWarnings("unchecked") //убираем предупреждения о неверном кастовании
public final class CheckedBase<T extends BaseModel> extends Request implements CrudInterface {
//Анчекет должен быть файнал тк не должен меняться: он предоставил ручки по созданию и больше не должен мяняться
    private final UncheckedBase uncheckedBase;

// ✔ Конструктор вызывает конструктор родительского класса Request.
// ✔ Вместо того, чтобы дублировать код запросов,
// CheckedBase просто создаёт объект UncheckedBase и передаёт ему spec и endpoint.
//✔ Это означает, что UncheckedBase уже умеет делать запросы, но без проверок.

    public CheckedBase(RequestSpecification spec, Endpoint endpoint) {
        super(spec, endpoint);
        // Чтобы не дублировать все запросы, мы просто обращаемся к анчектбейс
        this.uncheckedBase= new UncheckedBase(spec,endpoint);
    }
//✔ Метод create(BaseModel model) отправляет POST-запрос через UncheckedBase.
//✔ После отправки запроса добавляется валидация:
//
//.then().assertThat().statusCode(HttpStatus.SC_OK) → проверяет, что статус-код 200 (OK).
//✔ Затем результат конвертируется в Java-объект:

    @Override
    public T create(BaseModel model) {
        var createdModel = (T) uncheckedBase
                .create(model)
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                //Это значит, что endpoint.getModelClass() возвращает класс модели,
                // в которую API-ответ преобразуется автоматически.
                .extract().as(endpoint.getModelClass());

        // В этом месте мы уверены, что мы создали сущность
        TestDataStorage.getInstance().addCreatedEntity(endpoint,createdModel);
        return createdModel;
    }

    @Override
    public T read(String id) {
        return (T) uncheckedBase
                .read(id)
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .extract().as(endpoint.getModelClass());
    }

    @Override
    public T update(String id, BaseModel model) {
        return (T) uncheckedBase
                .update(id, model)
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .extract().as((endpoint.getModelClass()));
    }

    @Override
    public Object delete(String id) {
        return uncheckedBase
                .delete(id)
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .extract().asString();
    }
}
