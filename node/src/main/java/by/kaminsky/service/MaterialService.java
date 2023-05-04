package by.kaminsky.service;

import by.kaminsky.entity.Material;

import java.util.List;

public interface MaterialService {

    void save(Material material);

    void update(Long id, Material material);

    Material getById(Long id);

    List<Material> getAllByName(String name);

    Material getByNameAndSpecific(String name, String specific);

    void delete(Long id);


}
