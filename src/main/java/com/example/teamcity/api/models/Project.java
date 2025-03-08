package com.example.teamcity.api.models;

import com.example.teamcity.api.annotations.Optional;
import com.example.teamcity.api.annotations.Parameterizable;
import com.example.teamcity.api.annotations.Random;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Project extends BaseModel implements Identifiable {
    @Random
    @Parameterizable
    private String id;

    @Random
    @Parameterizable
    private String name;

    @Optional
    private ParentProject parentProject;

    private Boolean copyAllAssociatedSettings;

    @Optional
    private Map<String, String> projectsIdsMap;

    @Optional
    private Map<String, String> buildTypesIdsMap;

    @Optional
    private Map<String, String> vcsRootsIdsMap;

    @Optional
    private Project sourceProject;
}
