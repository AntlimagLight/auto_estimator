package by.kaminsky.service;

import by.kaminsky.entity.Material;
import by.kaminsky.repository.MaterialsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static by.kaminsky.utils.ValidationUtils.assertExistence;
import static by.kaminsky.utils.ValidationUtils.assertNotExistence;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialServiceImpl implements MaterialService {

    private final MaterialsRepository materialsRepository;

    @Transactional
    public void save(Material material) {
        log.info("Save: {}, {}", material.getId(), material.getName());
        assertNotExistence(materialsRepository.findByNameAndSpecific(material.getName(), material.getSpecific()),
                "Material already exist");
        materialsRepository.save(assertExistence(material, "Missing material to save"));
    }

    @Transactional
    public void update(Long id, Material material) {
        log.info("Update: {}, {}", material.getId(), material.getName());
        assertExistence(materialsRepository.findById(id), "Material not found");
        material.setId(id);
        materialsRepository.save(material);
    }

    @Transactional(readOnly = true)
    public Material getById(Long id) {
        log.info("Get by id: {}", id);
        return assertExistence(materialsRepository.findById(id), "Material not found");
    }

    @Transactional(readOnly = true)
    public List<Material> getAllByName(String name) {
        return materialsRepository.findAllByName(name);
    }

    @Transactional(readOnly = true)
    public Material getByNameAndSpecific(String name, String specific) {
        log.info("Get by name and specific: {}, {}", name, specific);
        return assertExistence(materialsRepository.findByNameAndSpecific(name, specific), "Material not found");
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleted: {}", id);
        assertExistence(materialsRepository.findById(id), "Material not found");
        materialsRepository.deleteById(id);
    }

}
