package com.example.teamcity.api.requests.checked;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.generators.TestDataStorage;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.requests.interfaces.CrudInterface;
import com.example.teamcity.api.requests.Request;
import com.example.teamcity.api.requests.interfaces.SearchInterface;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;

import java.util.List;
import java.util.Optional;

/**
 * Базовый класс для проверяемых запросов к API.
 * Реализует базовые CRUD операции и поиск с валидацией ответов.
 * @param <T> тип модели данных
 */
public final class CheckedBase<T extends BaseModel> extends Request implements CrudInterface, SearchInterface {
    private final UncheckedBase uncheckedBase;

    /**
     * Создает новый экземпляр проверяемых запросов
     *
     * @param spec        спецификация запроса
     * @param apiEndpoint эндпоинт API
     */
    public CheckedBase(RequestSpecification spec, ApiEndpoint apiEndpoint) {
        super(spec, apiEndpoint);
        this.uncheckedBase = new UncheckedBase(spec, apiEndpoint);
    }

    /**
     * Проверяет ответ на корректность.
     * @param response ответ от сервера
     * @param locator идентификатор запроса для сообщения об ошибке
     * @throws IllegalStateException если ответ пустой или статус не OK
     */
    private void validateResponse(Response response, String locator) {
        response.then().assertThat().statusCode(HttpStatus.SC_OK);
        if (response.getBody().asString().isEmpty()) {
            throw new IllegalStateException("Empty response for locator '%s'".formatted(locator));
        }
    }

    /**
     * Извлекает одну сущность из ответа
     * @param response ответ от сервера
     * @return сущность типа T
     */
    private T extractSingleEntity(Response response) {
        return response.as((Class<T>) apiEndpoint.getModelClass());
    }

    /**
     * Извлекает список сущностей из ответа
     * @param response ответ от сервера
     * @return список сущностей типа T
     */
    private List<T> extractEntityList(Response response) {
        return response.jsonPath().getList("project", (Class<T>) apiEndpoint.getModelClass());
    }

    /**
     * Создает новую сущность
     * @param model модель для создания
     * @return ответ сервера
     * @throws IllegalStateException если создание не удалось
     */
    @Override
    public Response create(BaseModel model) {
        Response response = uncheckedBase.create(model);
        validateResponse(response, model.toString());
        BaseModel createdModel = response.getBody().as(apiEndpoint.getModelClass());
        TestDataStorage.getInstance().addCreatedEntity(apiEndpoint, createdModel);
        return response;
    }

    /**
     * Читает сущность по идентификатору
     * @param id идентификатор сущности
     * @return найденная сущность
     * @throws IllegalStateException если сущность не найдена
     */
    @Override
    public T read(String id) {
        Response response = uncheckedBase.read(id);
        validateResponse(response, id);
        return extractSingleEntity(response);
    }

    /**
     * Обновляет существующую сущность
     * @param id    идентификатор сущности
     * @param model новые данные
     * @return обновленная сущность
     * @throws IllegalStateException если обновление не удалось
     */
    @Override
    public T update(String id, BaseModel model) {
        Response response = uncheckedBase.update(id, model);
        validateResponse(response, id);
        return extractSingleEntity(response);
    }

    /**
     * Удаляет сущность
     * @param id идентификатор сущности
     * @return результат удаления
     * @throws IllegalStateException если удаление не удалось
     */
    @Override
    public Object delete(String id) {
        Response response = uncheckedBase.delete(id);
        validateResponse(response, id);
        return response.asString();
    }

    // --- Методы поиска --- //

    /**
     * Ищет одну сущность по локатору
     * Если `count == 0`, возвращает `Optional.empty()`.
     */
    public Optional<T> findSingleByLocator(String locator) {
        Response response = uncheckedBase.findSingleByLocator(locator);
        validateResponse(response, locator);

        // Берём первый элемент массива "project"
        List<T> projects = response.jsonPath().getList("project", (Class<T>) apiEndpoint.getModelClass());
        return projects.isEmpty() ? Optional.empty() : Optional.of(projects.get(0));
    }


    /**
     * Ищет все сущности по локатору
     * Если `count == 0`, возвращает пустой список.
     */
    @Override
    public Object findAllByLocator(String locator) {
        Response response = uncheckedBase.findAllByLocator(locator);
        validateResponse(response, locator);
        return response.jsonPath().getList("project", apiEndpoint.getModelClass());
    }



    /**
     * Читает все сущности без локатора
     * Если `count == 0`, возвращает пустой список.
     */
    @Override
    public List<T> readAll() {
        Response response = uncheckedBase.readAll();
        validateResponse(response, "all");

        Integer count = response.jsonPath().getInt("count");
        if (count == null || count == 0) {
            return List.of();
        }

        return extractEntityList(response);
    }
}
