package by.kaminsky.service;

import by.kaminsky.entity.Material;

import java.util.List;
import java.util.Optional;

public interface MaterialService {

    void save(Material material);

    void update(Long id, Material material);

    Material getById(Long id);

    List<Material> getAllByName(String name);

    Material getByNameAndSpecific(String name, String specific);

    void delete(Long id);

    Optional<Material> getOptionalByNameAndSpecific(String name, String specific);


}
