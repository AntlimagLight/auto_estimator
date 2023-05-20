package by.kaminsky.service;

import by.kaminsky.entity.Material;
import by.kaminsky.repository.MaterialsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static by.kaminsky.utils.ValidationUtils.assertExistence;
import static by.kaminsky.utils.ValidationUtils.assertNotExistence;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialServiceImpl implements MaterialService {

    private final MaterialsRepository materialsRepository;

    @Override
    @Transactional
    public void save(Material material) {
        log.info("Save: {}", material.getName());
        assertNotExistence(materialsRepository.findByNameAndSpecificAndSource(material.getName(),
                material.getSpecific(), material.getSource()), "Material already exist");
        material.setLastUpdate(LocalDateTime.now());
        materialsRepository.save(assertExistence(material, "Missing material to save"));
    }

    @Override
    @Transactional
    public void update(Long id, Material material) {
        log.info("Update: {}, {}", id, material.getName());
        assertExistence(materialsRepository.findById(id), "Material not found");
        material.setId(id);
        material.setLastUpdate(LocalDateTime.now());
        materialsRepository.save(material);
    }

    @Override
    @Transactional(readOnly = true)
    public Material getById(Long id) {
        log.info("Get by id: {}", id);
        return assertExistence(materialsRepository.findById(id), "Material not found");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Material> getAllByName(String name) {
        return materialsRepository.findAllByName(name);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleted: {}", id);
        assertExistence(materialsRepository.findById(id), "Material not found");
        materialsRepository.deleteById(id);
    }

    @Override
    public Optional<Material> getOptionalByNameAndSpecificAndSource(String name, String specific, String source) {
        return materialsRepository.findByNameAndSpecificAndSource(name, specific, source);
    }

}
